package amped.web;

import keel.runtime.KeelRuntime;

/** 브라우저 진입점. KeelRuntime 의 static 초기화(플랫폼 판별)만 Windows 로 위장하고 즉시 원복한다. */
public class Main {
    public static void main(String[] args) throws Exception {
        String realOs = System.getProperty("os.name");
        System.setProperty("os.name", "Windows Web");
        System.setProperty("user.dir", "/app/game");
        System.setProperty("user.home", "/files");
        try {
            Class.forName("keel.runtime.KeelRuntime"); // static { Class.forName(OS_WINDOWS) ... }
        } finally {
            System.setProperty("os.name", realOs == null ? "Linux" : realOs); // CheerpJ 네이티브 라이브러리 이름 규칙 복원
        }
        String lang = "ja";
        if (args.length >= 2 && args[0].equals("-lang")) { lang = args[1]; String[] rest = new String[args.length - 2]; System.arraycopy(args, 2, rest, 0, rest.length); args = rest; }
        I18n.load(lang);
        if (args.length >= 1 && args[0].equals("-debug")) { System.setProperty("amped.debug", "1"); String[] rest = new String[args.length - 1]; System.arraycopy(args, 1, rest, 0, rest.length); args = rest; }
        // -accs <이름> : 싱글 플레이 캐릭터에 /accs/<이름>.accs 장착 (게임 오버라이드 UserDataTable 이 읽는다)
        if (args.length >= 2 && args[0].equals("-accs")) { System.setProperty("amped.accs", args[1]); String[] rest = new String[args.length - 2]; System.arraycopy(args, 2, rest, 0, rest.length); args = rest; }
        String[] a = args.length > 0 ? args : new String[]{"-entrypath", "/app/game/amped.jar", "com.cyberstep.ring.client.Game"};
        KeelRuntime.main(a);
    }
}
