public class Probe4 {
  public static void main(String[] a) throws Exception {
    for (String cs : new String[]{"SJIS","Shift_JIS","MS932","windows-31J","EUC-JP","UTF8","Unicode","UTF-16"}) {
      System.out.println("PROBE4 " + cs + " supported=" + java.nio.charset.Charset.isSupported(cs));
    }
    byte[] b = new byte[]{(byte)0x93,(byte)0xfa,(byte)0x96,(byte)0x7b}; // 日本 in SJIS
    try { String s = new String(b, "SJIS"); System.out.println("PROBE4 decode=" + s + " codes=" + (int)s.charAt(0) + "," + (int)s.charAt(1)); } catch (Exception e) { System.out.println("PROBE4 decode fail " + e); }
    System.out.println("PROBE4 done");
  }
}
