// 싱글 겟앰프드 웹(CheerpJ) 정적 서버
//
// CheerpJ 의 /app 가상 파일시스템 규칙에 맞춘다:
//  - 파일: Range 요청 필수 (tower-http ServeDir 가 처리)
//  - 디렉토리 stat: 슬래시 없는 디렉토리 URL → 301 로 슬래시 붙인 URL 로 보내야 디렉토리로 인식
//  - 디렉토리 목록: <dir>/index.list 를 줄바꿈 구분 이름 목록으로 응답(슬래시 없이)
//
// 실행: WEB_DIR=../web PORT=8777 cargo run --release
// 접속: http://localhost:8777/

use std::net::SocketAddr;
use std::path::{Component, Path, PathBuf};

use axum::body::Body;
use axum::extract::State;
use axum::http::{header, HeaderValue, Request, Response, StatusCode, Uri};
use axum::response::IntoResponse;
use tower::ServiceExt;
use tower_http::services::ServeDir;
use tower_http::set_header::SetResponseHeaderLayer;

#[derive(Clone)]
struct App {
    root: PathBuf,
}

fn safe_join(root: &Path, uri_path: &str) -> Option<PathBuf> {
    let decoded = percent_decode(uri_path);
    let mut p = root.to_path_buf();
    for c in Path::new(decoded.trim_start_matches('/')).components() {
        match c {
            Component::Normal(s) => p.push(s),
            Component::CurDir => {}
            _ => return None, // ParentDir 등은 거부
        }
    }
    Some(p)
}

fn percent_decode(s: &str) -> String {
    let b = s.as_bytes();
    let mut out = Vec::with_capacity(b.len());
    let mut i = 0;
    while i < b.len() {
        if b[i] == b'%' && i + 2 < b.len() {
            if let Ok(v) = u8::from_str_radix(std::str::from_utf8(&b[i + 1..i + 3]).unwrap_or("zz"), 16) {
                out.push(v);
                i += 3;
                continue;
            }
        }
        out.push(b[i]);
        i += 1;
    }
    String::from_utf8_lossy(&out).into_owned()
}

fn listing(dir: &Path) -> String {
    let mut names: Vec<String> = match std::fs::read_dir(dir) {
        Ok(rd) => rd
            .filter_map(|e| e.ok())
            .map(|e| e.file_name().to_string_lossy().into_owned())
            .filter(|n| n != "index.list")
            .collect(),
        Err(_) => Vec::new(),
    };
    names.sort();
    let mut s = names.join("\n");
    if !names.is_empty() {
        s.push('\n');
    }
    s
}

/// Range 를 지원하는 텍스트 응답(CheerpJ 는 디렉토리/목록에도 Range 를 보낸다)
fn text_with_range(req_range: Option<&HeaderValue>, body: String) -> Response<Body> {
    let bytes = body.into_bytes();
    let len = bytes.len();
    if let Some(r) = req_range.and_then(|v| v.to_str().ok()) {
        if let Some(spec) = r.strip_prefix("bytes=") {
            let mut it = spec.splitn(2, '-');
            let a = it.next().unwrap_or("");
            let b = it.next().unwrap_or("");
            let (start, end) = if a.is_empty() {
                let n: usize = b.parse().unwrap_or(0);
                (len.saturating_sub(n), len.saturating_sub(1))
            } else {
                let s: usize = a.parse().unwrap_or(0);
                let e: usize = if b.is_empty() { len.saturating_sub(1) } else { b.parse().unwrap_or(len.saturating_sub(1)) };
                (s, e.min(len.saturating_sub(1)))
            };
            let slice = if len == 0 || start > end { Vec::new() } else { bytes[start..=end].to_vec() };
            return Response::builder()
                .status(StatusCode::PARTIAL_CONTENT)
                .header(header::CONTENT_TYPE, "text/plain")
                .header(header::ACCEPT_RANGES, "bytes")
                .header(header::CONTENT_RANGE, format!("bytes {}-{}/{}", start, end, len))
                .header(header::CONTENT_LENGTH, slice.len())
                .body(Body::from(slice))
                .unwrap();
        }
    }
    Response::builder()
        .status(StatusCode::OK)
        .header(header::CONTENT_TYPE, "text/plain")
        .header(header::ACCEPT_RANGES, "bytes")
        .header(header::CONTENT_LENGTH, len)
        .body(Body::from(bytes))
        .unwrap()
}

async fn handle(State(app): State<App>, req: Request<Body>) -> Response<Body> {
    let uri: Uri = req.uri().clone();
    let path = uri.path().to_string();
    let Some(fs_path) = safe_join(&app.root, &path) else {
        return StatusCode::FORBIDDEN.into_response();
    };
    let range = req.headers().get(header::RANGE).cloned();

    // <dir>/index.list → 목록 생성
    if path.ends_with("/index.list") {
        let dir = fs_path.parent().map(|p| p.to_path_buf()).unwrap_or(app.root.clone());
        if dir.is_dir() {
            return text_with_range(range.as_ref(), listing(&dir));
        }
    }
    if fs_path.is_dir() {
        if !path.ends_with('/') {
            return Response::builder()
                .status(StatusCode::MOVED_PERMANENTLY)
                .header(header::LOCATION, format!("{}/", path))
                .body(Body::empty())
                .unwrap();
        }
        // 루트(또는 index.html 이 있는 디렉토리)는 페이지, 나머지는 목록
        if fs_path.join("index.html").is_file() && (path == "/" || req.headers().get(header::RANGE).is_none()) {
            let serve = ServeDir::new(&app.root).append_index_html_on_directories(true);
            return serve.oneshot(req).await.map(|r| r.map(Body::new)).unwrap_or_else(|_| StatusCode::NOT_FOUND.into_response());
        }
        return text_with_range(range.as_ref(), listing(&fs_path));
    }
    let serve = ServeDir::new(&app.root);
    serve.oneshot(req).await.map(|r| r.map(Body::new)).unwrap_or_else(|_| StatusCode::NOT_FOUND.into_response())
}

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt().with_target(false).init();
    let web_dir = std::env::var("WEB_DIR").unwrap_or_else(|_| "../web".to_string());
    let port: u16 = std::env::var("PORT").ok().and_then(|p| p.parse().ok()).unwrap_or(8777);
    let root = std::fs::canonicalize(&web_dir).expect("WEB_DIR");

    let no_cache = SetResponseHeaderLayer::overriding(header::CACHE_CONTROL, HeaderValue::from_static("no-store"));
    let app = axum::Router::new()
        .fallback(handle)
        .with_state(App { root: root.clone() })
        .layer(no_cache);

    let addr = SocketAddr::from(([0, 0, 0, 0], port));
    let listener = tokio::net::TcpListener::bind(addr).await.expect("bind");
    println!("amped-web-server: http://localhost:{port}/   (serving {})", root.display());
    axum::serve(listener, app).await.expect("serve");
}
