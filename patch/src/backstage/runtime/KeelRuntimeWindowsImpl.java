package backstage.runtime;

import amped.web.WebInput;
import amped.web.WebRuntime;
import backstage.runtime.audio.WebMpegPlayer;
import java.awt.Image;
import java.io.File;
import keel.gfx.GLGraphics;
import keel.gfx.PixelFormat;
import keel.gfx.WebPixelFormats;
import keel.runtime.DisplayMode;
import keel.runtime.audio.AudioPlayer;
import keel.runtime.event.*;
import keel.runtime.im.ActiveInputMethodRequests;
import keel.runtime.im.PassiveInputMethodRequests;

/**
 * 웹 포트 런타임. 이름은 원본(Windows 구현)을 그대로 써서 KeelRuntime 의 Class.forName 이 이 클래스를 고르게 한다.
 * 창 = <canvas>, 이벤트 = JS 큐 폴링, GL 컨텍스트 = WebGL(항상 current).
 */
public class KeelRuntimeWindowsImpl extends KeelRuntimeImpl {
    private static File commonDirectory;
    private final int[] evbuf = new int[6 * 256];
    private AudioPlayer mpegPlayer;
    private boolean windowCreated;
    int loopCount;

    public KeelRuntimeWindowsImpl() {
        GLGraphics.getVendor();
        this.displaymodelist = new DisplayMode[]{
            new DisplayMode(640, 480, 32, 60), new DisplayMode(800, 600, 32, 60), new DisplayMode(1024, 768, 32, 60)
        };
    }

    public void setCursorVisible(boolean b) { WebRuntime.setCursorVisible(b); }
    public AudioPlayer getMpegAudioPlayer() { if (mpegPlayer == null) mpegPlayer = WebMpegPlayer.getPlayer(); return mpegPlayer; }

    protected void _createWindow(String title, int w, int h) { WebRuntime.createWindow(title, w, h, false); windowCreated = true; }
    protected void _createDisplay(DisplayMode d) { WebRuntime.createWindow("GetAmped", d.getWidth(), d.getHeight(), true); windowCreated = true; }
    protected void _disposeDisplay() { if (windowCreated) { WebRuntime.disposeWindow(); windowCreated = false; } }
    protected final void updatePixelFormat() { this.pixelformatlist = WebPixelFormats.list(); }
    protected File getCommonDirectory() { return commonDirectory; }

    public void yield() {
        // 프레임 페이싱은 swapBuffers 가 rAF 로 담당한다. 포커스 없을 땐 쉬어 준다.
        if (!this.foreprocess) { try { Thread.sleep(50L); } catch (InterruptedException e) { } }
    }

    protected boolean _isDisplayAvailable(DisplayMode d) {
        for (int i = 0; i < displaymodelist.length; i++) if (displaymodelist[i].equals(d)) return true;
        return d.getWidth() > 0 && d.getHeight() > 0;
    }
    protected long _createGraphics(PixelFormat pf) { return 1L; }
    protected boolean _makeCurrent() { return true; }
    protected void _free() { }
    protected void _swapBuffers() { WebRuntime.swapBuffers(); }
    public boolean isGraphicsActive() { return this.graphicsThread != null; }

    /** JS 큐에서 이벤트를 꺼내 keel 이벤트로 변환 */
    protected Event _peekEvent() {
        if (!windowCreated) return null;
        int n = WebRuntime.pollEvents(evbuf);
        for (int i = 0; i < n; i++) {
            int o = i * 6, type = evbuf[o], id = evbuf[o + 1];
            Event e = null;
            if (type == 1) {
                int code = evbuf[o + 3], ch = evbuf[o + 4];
                // 게임 키 설정(Config.Key)은 문자 코드(118='v', 120='x')로 isPressed 를 묻는다 → VK 코드와 소문자 문자 코드 둘 다 기록
                if (id == KeyEvent.KEY_PRESSED || id == KeyEvent.KEY_RELEASED) {
                    boolean down = id == KeyEvent.KEY_PRESSED;
                    WebInput.set(code, down);
                    if (ch >= 32 && ch < 512) WebInput.set(Character.toLowerCase((char) ch), down);
                }
                e = new KeyEvent(id, evbuf[o + 2], code, (char) evbuf[o + 4]);
            } else if (type == 2) {
                int extra = evbuf[o + 5];
                e = new MouseEvent(id, evbuf[o + 2], evbuf[o + 3], evbuf[o + 4],
                        id == MouseEvent.MOUSE_WHEEL_MOVED ? 0 : extra, false, id == MouseEvent.MOUSE_WHEEL_MOVED ? extra : 0);
            } else if (type == 3) {
                if (id == ApplicationEvent.DEACTIVATED) WebInput.clear();
                e = new ApplicationEvent(id);
            }
            if (e != null) this.eventQueue.add(e);
        }
        return null; // 큐에 넣었으니 KeelRuntimeImpl.peekEvent 가 꺼낸다
    }

    public void enableInputMethods(boolean b) { }
    public void setInputMethodPassiveMode(PassiveInputMethodRequests reqs, boolean floating) { }
    public boolean setInputMethodActiveMode(ActiveInputMethodRequests reqs) { return false; }
    public void setIconImage(Image im) { }
    public boolean initRuntime(String dist, String dev) { return true; }

    static {
        commonDirectory = new File(System.getProperty("user.home", "/files"), "keel");
        if (!commonDirectory.exists()) commonDirectory.mkdirs();
    }
}
