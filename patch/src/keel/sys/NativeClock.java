package keel.sys;

/** 웹 포트: System.nanoTime 기반 고해상도 시계. unitTime = 1틱의 마이크로초(=0.001ms 단위) 환산. */
public class NativeClock implements Clock {
    static NativeClock singleton = new NativeClock();
    // 원본: unitTime 은 "1 tick 이 몇 마이크로초인가". 틱을 마이크로초로 정의하면 1.0
    double unitTime = 1.0;
    AdjustableClock clockAsMilli = new AdjustableClock(this, 1000.0);
    AdjustableClock clockAsMicro = new AdjustableClock(this, 1.0);
    private final long origin = System.nanoTime();

    NativeClock() { }
    public static NativeClock getClock() { return singleton; }
    public long getTime() { return (System.nanoTime() - origin) / 1000L; }
    public double getUnitTime() { return unitTime; }
    public long getTimeMillis() { return clockAsMilli.getTime(); }
    public long getTimeMicros() { return clockAsMicro.getTime(); }
}
