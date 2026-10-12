/*
 * Decompiled with CFR 0.152.
 */
package com.cyberstep.onstage.audio;

import com.cyberstep.onstage.audio.Audio;
import com.cyberstep.onstage.audio.Sound;
import com.cyberstep.onstage.audio.SoundResource;
import com.cyberstep.onstage.util.Sys;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

public class JSAudio
implements Audio {
    static final double loge10 = Math.log(10.0);
    static Mixer.Info[] mixers = AudioSystem.getMixerInfo();
    static String[] conditions = new String[mixers.length];
    Hashtable soundtable = new Hashtable(100);
    Mixer mixer;
    FloatControl masterctrl;

    public JSAudio() {
        try {
            this.setCondition(0);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public String[] getConditions() {
        return conditions;
    }

    public void setCondition(int n) {
        this.mixer = AudioSystem.getMixer(mixers[n]);
        if (this.mixer.isControlSupported(FloatControl.Type.VOLUME)) {
            this.masterctrl = (FloatControl)this.mixer.getControl(FloatControl.Type.VOLUME);
        }
    }

    public void setMasterVolume(float f) {
        if (this.masterctrl != null) {
            this.masterctrl.setValue(f);
        }
    }

    public float getMasterVolume() {
        return 100.0f;
    }

    public SoundResource getSoundResource(File file) {
        try {
            SoundResource soundResource = new SoundResource();
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat audioFormat = audioInputStream.getFormat();
            System.err.println("" + audioFormat + " " + audioFormat.isBigEndian());
            soundResource.freq = audioFormat.getFrameRate();
            soundResource.samplebits = audioFormat.getSampleSizeInBits();
            soundResource.channel = audioFormat.getChannels();
            soundResource.data = new byte[(int)((long)(audioFormat.getSampleSizeInBits() / 8) * audioInputStream.getFrameLength() * (long)soundResource.channel)];
            soundResource.issigned = audioFormat.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED;
            byte[] byArray = new byte[1024];
            int n = 0;
            int n2 = 0;
            do {
                n2 = audioInputStream.read(byArray, 0, 1024);
                System.arraycopy(byArray, 0, soundResource.data, n, n2);
            } while ((n += n2) < soundResource.data.length && n2 != -1);
            audioInputStream.close();
            return soundResource;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void preBuffer(SoundResource soundResource, int n) {
        if (soundResource != null) {
            Object v = this.soundtable.get(soundResource);
            JSSoundList jSSoundList = null;
            jSSoundList = v != null ? (JSSoundList)v : new JSSoundList(soundResource);
            this.soundtable.put(soundResource, jSSoundList);
            jSSoundList.makeChannel(n);
        }
    }

    public Sound getSound(SoundResource soundResource) {
        if (soundResource == null) {
            return null;
        }
        JSSoundList jSSoundList = (JSSoundList)this.soundtable.get(soundResource);
        if (jSSoundList == null) {
            jSSoundList = new JSSoundList(soundResource);
            this.soundtable.put(soundResource, jSSoundList);
        }
        return jSSoundList.getChannel();
    }

    public static void main(String[] stringArray) throws IOException {
        if (stringArray.length == 0) {
            System.err.println("Specify sound file name.");
            System.exit(0);
        }
        JSAudio jSAudio = new JSAudio();
        SoundResource soundResource = jSAudio.getSoundResource(new File(stringArray[0]));
        Sound sound = jSAudio.getSound(soundResource);
        sound.setVolume(10.0f);
        sound.setPan(-1.0f);
        sound.setPitch(2.0f);
        sound.play(5);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(stringArray[0].substring(0, stringArray[0].lastIndexOf(".")) + ".sr"));
        objectOutputStream.writeObject(soundResource);
        objectOutputStream.close();
        System.exit(0);
    }

    static {
        int n = 0;
        while (n < mixers.length) {
            Mixer mixer = AudioSystem.getMixer(mixers[n]);
            JSAudio.conditions[n] = mixer.toString();
            ++n;
        }
    }

    class JSSound
    implements Sound {
        Clip clip;
        FloatControl volctrl;
        FloatControl panctrl;
        FloatControl pitchctrl;
        float basefreq;
        float vol;
        float voloffs;
        float volrange;
        float pan;
        float pitch;
        long starttime;

        JSSound(SoundResource soundResource) throws Exception {
            Line.Info info = new Line.Info(Clip.class);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("line not supported");
                throw new Exception();
            }
            AudioFormat audioFormat = new AudioFormat(soundResource.freq, soundResource.samplebits, soundResource.channel, soundResource.issigned, soundResource.isbigendian);
            this.clip = (Clip)AudioSystem.getLine(info);
            this.clip.open(audioFormat, soundResource.data, 0, soundResource.data.length);
            this.basefreq = soundResource.freq;
            // 웹 포트: 브라우저 오디오는 PAN/SAMPLE_RATE 컨트롤이 없을 수 있다 → 지원되는 것만 잡는다
            this.volctrl = this.clip.isControlSupported(FloatControl.Type.MASTER_GAIN) ? (FloatControl)this.clip.getControl(FloatControl.Type.MASTER_GAIN) : null;
            this.panctrl = this.clip.isControlSupported(FloatControl.Type.PAN) ? (FloatControl)this.clip.getControl(FloatControl.Type.PAN) : null;
            this.pitchctrl = this.clip.isControlSupported(FloatControl.Type.SAMPLE_RATE) ? (FloatControl)this.clip.getControl(FloatControl.Type.SAMPLE_RATE) : null;
        }

        public void play() {
            try {
                this.clip.setFramePosition(0);
            }
            catch (AbstractMethodError abstractMethodError) {
                System.err.println("AbstractMethodError catched!");
                abstractMethodError.printStackTrace();
            }
            this.clip.start();
            this.starttime = Sys.getTime();
        }

        public void play(int n) {
            if (n == 1) {
                this.play();
            } else {
                this.clip.setFramePosition(0);
                this.clip.setLoopPoints(0, 20000);
                this.clip.loop(n == 0 ? -1 : n - 1);
            }
            this.starttime = Sys.getTime();
        }

        public void play(float f, float f2, int n) {
            int n2 = (int)(this.basefreq * f);
            int n3 = (int)(this.basefreq * f2);
            try {
                this.clip.setLoopPoints(n2, n3);
                this.clip.loop(n);
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.starttime = Sys.getTime();
        }

        public void stop() {
            this.clip.stop();
        }

        public void setPitch(float f) {
            this.pitch = f;
            if (this.pitchctrl != null) this.pitchctrl.setValue(this.basefreq * f);
        }

        public void setPan(float f) {
            this.pan = f;
            if (this.panctrl != null) this.panctrl.setValue(f);
        }

        public void setVolume(float f) {
            this.vol = f;
            f = Math.max(f, 1.0f);
            f = Math.min(f, 100.0f);
            if (this.volctrl != null) this.volctrl.setValue(20.0f * (float)(Math.log(f *= 0.02f) / loge10));
        }

        public float getPitch() {
            return this.pitch;
        }

        public float getPan() {
            return this.pan;
        }

        public float getVolume() {
            return this.vol;
        }

        public boolean isPlaying() {
            return this.clip.isRunning();
        }

    }

    class JSSoundList {
        SoundResource resource;
        Vector soundlist = new Vector();

        public JSSoundList(SoundResource soundResource) {
            this.resource = soundResource;
        }

        public void makeChannel(int n) {
            try {
                int n2 = this.soundlist.size();
                while (n2 < n) {
                    JSSound jSSound = new JSSound(this.resource);
                    this.soundlist.addElement(jSSound);
                    ++n2;
                }
            }
            catch (Exception exception) {
                System.err.println("JSAudio.JSSoundList.makeChannel failed.");
                exception.printStackTrace();
            }
        }

        public Sound getChannel() {
            if (this.soundlist.size() == 0) {
                this.makeChannel(1);
            }
            if (this.soundlist.size() == 0) {
                return null;
            }
            int n = 0;
            while (n < this.soundlist.size()) {
                if (!((JSSound)this.soundlist.elementAt(n)).isPlaying()) {
                    return (JSSound)this.soundlist.elementAt(n);
                }
                ++n;
            }
            JSSound jSSound = (JSSound)this.soundlist.elementAt(0);
            long l = jSSound.starttime;
            int n2 = 1;
            while (n2 < this.soundlist.size()) {
                JSSound jSSound2 = (JSSound)this.soundlist.elementAt(n2);
                if (l < jSSound2.starttime) {
                    jSSound = jSSound2;
                    l = jSSound2.starttime;
                }
                ++n2;
            }
            return jSSound;
        }
    }
}

