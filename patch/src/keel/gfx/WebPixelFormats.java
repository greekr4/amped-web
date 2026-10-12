package keel.gfx;

/** PixelFormat 생성자가 package-private 이라 같은 패키지에서 만들어 준다. */
public final class WebPixelFormats {
    private WebPixelFormats() { }
    public static PixelFormat[] list() {
        return new PixelFormat[]{ new PixelFormat(1L, 32, 8, 8, 8, 8, 24, 0, 0, 0, 0, 0, 8) };
    }
}
