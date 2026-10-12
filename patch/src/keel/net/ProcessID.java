package keel.net;

/** 웹 포트: 소켓 없이 프로세스 ID 생성(브라우저 탭당 임의값). */
class ProcessID {
    private static int id = 20000 + (int) (Math.random() * 20000);
    ProcessID() { }
    public static int getID() { return id; }
}
