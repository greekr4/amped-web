package keel.net.creator;

import keel.net.Creator;
import keel.net.NetProperty;

/** 웹 포트: UDP 사용 불가 → TunnelFactory 가 아닌 객체를 돌려 등록에서 제외시킨다. */
public class UDPTunnelFactoryCreator implements Creator {
    public Object createInstance(NetProperty property) { return "udp-disabled"; }
}
