import com.cyberstep.geometry.Quaternion;
import com.cyberstep.geometry.Vector3D;
import com.cyberstep.ring.data.AccessoryData;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * 악세사리(.accs = AccessoryData 직렬화) 생성/덤프 도구.
 *   dump a.accs [b.accs ...]
 *   make spec.txt out.accs
 *
 * spec 형식(# 주석):
 *   stat STR 3            (STR TEC SPD JMP DEF, 기본 스탯에 더해짐)
 *   playerdata /path.poa  (기술 교체용 poa, 생략 가능)
 *   part <부위> <모델경로> [x y z] [sx sy sz] [rx ry rz(도)]
 * 부위: 0 오른손, 7 왼손, 6/12 손목(오/왼), 13 몸통(등)
 */
public class AccsTool {
    public static void main(String[] a) throws Exception {
        if (a.length >= 2 && a[0].equals("dump")) { for (int i = 1; i < a.length; i++) dump(a[i]); return; }
        if (a.length == 3 && a[0].equals("make")) { make(a[1], a[2]); dump(a[2]); return; }
        System.err.println("usage: AccsTool dump <accs...> | make <spec.txt> <out.accs>"); System.exit(1);
    }

    static Field f(Class c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }

    static void dump(String path) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
        AccessoryData d = (AccessoryData) in.readObject(); in.close();
        StringBuilder sb = new StringBuilder(path + " :");
        for (String n : new String[]{"STR", "TEC", "SPD", "JMP", "DEF", "playerdata"}) sb.append(' ').append(n).append('=').append(f(AccessoryData.class, n).get(d));
        System.out.println(sb);
        for (int i = 0; i < d.size(); i++) {
            AccessoryData.Parts p = d.partsAt(i);
            System.out.println("  part place=" + p.place + " path=" + p.path + " loc=" + p.loc + " size=" + p.size + " rot=" + p.rot);
        }
    }

    static void make(String spec, String out) throws Exception {
        AccessoryData d = new AccessoryData();
        Vector parts = (Vector) f(AccessoryData.class, "parts").get(d);
        Constructor pc = AccessoryData.Parts.class.getDeclaredConstructor(int.class, String.class); pc.setAccessible(true);
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(spec), "UTF-8"));
        String line; int ln = 0;
        while ((line = r.readLine()) != null) {
            ln++; line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] t = line.split("\\s+");
            if (t[0].equals("stat")) {
                f(AccessoryData.class, t[1]).setInt(d, Integer.parseInt(t[2]));
            } else if (t[0].equals("playerdata")) {
                f(AccessoryData.class, "playerdata").set(d, t[1]);
            } else if (t[0].equals("part")) {
                AccessoryData.Parts p = (AccessoryData.Parts) pc.newInstance(Integer.parseInt(t[1]), t[2]);
                if (t.length >= 6) p.loc.set(fl(t[3]), fl(t[4]), fl(t[5]));
                if (t.length >= 9) p.size.set(fl(t[6]), fl(t[7]), fl(t[8]));
                if (t.length >= 12) {
                    Quaternion qx = Quaternion.getRotateQuaternion(rad(t[9]), new Vector3D(1, 0, 0));
                    Quaternion qy = Quaternion.getRotateQuaternion(rad(t[10]), new Vector3D(0, 1, 0));
                    Quaternion qz = Quaternion.getRotateQuaternion(rad(t[11]), new Vector3D(0, 0, 1));
                    Quaternion q = qy.synthesize(qx); q = q.synthesize(qz); q.normalize();
                    p.rot.set(q);
                }
                parts.add(p);
            } else throw new IOException(spec + ":" + ln + " 알 수 없는 항목 " + t[0]);
        }
        r.close();
        ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(out));
        o.writeObject(d); o.close();
    }

    static float fl(String s) { return Float.parseFloat(s); }
    static float rad(String s) { return (float) (Float.parseFloat(s) / 180.0 * Math.PI); }
}
