package amped.web;

/** 브라우저 창/입력/프레임 네이티브. JS: Java_amped_web_WebRuntime_* */
public final class WebRuntime {
    private WebRuntime() { }
    /** 캔버스 생성/리사이즈(게임 해상도). fullscreen 이면 화면에 맞춰 확대. */
    public static native void createWindow(String title, int w, int h, boolean fullscreen);
    public static native void disposeWindow();
    /** 이벤트를 꺼낸다. 레코드당 6 int: [type,id,a,b,c,d]. 반환: 레코드 수. type 1=key(id,mods,code,char) 2=mouse(id,mods,x,y,click|wheel) 3=app(id) */
    public static native int pollEvents(int[] buf);
    /** 프레임 끝: WebGL 표시 + requestAnimationFrame 까지 양보 */
    public static native void swapBuffers();
    public static native void setCursorVisible(boolean b);
    public static native boolean hasFocus();
    public static native void log(String s);
    /** 브라우저 캔버스로 글리프 래스터화: out 에 size*size 알파(0~255) 기록, 반환값은 advance/size (실패 시 -1) */
    public static native float renderGlyph(int c, int size, byte[] out);
}
