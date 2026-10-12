import java.io.*;
public class Probe2 {
  public static void main(String[] a) throws Exception {
    System.setProperty("user.dir", "/app");
    System.out.println("PROBE2 user.home=" + System.getProperty("user.home") + " tmpdir=" + System.getProperty("java.io.tmpdir"));
    System.out.println("PROBE2 relExists(after user.dir=/app)=" + new File("probe/hello.txt").exists() + " abs=" + new File("probe/hello.txt").getAbsolutePath());
    try (FileInputStream in = new FileInputStream("probe/hello.txt")) { System.out.println("PROBE2 relRead=" + in.read()); } catch (Exception e) { System.out.println("PROBE2 relRead fail " + e); }
    String[] l = new File("/app/probe/sub").list();
    System.out.println("PROBE2 list(sub)=" + (l == null ? "null" : java.util.Arrays.toString(l)));
    File[] lf = new File("/app/probe/sub").listFiles();
    if (lf != null) for (File f : lf) System.out.println("PROBE2  " + f.getName() + " dir=" + f.isDirectory() + " len=" + f.length());
    File w = new File("/files/keel"); System.out.println("PROBE2 mkdirs=" + w.mkdirs() + " exists=" + w.exists());
    try (FileOutputStream o = new FileOutputStream("/files/keel/t.txt")) { o.write(65); }
    System.out.println("PROBE2 wrote=" + new File("/files/keel/t.txt").length());
    System.out.println("PROBE2 done");
  }
}
