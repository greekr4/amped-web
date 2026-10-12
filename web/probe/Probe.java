import java.io.*;
import java.net.*;
public class Probe {
  public static void main(String[] a) throws Exception {
    System.setProperty("os.name","Windows Web"); System.out.println("PROBE os.name=" + System.getProperty("os.name") + " java.version=" + System.getProperty("java.version"));
    System.out.println("PROBE user.dir=" + System.getProperty("user.dir") + " abs=" + new File(".").getAbsolutePath());
    File d = new File("/app/probe");
    String[] l = d.list();
    System.out.println("PROBE list(/app/probe)=" + (l == null ? "null" : java.util.Arrays.toString(l)) + " isDir=" + d.isDirectory());
    File f = new File("/app/probe/hello.txt");
    System.out.println("PROBE exists=" + f.exists() + " len=" + f.length());
    try (BufferedReader r = new BufferedReader(new FileReader(f))) { System.out.println("PROBE read=" + r.readLine()); }
    File rel = new File("probe/hello.txt");
    System.out.println("PROBE relExists=" + rel.exists());
    try {
      ServerSocket ss = new ServerSocket(0);
      System.out.println("PROBE serversocket port=" + ss.getLocalPort());
      final ServerSocket fss = ss;
      new Thread(() -> { try { Socket s = fss.accept(); s.getOutputStream().write(42); s.getOutputStream().flush(); System.out.println("PROBE accepted"); } catch (Exception e) { System.out.println("PROBE accept fail " + e); } }).start();
      Socket c = new Socket("localhost", ss.getLocalPort());
      System.out.println("PROBE loopback read=" + c.getInputStream().read());
    } catch (Throwable t) { System.out.println("PROBE socket fail " + t); }
    try { DatagramSocket ds = new DatagramSocket(); System.out.println("PROBE udp port=" + ds.getLocalPort()); } catch (Throwable t) { System.out.println("PROBE udp fail " + t); }
    try { System.out.println("PROBE localhost=" + InetAddress.getLocalHost().getHostAddress()); } catch (Throwable t) { System.out.println("PROBE inet fail " + t); }
    try {
      javax.sound.sampled.Mixer.Info[] m = javax.sound.sampled.AudioSystem.getMixerInfo();
      System.out.println("PROBE mixers=" + m.length);
      javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
      System.out.println("PROBE clip=" + clip);
    } catch (Throwable t) { System.out.println("PROBE sound fail " + t); }
    System.out.println("PROBE native=" + nat(new float[]{1,2,3}, "hi"));
    System.out.println("PROBE done");
  }
  static native String nat(float[] f, String s);
}
