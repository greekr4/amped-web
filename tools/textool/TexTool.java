import com.cyberstep.onstage.gfx.Texture;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.*;
import javax.imageio.ImageIO;

/** .tex(직렬화 Texture) ↔ PNG 변환. dump <tex> <png> | import <tex-in> <png> <tex-out> | render <tex-in> <text> <tex-out> [fontpx] */
public class TexTool {
    static Texture load(File f) throws Exception { try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)))) { return (Texture) in.readObject(); } }
    static void save(Texture t, File f) throws Exception { try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) { out.writeObject(t); } }
    static byte[] inflate(byte[] c, int size) throws Exception { byte[] out = new byte[size]; InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(c)); int n = 0, r; while (n < size && (r = in.read(out, n, size - n)) != -1) n += r; return out; }
    static byte[] deflate(byte[] p) throws Exception { ByteArrayOutputStream bo = new ByteArrayOutputStream(); DeflaterOutputStream d = new DeflaterOutputStream(bo); d.write(p); d.close(); return bo.toByteArray(); }
    static int bits(int pixtype) { switch (pixtype) { case 0: case 2: return 8; case 3: return 32; case 4: return 1; default: return 24; } }

    static BufferedImage toImage(Texture t) throws Exception {
        int w = t.width, h = t.height, b = bits(t.pixtype);
        byte[] p = t.cpix != null ? inflate(t.cpix, w * h * b / 8) : t.pix;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int i = y * w + x, argb;
            switch (t.pixtype) {
                case 0: { int k = p[i] & 0xff; argb = 0xff000000 | ((int)(t.r[k]*255)&0xff) << 16 | ((int)(t.g[k]*255)&0xff) << 8 | ((int)(t.b[k]*255)&0xff); break; }
                case 2: argb = (p[i] & 0xff) << 24 | 0xffffff; break;
                case 3: argb = (p[i*4+3] & 0xff) << 24 | (p[i*4] & 0xff) << 16 | (p[i*4+1] & 0xff) << 8 | (p[i*4+2] & 0xff); break;
                case 4: { int bit = (p[y * ((w + 7) / 8) + (x >> 3)] >> (7 - (x & 7))) & 1; argb = bit != 0 ? 0xffffffff : 0xff000000; break; }
                default: argb = 0xff000000 | (p[i*3] & 0xff) << 16 | (p[i*3+1] & 0xff) << 8 | (p[i*3+2] & 0xff);
            }
            img.setRGB(x, y, argb);
        }
        return img;
    }
    /** 이미지 → 텍스처 픽셀. 알파가 있으면 RGBA(3), 아니면 RGB(1). 원본이 alpha(2)면 alpha 유지 */
    static void fromImage(Texture t, BufferedImage img) throws Exception {
        int w = img.getWidth(), h = img.getHeight();
        t.width = w; t.height = h; t.r = t.g = t.b = null;
        int type = t.pixtype == 2 ? 2 : (img.getColorModel().hasAlpha() ? 3 : 1);
        if (type == 1) { boolean anyAlpha = false; for (int y = 0; y < h && !anyAlpha; y++) for (int x = 0; x < w; x++) if ((img.getRGB(x, y) >>> 24) != 255) { anyAlpha = true; break; } if (anyAlpha) type = 3; }
        t.pixtype = type;
        byte[] p = new byte[w * h * bits(type) / 8];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int i = y * w + x, c = img.getRGB(x, y);
            if (type == 2) p[i] = (byte) (c >>> 24);
            else if (type == 3) { p[i*4] = (byte)(c >> 16); p[i*4+1] = (byte)(c >> 8); p[i*4+2] = (byte)c; p[i*4+3] = (byte)(c >>> 24); }
            else { p[i*3] = (byte)(c >> 16); p[i*3+1] = (byte)(c >> 8); p[i*3+2] = (byte)c; }
        }
        t.pix = p; t.cpix = deflate(p);
    }

    static Font baseFont;
    static void renderTex(File in, File outFile, String text, String modeSpec, int px, String fillHex, String outlineHex) throws Exception {
        Texture t = load(in); BufferedImage src = toImage(t);
        int w = src.getWidth(), h = src.getHeight();
        String mode = modeSpec; double y0f = 0, y1f = 1; String fillBg = null;
        { String[] m = modeSpec.split(":"); mode = m[0]; for (int i = 1; i < m.length; i++) { if (m[i].contains("-")) { String[] r = m[i].split("-"); y0f = Double.parseDouble(r[0]); y1f = Double.parseDouble(r[1]); } else fillBg = m[i]; } }
        int ry0 = (int) (h * y0f), ry1 = (int) (h * y1f);
        Color fill = new Color((int) Long.parseLong(fillHex, 16)), outline = new Color((int) Long.parseLong(outlineHex, 16));
        BufferedImage up = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) up.setRGB(x, y, src.getRGB(x, h - 1 - y)); // GL 은 아래→위 저장
        if (mode.equals("clear")) { for (int y = ry0; y < ry1; y++) for (int x = 0; x < w; x++) up.setRGB(x, y, 0); }
        else if (mode.equals("fill")) { int bg = 0xff000000 | (int) Long.parseLong(fillBg, 16); for (int y = ry0; y < ry1; y++) for (int x = 0; x < w; x++) up.setRGB(x, y, bg); }
        else { // inpaint: 구간 안의 불투명 픽셀 중 어두운 쪽 중앙값을 배경으로 보고, 그보다 눈에 띄게 밝은 픽셀(글자)을 배경색으로 덮는다
            java.util.List<Integer> lum = new java.util.ArrayList<>();
            for (int y = ry0; y < ry1; y++) for (int x = 0; x < w; x++) { int c = up.getRGB(x, y); if ((c >>> 24) < 200) continue; lum.add(((c >> 16) & 255) + ((c >> 8) & 255) + (c & 255)); }
            java.util.Collections.sort(lum); int med = lum.isEmpty() ? 150 : lum.get(lum.size() / 2);
            java.util.List<Integer> rs = new java.util.ArrayList<>(), gs = new java.util.ArrayList<>(), bs = new java.util.ArrayList<>();
            for (int y = ry0; y < ry1; y++) for (int x = 0; x < w; x++) { int c = up.getRGB(x, y); if ((c >>> 24) < 200) continue; int l = ((c >> 16) & 255) + ((c >> 8) & 255) + (c & 255); if (l <= med) { rs.add((c >> 16) & 255); gs.add((c >> 8) & 255); bs.add(c & 255); } }
            java.util.Collections.sort(rs); java.util.Collections.sort(gs); java.util.Collections.sort(bs);
            int bg = rs.isEmpty() ? 0xff303848 : 0xff000000 | rs.get(rs.size()/2) << 16 | gs.get(gs.size()/2) << 8 | bs.get(bs.size()/2);
            boolean flat = mode.equals("flat"); // flat: 구간의 불투명 픽셀 전부를 배경색으로(모양은 알파로 유지)
            for (int y = ry0; y < ry1; y++) for (int x = 0; x < w; x++) { int c = up.getRGB(x, y); if ((c >>> 24) < 200) continue; int l = ((c >> 16) & 255) + ((c >> 8) & 255) + (c & 255); if (flat || l > med + 25) up.setRGB(x, y, bg); }
        }
        Graphics2D g = up.createGraphics(); g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (baseFont == null) baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("fontfile", "assets/fonts/NotoSansKR-Regular.ttf")));
        String[] lines = text.split("\n"); int rh = ry1 - ry0;
        int size = px > 0 ? px : Math.max(8, rh / lines.length - 4);
        Font f; FontMetrics fm;
        while (true) { f = baseFont.deriveFont(Font.BOLD, (float) size); fm = g.getFontMetrics(f); int maxw = 0; for (String l : lines) maxw = Math.max(maxw, fm.stringWidth(l)); if (px > 0 || maxw <= w - 8 || size <= 8) break; size--; }
        g.setFont(f);
        int th = fm.getHeight() * lines.length, yb = ry0 + (rh - th) / 2 + fm.getAscent();
        for (int i = 0; i < lines.length; i++) { int tw = fm.stringWidth(lines[i]); int x0 = (w - tw) / 2, y = yb + i * fm.getHeight();
            g.setColor(outline); for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) if (dx != 0 || dy != 0) g.drawString(lines[i], x0 + dx, y + dy);
            g.setColor(fill); g.drawString(lines[i], x0, y); }
        g.dispose();
        BufferedImage down = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB); for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) down.setRGB(x, y, up.getRGB(x, h - 1 - y));
        fromImage(t, down); save(t, outFile); System.out.println("rendered " + outFile.getName() + " '" + text.replace("\n", "|") + "' px=" + size + " pixtype=" + t.pixtype);
    }

    public static void main(String[] a) throws Exception {
        if (a[0].equals("dump")) { Texture t = load(new File(a[1])); ImageIO.write(toImage(t), "png", new File(a[2])); System.out.println(a[1] + " " + t.width + "x" + t.height + " pixtype=" + t.pixtype + " name=" + t.name); }
        else if (a[0].equals("dumpdir")) { File out = new File(a[2]); out.mkdirs(); for (File f : new File(a[1]).listFiles()) if (f.getName().endsWith(".tex")) { try { Texture t = load(f); ImageIO.write(toImage(t), "png", new File(out, f.getName() + ".png")); System.out.println(f.getName() + " " + t.width + "x" + t.height + " pixtype=" + t.pixtype); } catch (Exception e) { System.out.println(f.getName() + " FAIL " + e); } } }
        else if (a[0].equals("import")) { Texture t = load(new File(a[1])); fromImage(t, ImageIO.read(new File(a[2]))); save(t, new File(a[3])); System.out.println("wrote " + a[3] + " pixtype=" + t.pixtype); }
        else if (a[0].equals("scan")) { // 임의 직렬화 객체 안의 Texture 를 전부 덤프
            Object root; try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(a[1])))) { root = in.readObject(); }
            File out = new File(a[2]); out.mkdirs(); java.util.IdentityHashMap<Object,Boolean> seen = new java.util.IdentityHashMap<>(); java.util.ArrayDeque<Object[]> q = new java.util.ArrayDeque<>(); q.add(new Object[]{root, "root"}); int n = 0;
            while (!q.isEmpty()) { Object[] e = q.poll(); Object o = e[0]; String path = (String) e[1]; if (o == null || seen.containsKey(o)) continue; seen.put(o, true);
                Class<?> c = o.getClass(); if (c.isPrimitive() || o instanceof String || o instanceof Number || o instanceof Boolean || o instanceof Character) continue;
                if (o instanceof Texture) { Texture t = (Texture) o; String nm = (t.name == null ? "" : t.name).replaceAll("[^A-Za-z0-9._-]", "_"); try { ImageIO.write(toImage(t), "png", new File(out, String.format("%03d_%s_%dx%d.png", n, nm, t.width, t.height))); System.out.println(n + " " + path + " name=" + t.name + " " + t.width + "x" + t.height + " pixtype=" + t.pixtype); } catch (Exception ex) { System.out.println("fail " + path + " " + ex); } n++; continue; }
                if (c.isArray()) { if (!c.getComponentType().isPrimitive()) for (int i = 0; i < java.lang.reflect.Array.getLength(o); i++) q.add(new Object[]{java.lang.reflect.Array.get(o, i), path + "[" + i + "]"}); continue; }
                if (o instanceof java.util.Collection) { int i = 0; for (Object x : (java.util.Collection<?>) o) q.add(new Object[]{x, path + "{" + (i++) + "}"}); continue; }
                if (o instanceof java.util.Map) { for (Object x : ((java.util.Map<?,?>) o).values()) q.add(new Object[]{x, path + "{v}"}); continue; }
                for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) for (java.lang.reflect.Field f : k.getDeclaredFields()) { if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) continue; try { f.setAccessible(true); q.add(new Object[]{f.get(o), path + "." + f.getName()}); } catch (Throwable t) {} }
            }
        }
        else if (a[0].equals("render")) { renderTex(new File(a[1]), new File(a[2]), a[3].replace("\\n", "\n"), a[4], a.length > 5 && !a[5].isEmpty() ? Integer.parseInt(a[5]) : 0, a.length > 6 ? a[6] : "ffffff", a.length > 7 ? a[7] : "1a2430"); }
        else if (a[0].equals("batch")) { // batch <tsv> <src-resource-root> <dst-resource-root>
            int n = 0; for (String line : java.nio.file.Files.readAllLines(new File(a[1]).toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue; String[] c = line.split("\t");
                File src = new File(a[2], c[0]), dst = new File(a[3], c[0]); if (!src.isFile()) { System.out.println("missing " + c[0]); continue; }
                dst.getParentFile().mkdirs(); renderTex(src, dst, c[1].replace("\\n", "\n"), c[2], c.length > 3 && !c[3].isEmpty() ? Integer.parseInt(c[3]) : 0, c.length > 4 ? c[4] : "ffffff", c.length > 5 ? c[5] : "1a2430"); n++; }
            System.out.println("batch rendered " + n);
        }
        else if (a[0].equals("info")) { Texture t = load(new File(a[1])); System.out.println(t.width + "x" + t.height + " pixtype=" + t.pixtype + " name=" + t.name + " repeat=" + t.repeat + " anime=" + t.animecount); }
    }
}
