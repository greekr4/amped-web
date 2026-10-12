package backstage.net;

import java.io.*;
import java.util.*;

/** 웹 포트: 브라우저에선 소켓이 금지되어 같은 JVM 안의 TCP 를 메모리 파이프로 대체한다. */
public final class Loopback {
    private Loopback() { }
    /** 포트 → 대기 큐(accept 대기) */
    private static final Map servers = new HashMap();

    public static synchronized boolean bind(int port) {
        Integer k = new Integer(port);
        if (servers.containsKey(k)) return false;
        servers.put(k, new LinkedList());
        return true;
    }
    public static synchronized void unbind(int port) { servers.remove(new Integer(port)); }

    /** 클라이언트 측: 서버 포트로 접속. 서버에게는 Endpoint 가 accept 큐로 전달된다. */
    public static Endpoint connect(int port, int timeoutMs) throws IOException {
        LinkedList q;
        synchronized (Loopback.class) {
            q = (LinkedList) servers.get(new Integer(port));
            if (q == null) throw new java.net.ConnectException("loopback: connection refused to port " + port);
        }
        Pipe c2s = new Pipe(), s2c = new Pipe();
        Endpoint client = new Endpoint(s2c, c2s, port), server = new Endpoint(c2s, s2c, port);
        synchronized (q) { q.add(server); q.notifyAll(); }
        return client;
    }
    /** 서버 측: accept (timeout ms, 0=무한). 없으면 null */
    public static Endpoint accept(int port, int timeoutMs) throws IOException {
        LinkedList q;
        synchronized (Loopback.class) { q = (LinkedList) servers.get(new Integer(port)); }
        if (q == null) throw new IOException("loopback: not bound " + port);
        synchronized (q) {
            long end = System.currentTimeMillis() + timeoutMs;
            while (q.isEmpty()) {
                long wait = timeoutMs <= 0 ? 0 : end - System.currentTimeMillis();
                if (timeoutMs > 0 && wait <= 0) return null;
                try { q.wait(wait); } catch (InterruptedException e) { throw new InterruptedIOException(); }
            }
            return (Endpoint) q.removeFirst();
        }
    }

    /** 한 방향 바이트 파이프(블로킹, 타임아웃 지원) */
    static final class Pipe {
        private byte[] buf = new byte[1 << 16];
        private int head, len;
        private boolean closed;
        int timeout = 0;
        synchronized void write(byte[] b, int off, int n) throws IOException {
            if (closed) throw new IOException("pipe closed");
            if (len + n > buf.length) {
                byte[] nb = new byte[Math.max(buf.length * 2, len + n)];
                for (int i = 0; i < len; i++) nb[i] = buf[(head + i) % buf.length];
                buf = nb; head = 0;
            }
            for (int i = 0; i < n; i++) buf[(head + len + i) % buf.length] = b[off + i];
            len += n; notifyAll();
        }
        synchronized int read(byte[] b, int off, int n) throws IOException {
            long end = System.currentTimeMillis() + timeout;
            while (len == 0) {
                if (closed) return -1;
                long wait = timeout <= 0 ? 0 : end - System.currentTimeMillis();
                if (timeout > 0 && wait <= 0) throw new java.net.SocketTimeoutException("loopback read timeout");
                try { wait(wait); } catch (InterruptedException e) { throw new InterruptedIOException(); }
            }
            int c = Math.min(n, len);
            for (int i = 0; i < c; i++) b[off + i] = buf[(head + i) % buf.length];
            head = (head + c) % buf.length; len -= c; notifyAll();
            return c;
        }
        synchronized int available() { return len; }
        synchronized void close() { closed = true; notifyAll(); }
    }

    public static final class Endpoint {
        final Pipe in, out; final int port;
        Endpoint(Pipe in, Pipe out, int port) { this.in = in; this.out = out; this.port = port; }
        public InputStream getInputStream() {
            return new InputStream() {
                public int read() throws IOException { byte[] b = new byte[1]; int n = in.read(b, 0, 1); return n < 0 ? -1 : (b[0] & 0xff); }
                public int read(byte[] b, int off, int n) throws IOException { if (n == 0) return 0; return in.read(b, off, n); }
                public int available() { return in.available(); }
                public void close() { in.close(); }
            };
        }
        public OutputStream getOutputStream() {
            return new OutputStream() {
                public void write(int b) throws IOException { out.write(new byte[]{(byte) b}, 0, 1); }
                public void write(byte[] b, int off, int n) throws IOException { out.write(b, off, n); }
                public void close() { out.close(); }
            };
        }
        public void setTimeout(int ms) { in.timeout = ms; }
        public void close() { in.close(); out.close(); }
    }
}
