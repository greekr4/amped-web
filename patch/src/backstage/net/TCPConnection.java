package backstage.net;

import java.io.*;
import keel.net.tunnel.*;

/** 웹 포트: Socket 대신 Loopback.Endpoint. */
public class TCPConnection implements Connection {
    public static final String PROTOCOL_NAME = "tcp";
    protected String hostName;
    protected int port, connectTimeout;
    protected Loopback.Endpoint ep;
    protected InputStream inputStream;
    protected OutputStream outputStream;

    public TCPConnection(String aHostName, int aPort, int aConnectTimeout, int s, int r, boolean nd) {
        hostName = aHostName; port = aPort; connectTimeout = aConnectTimeout > 0 ? aConnectTimeout : 5000;
    }
    public TCPConnection(Loopback.Endpoint ep) { this.ep = ep; }
    public void open() throws ConnectionException {
        if (ep == null) {
            try { ep = Loopback.connect(port, connectTimeout); }
            catch (IOException ex) { throw new ConnectionException("can't open loopback " + ex); }
        }
        inputStream = new BufferedInputStream(ep.getInputStream(), 16383);
        outputStream = new BufferedOutputStream(ep.getOutputStream(), 32767);
    }
    public void close() { if (ep != null) ep.close(); }
    public InputStream getInputStream() { return inputStream; }
    public OutputStream getOutputStream() { return outputStream; }
    public String getConnectedHostName() { return "127.0.0.1"; }
    public void setTimeout(int time) { if (ep != null) ep.setTimeout(time); }
}
