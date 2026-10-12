package amped.web;

/** 브라우저 키 상태(AWT VK 코드 기준). WebRuntime 이 이벤트를 꺼낼 때 갱신한다. */
public final class WebInput {
    private static final boolean[] keys = new boolean[512];
    private WebInput() { }
    public static boolean isKeyDown(int code) { return code >= 0 && code < keys.length && keys[code]; }
    public static void set(int code, boolean down) { if (code >= 0 && code < keys.length) keys[code] = down; }
    public static void clear() { java.util.Arrays.fill(keys, false); }
}
