import java.io.*;
public class Probe3 {
  static void show(String p) { File f = new File(p); String[] l = f.list(); System.out.println("PROBE3 " + p + " -> path=" + f.getPath() + " isDir=" + f.isDirectory() + " list=" + (l == null ? "null" : l.length + (l.length > 0 ? " first=" + l[0] : ""))); }
  public static void main(String[] a) throws Exception {
    show("/app/game/resource/component/menu");
    show("/app/game/resource//component/menu");
    show("/app/game/resource/");
    show("/app/game/resource/component");
    System.setProperty("user.dir", "/app/game");
    String rd = System.getProperty("user.dir") + "/resource/";
    show(rd + "/component/menu");
    File f = new File(rd + "/component/menu/ampedmark.tex"); System.out.println("PROBE3 file exists=" + f.exists() + " len=" + f.length());
    System.out.println("PROBE3 done");
  }
}
