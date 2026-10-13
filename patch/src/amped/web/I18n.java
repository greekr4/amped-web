package amped.web;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 런타임 i18n. /app/game/l10n/<lang>.tsv (원문\t번역) 을 읽어
 *  - 클래스 로드 시 상수풀 문자열 치환 (CodedClassLoader)
 *  - 직렬화 리소스의 String 치환 (BasicResourceSource 의 resolveObject)
 *  - 텍스처 등 파일 덮어쓰기 (/app/game/l10n/<lang>/resource/...)
 */
public final class I18n {
    private static String lang = "ja";
    private static final Map<String, String> map = new HashMap<String, String>();
    private static final Map<String, Boolean> fileCache = new HashMap<String, Boolean>();
    private static String overrideRoot = null;
    private static java.util.Set<String> overrideSet = null; // files.txt 목록 (resource 기준 경로)
    private I18n() { }

    public static String lang() { return lang; }
    public static boolean active() { return !map.isEmpty(); }

    public static void load(String l) {
        lang = l == null || l.isEmpty() ? "ja" : l;
        map.clear(); overrideRoot = null;
        if (lang.equals("ja")) return;
        File f = new File("/app/game/l10n/" + lang + ".tsv");
        if (!f.exists()) { System.err.println("[I18n] no table for " + lang); return; }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("#")) continue;
                int t = line.indexOf('\t'); if (t < 0) continue;
                map.put(line.substring(0, t).replace("\\n", "\n"), line.substring(t + 1).replace("\\n", "\n"));
            }
        } catch (IOException e) { System.err.println("[I18n] " + e); }
        File dir = new File("/app/game/l10n/" + lang + "/resource");
        if (dir.exists()) overrideRoot = dir.getPath();
        // 덮어쓰기 파일 목록(files.txt)을 한 번에 읽어 둔다. 없으면 파일마다 isFile() 로 탐색하는데,
        // CheerpJ 에선 그게 리소스마다 HTTP 요청(404) 하나씩이라 화면 전환이 두 배로 느려진다.
        overrideSet = null;
        if (overrideRoot != null) {
            File list = new File("/app/game/l10n/" + lang + "/files.txt");
            if (list.exists()) {
                java.util.HashSet<String> set = new java.util.HashSet<String>();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(list), StandardCharsets.UTF_8))) {
                    String line; while ((line = r.readLine()) != null) { line = line.trim(); if (!line.isEmpty()) set.add(line.startsWith("/") ? line : "/" + line); }
                    overrideSet = set;
                } catch (IOException e) { System.err.println("[I18n] files.txt: " + e); }
            }
        }
        System.err.println("[I18n] " + lang + ": " + map.size() + " strings, overrides=" + overrideRoot + (overrideSet != null ? " (" + overrideSet.size() + " files)" : " (probe)"));
    }

    public static String translate(String s) {
        if (s == null || map.isEmpty()) return s;
        String t = map.get(s);
        return t != null ? t : s;
    }

    /** 리소스 경로("/component/menu/x.tex")에 대한 언어별 덮어쓰기 파일. 없으면 null */
    public static File override(String resPath) {
        if (overrideRoot == null) return null;
        String key = resPath.startsWith("/") ? resPath : "/" + resPath;
        if (overrideSet != null) return overrideSet.contains(key) ? new File(overrideRoot + key) : null;
        Boolean b = fileCache.get(key);
        File f = new File(overrideRoot + key);
        if (b == null) { b = Boolean.valueOf(f.isFile()); fileCache.put(key, b); }
        return b.booleanValue() ? f : null;
    }

    /** 클래스 바이트의 CONSTANT_Utf8 중 번역표에 있는 것을 치환 */
    public static byte[] patchClass(byte[] b) {
        if (map.isEmpty() || b.length < 10 || (b[0] & 0xff) != 0xCA) return b;
        try {
            int cpCount = ((b[8] & 0xff) << 8) | (b[9] & 0xff);
            ByteArrayOutputStream out = new ByteArrayOutputStream(b.length + 256);
            out.write(b, 0, 10);
            int pos = 10; boolean changed = false;
            for (int i = 1; i < cpCount; i++) {
                int tag = b[pos] & 0xff;
                if (tag == 1) {
                    int n = ((b[pos + 1] & 0xff) << 8) | (b[pos + 2] & 0xff);
                    String s = decodeMutf8(b, pos + 3, n);
                    String t = s == null ? null : map.get(s);
                    if (t != null) {
                        byte[] enc = encodeMutf8(t);
                        out.write(1); out.write(enc.length >> 8); out.write(enc.length & 0xff); out.write(enc, 0, enc.length); changed = true;
                    } else out.write(b, pos, 3 + n);
                    pos += 3 + n;
                } else {
                    int size;
                    switch (tag) { case 7: case 8: case 16: size = 3; break; case 15: size = 4; break; case 5: case 6: size = 9; break; default: size = 5; }
                    out.write(b, pos, size); pos += size;
                    if (tag == 5 || tag == 6) i++;
                }
            }
            if (!changed) return b;
            out.write(b, pos, b.length - pos);
            return out.toByteArray();
        } catch (RuntimeException e) { return b; }
    }

    private static String decodeMutf8(byte[] b, int off, int n) {
        try { return new DataInputStream(new ByteArrayInputStream(lenPrefixed(b, off, n))).readUTF(); } catch (IOException e) { return null; }
    }
    private static byte[] lenPrefixed(byte[] b, int off, int n) { byte[] r = new byte[n + 2]; r[0] = (byte) (n >> 8); r[1] = (byte) n; System.arraycopy(b, off, r, 2, n); return r; }
    private static byte[] encodeMutf8(String s) {
        try { ByteArrayOutputStream bo = new ByteArrayOutputStream(); DataOutputStream d = new DataOutputStream(bo); d.writeUTF(s); byte[] r = bo.toByteArray(); byte[] body = new byte[r.length - 2]; System.arraycopy(r, 2, body, 0, body.length); return body; }
        catch (IOException e) { return s.getBytes(StandardCharsets.UTF_8); }
    }
}
