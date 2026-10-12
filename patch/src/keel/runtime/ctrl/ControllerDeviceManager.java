package keel.runtime.ctrl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 웹 포트: 게임패드 미지원(빈 목록). 키보드 컨트롤러는 KeelRuntimeImpl 이 따로 만든다. */
public final class ControllerDeviceManager {
    private static ControllerDeviceManager singleton = new ControllerDeviceManager();
    private ControllerDevice[] controllerDevices = new ControllerDevice[0];
    public static ControllerDeviceManager getManager() { return singleton; }
    private ControllerDeviceManager() { }
    public List scanControllerDevices() { controllerDevices = new ControllerDevice[0]; return Collections.unmodifiableList(Arrays.asList(controllerDevices)); }
    public List getControllerDevices() { return Collections.unmodifiableList(Arrays.asList(controllerDevices)); }
    public ControllerDevice searchControllerDevice(String id) { return null; }
}
