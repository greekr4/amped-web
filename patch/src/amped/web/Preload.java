package amped.web;

import java.io.*;

/**
 * 선로딩 파일 조회. 페이지의 JS 가 게임 리소스를 병렬 fetch 해서 CheerpJ 메모리 FS(/str/game~resource~...)에 넣어 두면
 * 여기서 먼저 찾는다. /app/ 경로는 파일마다 HTTP 요청 2개(크기 확인 + 본문)가 순차로 나가 느리지만
 * /str/ 은 메모리라 즉시다. 아직 안 받았거나 목록에 없는 파일은 null → 호출자가 /app/ 로 간다.
 */
public final class Preload {
    private static int hit, miss;
    private Preload() { }

    /** appPath: "/app/game/resource/x.tex" 처럼 /app/ 로 시작하는 절대 경로 */
    public static InputStream open(String appPath) {
        if (appPath == null || !appPath.startsWith("/app/")) return null;
        while (appPath.indexOf("//") >= 0) appPath = appPath.replace("//", "/"); // resourceDir + "/x" 가 "//" 를 만든다
        // CheerpJ 의 /str/ 는 디렉토리가 없는 평면 저장소라 경로의 '/' 를 '~' 로 바꾼 이름을 쓴다 (index.html preloadPut 과 약속)
        File f = new File("/str/" + appPath.substring(5).replace('/', '~'));
        if (!f.isFile()) { if (miss < 3) System.err.println("[preload] miss " + f.getPath() + " exists=" + f.exists()); miss++; return null; }
        try { if (hit < 3) System.err.println("[preload] hit " + f.getPath()); hit++; return new BufferedInputStream(new FileInputStream(f), 4096); }
        catch (IOException e) { return null; }
    }

    public static String stats() { return "preload hit=" + hit + " miss=" + miss; }
}
