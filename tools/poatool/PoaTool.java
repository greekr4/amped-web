import com.cyberstep.ring.data.PlayerData;
import com.cyberstep.ring.scp.Scp;
import com.cyberstep.ring.util.Script;
import java.io.*;
import java.util.*;

/**
 * PlayerData(.poa) 패치 도구. 기존 poa 를 바탕으로 기술/타격정보/스크립트를 추가·교체해 새 poa 를 만든다.
 *   dump <poa>
 *   make <spec.txt> <out.poa>     (spec 안의 base 로 바탕 poa 지정)
 *
 * spec 형식(# 주석, 경로는 spec 파일 기준 상대경로):
 *   base <resource/player/style/xxx/player.poa>
 *   tech <이름> <A|B|AB> <상태문자열 S W R B F A 조합 또는 -> <스크립트명> [<이전기술> <from> <to>]
 *        같은 이름이 있으면 교체. 상태: S 서있음 W 걷기 R 달리기 B 브레이크 F 공중 A 조준
 *   deltech <이름>
 *   attack <이름> <공격모델> <피격sc> <power> <str> <time> <x> <y> <z> <pitch> [feedback]
 *        공격모델: poa resources 키(attack_s1.aoa) 또는 절대경로(/attack/hadou.aoa)
 *   script <이름.sc> <소스파일>      poa 내장 스크립트로 컴파일(defines.sc 헤더 자동)
 *   resource <키> <직렬화파일>      poa resources 에 객체 추가(.aoa 등)
 */
public class PoaTool {
    static final byte A = 1, B = 2, AB = 3;

    public static void main(String[] a) throws Exception {
        if (a.length == 2 && a[0].equals("dump")) { System.out.println(load(a[1])); return; }
        if (a.length == 3 && a[0].equals("make")) { make(new File(a[1]), new File(a[2])); return; }
        System.err.println("usage: PoaTool dump <poa> | make <spec.txt> <out.poa>"); System.exit(1);
    }

    static PlayerData load(String p) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(p));
        PlayerData d = (PlayerData) in.readObject(); in.close(); return d;
    }

    static String defines() throws IOException {
        File f = new File(System.getProperty("poatool.defines", "tools/poatool/defines.sc"));
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        for (String l; (l = r.readLine()) != null; ) if (l.startsWith("#define")) sb.append(l).append('\n');
        r.close(); return sb.toString();
    }

    static String read(File f) throws IOException {
        byte[] b = new byte[(int) f.length()]; DataInputStream in = new DataInputStream(new FileInputStream(f)); in.readFully(b); in.close();
        return new String(b, "UTF-8");
    }

    static int status(String s) {
        if (s.equals("-")) return 0;
        int v = 0;
        for (char c : s.toCharArray()) switch (c) {
            case 'S': v |= 1; break; case 'W': v |= 2; break; case 'R': v |= 4; break; case 'B': v |= 8; break;
            case 'F': v |= 0x20; break; case 'A': v |= 0x800; break;
            default: throw new IllegalArgumentException("status " + s);
        }
        return v;
    }

    static void make(File spec, File out) throws Exception {
        File dir = spec.getAbsoluteFile().getParentFile();
        PlayerData d = null;
        String defs = defines();
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(spec), "UTF-8"));
        String line; int ln = 0;
        while ((line = r.readLine()) != null) {
            ln++; line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] t = line.split("\\s+");
            String cmd = t[0];
            if (cmd.equals("base")) { d = load(new File(dir, t[1]).getPath()); continue; }
            if (d == null) throw new IOException(spec + ":" + ln + " base 가 먼저 와야 한다");
            if (cmd.equals("tech")) {
                byte btn = t[2].equals("A") ? A : t[2].equals("B") ? B : AB;
                String prev = t.length > 5 ? t[5] : ""; int from = t.length > 6 ? Integer.parseInt(t[6]) : 0, to = t.length > 7 ? Integer.parseInt(t[7]) : 0;
                d.deleteTechnique(t[1]);
                d.addTechnique(t[1], btn, status(t[3]), t[4], prev, from, to);
            } else if (cmd.equals("deltech")) {
                d.deleteTechnique(t[1]);
            } else if (cmd.equals("attack")) {
                // 주의: 디컴파일된 PlayerData.newAttack 은 테이블에 넣지 않는다 → 직접 put (시행착오 #1)
                PlayerData.Attack k = new PlayerData.Attack();
                d.attacks.put(t[1], k);
                k.attackmodel = t[2]; k.effect = t[3]; k.power = Float.parseFloat(t[4]); k.str = Float.parseFloat(t[5]);
                k.time = Integer.parseInt(t[6]); k.x = Float.parseFloat(t[7]); k.y = Float.parseFloat(t[8]); k.z = Float.parseFloat(t[9]);
                k.pitch = Float.parseFloat(t[10]); k.feedback = t.length > 11 ? t[11] : "";
            } else if (cmd.equals("script")) {
                String src = read(new File(dir, t[2]));
                Script s = new Script();
                s.source = src;
                s.prog = Scp.generate(defs + src);
                d.scripts.put(t[1], s);
            } else if (cmd.equals("resource")) {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(dir, t[2])));
                d.resources.put(t[1], in.readObject()); in.close();
            } else throw new IOException(spec + ":" + ln + " 알 수 없는 항목 " + cmd);
        }
        r.close();
        ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(out));
        o.writeObject(d); o.close();
        System.out.println(out + " : techs=" + d.techs.size() + " attacks=" + d.attacks.size() + " scripts=" + d.scripts.keySet());
    }
}
