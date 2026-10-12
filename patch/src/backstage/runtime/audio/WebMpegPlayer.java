package backstage.runtime.audio;

import java.io.*;
import keel.resource.Resource;
import keel.runtime.audio.AudioPlayer;

/** 웹 포트: BGM(mp3)을 브라우저 <audio> 로 재생. JS: Java_backstage_runtime_audio_WebMpegPlayer_* */
public class WebMpegPlayer implements AudioPlayer {
    static WebMpegPlayer singleton;
    boolean opened; boolean loop = true; float volume = 1f; int totalTime;
    public static synchronized WebMpegPlayer getPlayer() { if (singleton == null) singleton = new WebMpegPlayer(); return singleton; }

    static native int openNative(String absPath);
    static native void closeNative();
    static native void playNative();
    static native void stopNative();
    static native void setVolumeNative(float v);
    static native void setLoopNative(boolean b);
    static native void seekNative(int ms);
    static native int currentTimeNative();

    public void open(InputStream in) throws IOException { throw new IOException("stream BGM unsupported on web"); }
    public void open(Resource res) throws IOException { open(new File(res.getPath())); }
    public synchronized void open(File file) throws IOException {
        if (opened) close();
        totalTime = openNative(file.getAbsolutePath());
        if (totalTime < 0) throw new IOException("couldn't open " + file);
        opened = true; setLoopNative(loop); setVolumeNative(volume);
    }
    public void play() { if (opened) playNative(); }
    public void stop() { if (opened) stopNative(); }
    public void close() { if (opened) { closeNative(); opened = false; } }
    public boolean isOpened() { return opened; }
    public boolean isLoop() { return loop; }
    public void setLoop(boolean b) { loop = b; if (opened) setLoopNative(b); }
    public float getVolume() { return volume; }
    public void setVolume(float v) { volume = v; if (opened) setVolumeNative(v); }
    public int getTotalTime() { return totalTime; }
    public int getCurrentTime() { return opened ? currentTimeNative() : 0; }
    public void seek(int t) { if (opened) seekNative(t); }
}
