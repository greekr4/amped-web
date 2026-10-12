package keel.gfx;

/** 웹 포트: 네이티브 메모리 대신 자바 배열을 그대로 보관한다. JS는 TypedArray 참조로 받는다. */
public class NativeArray {
    int type = 0;
    private int size = 0;
    long nativePointer = 0L;
    Object data;

    public NativeArray(byte[] a, boolean unsigned) { type = unsigned ? 5121 : 5120; size = a.length; data = a; nativePointer = 1; }
    public NativeArray(byte[] a) { this(a, false); }
    public NativeArray(short[] a, boolean unsigned) { if (a == null) throw new NullPointerException(); type = unsigned ? 5123 : 5122; size = a.length; data = a; nativePointer = 1; }
    public NativeArray(short[] a) { this(a, false); }
    public NativeArray(int[] a, boolean unsigned) { if (a == null) throw new NullPointerException(); type = unsigned ? 5125 : 5124; size = a.length; data = a; nativePointer = 1; }
    public NativeArray(int[] a) { this(a, false); }
    public NativeArray(float[] a) { if (a == null) throw new NullPointerException(); type = 5126; size = a.length; data = a; nativePointer = 1; }
    public NativeArray(double[] a) { if (a == null) throw new NullPointerException(); type = 5130; size = a.length; data = a; nativePointer = 1; }

    private void copy(Object src, int srcOff, int dstOff, int len) {
        if (len <= 0) return;
        if (srcOff < 0 || dstOff < 0 || size < len + dstOff) throw new ArrayIndexOutOfBoundsException();
        System.arraycopy(src, srcOff, data, dstOff, len);
    }
    public void update(byte[] a) { check(a, 5120, 5121); copy(a, 0, 0, a.length); }
    public void update(byte[] a, int s, int d, int n) { check(a, 5120, 5121); copy(a, s, d, n); }
    public void update(short[] a) { check(a, 5122, 5123); copy(a, 0, 0, a.length); }
    public void update(short[] a, int s, int d, int n) { check(a, 5122, 5123); copy(a, s, d, n); }
    public void update(int[] a) { check(a, 5124, 5125); copy(a, 0, 0, a.length); }
    public void update(int[] a, int s, int d, int n) { check(a, 5124, 5125); copy(a, s, d, n); }
    public void update(float[] a) { check(a, 5126, 5126); copy(a, 0, 0, a.length); }
    public void update(float[] a, int s, int d, int n) { check(a, 5126, 5126); copy(a, s, d, n); }
    public void update(double[] a) { check(a, 5130, 5130); copy(a, 0, 0, a.length); }
    public void update(double[] a, int s, int d, int n) { check(a, 5130, 5130); copy(a, s, d, n); }
    private void check(Object a, int t1, int t2) { if (type != t1 && type != t2) throw new IllegalArgumentException("Invalid array type"); }

    public Object getData() { return data; }
    protected final void finalize() { data = null; nativePointer = 0L; }
    public String toString() { return "NativeArray(web) type:" + type + " size:" + size; }
    static final synchronized void compact(boolean b) { }
}
