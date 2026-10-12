package backstage.net;

import backstage.util.ConsumerThread;
import keel.net.ConnectionType;
import keel.net.tunnel.*;
import keel.net.type.TCPConnectionType;

/** 웹 포트: ServerSocket 대신 Loopback 레지스트리에 바인드한다. */
public class TCPConnectionServer implements ConnectionServer {
    private int portMin, range, timeout;
    protected int sendBufferSize, receiveBufferSize;
    protected boolean isTcpNoDelay;
    protected ConnectionAcceptor acceptor;
    private int port = -1;
    private ConsumerThread acceptThread;

    public TCPConnectionServer(int port, int aRange, int aTimeout, int aSendBufferSize, int aReceiveBufferSize, boolean tcpNoDelay) {
        this.portMin = port >= 0 ? port : 16529;
        this.range = aRange > 0 ? aRange : 10;
        this.timeout = aTimeout > 0 ? aTimeout : 200;
    }
    public void open() throws ConnectionException {
        for (int i = 0; i < range; i++) {
            if (Loopback.bind(portMin + i)) { port = portMin + i; break; }
        }
        if (port < 0) throw new ConnectionException("can't open loopback server");
        acceptThread = new ConsumerThread(new ConsumerThread.Consumable() {
            public void init() { }
            public void consume() throws Exception {
                Loopback.Endpoint ep = Loopback.accept(port, timeout);
                if (ep != null) acceptSocket(ep);
            }
            public void close() { Loopback.unbind(port); }
            public void error(Exception ex) { }
        }, "TCPConnectionServer(loopback)");
        acceptThread.start();
    }
    public void close() { if (acceptThread != null) { try { acceptThread.stop(); } catch (Exception e) { } acceptThread = null; } }
    public int getPort() { return port; }
    protected void acceptSocket(Loopback.Endpoint ep) {
        if (acceptor != null) acceptor.connectionAccepted(new TCPConnection(ep)); else ep.close();
    }
    public ConnectionType getConnectionType() { return new TCPConnectionType(); }
    public void setConnectionAcceptor(ConnectionAcceptor a) { this.acceptor = a; }
}
