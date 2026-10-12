/*
 * Decompiled with CFR 0.152.
 */
package backstage.runtime.ctrl;

import backstage.runtime.ctrl.BasicController;
import backstage.runtime.ctrl.ControllerConfigSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import keel.runtime.ctrl.Controller;
import keel.runtime.ctrl.ControllerDescriptor;
import keel.runtime.ctrl.ControllerElement;
import keel.runtime.ctrl.KeyboardDevice;
import keel.runtime.ctrl.KeyboardDeviceConversion;

public class KeyboardDeviceImpl
extends KeyboardDevice {
    List controllerList = Collections.synchronizedList(new ArrayList());

    public boolean isPressed(int keyCode) {
        return this.isPressedNative(keyCode);
    }

    protected Controller createControllerImpl(ControllerDescriptor descriptor, KeyboardDeviceConversion conversion) {
        ControllerImpl controller = new ControllerImpl(descriptor, conversion);
        this.controllerList.add(controller);
        return controller;
    }

    public void updateConfig() {
        List keyctrls = this.getControllers();
        int l = 0;
        while (l < keyctrls.size()) {
            ControllerImpl ctrl = (ControllerImpl)keyctrls.get(l);
            ctrl.setConversion(ControllerConfigSet.getConfigSet().getKeyboardDeviceConversion(l, ctrl.getDescriptor()));
            ++l;
        }
    }

    public void update() {
        int i = 0;
        while (i < this.controllerList.size()) {
            ((ControllerImpl)this.controllerList.get(i)).updateInternal();
            ++i;
        }
    }

    public void deleteController(Controller controller) {
        this.controllerList.remove(controller);
    }

    public List getControllers() {
        return new ArrayList(this.controllerList);
    }

    public KeyboardDeviceConversion getConversion(Controller controller) {
        if (!this.controllerList.contains(controller)) {
            throw new IllegalArgumentException("couldn't find controller : " + controller);
        }
        return (KeyboardDeviceConversion)((ControllerImpl)controller).getConversion().clone();
    }

    boolean isPressedNative(int code) { return amped.web.WebInput.isKeyDown(code); }

    class ControllerImpl
    extends BasicController {
        KeyboardDeviceConversion conversion;
        boolean onewayHatswitchPushed;
        int onewayHatswitchValue;

        public ControllerImpl(ControllerDescriptor descriptor, KeyboardDeviceConversion conv) {
            super(descriptor);
            this.conversion = conv;
            this.onewayHatswitchValue = 0;
            this.onewayHatswitchPushed = false;
        }

        void setConversion(KeyboardDeviceConversion conv) {
            this.conversion = conv;
        }

        boolean[] isButtonPressedCurrent(ControllerElement button) {
            int[] code = this.conversion.getKeyCode(button);
            int dimension = button.getNumElements();
            boolean[] pressed = new boolean[dimension];
            int j = 0;
            while (j < dimension) {
                pressed[j] = KeyboardDeviceImpl.this.isPressed(code[j]);
                ++j;
            }
            return pressed;
        }

        float[] getAxisPointCurrent(ControllerElement axis) {
            int[] code = this.conversion.getKeyCode(axis);
            int dimension = axis.getNumElements();
            float[] point = new float[dimension];
            int j = 0;
            while (j < dimension) {
                boolean bottom = KeyboardDeviceImpl.this.isPressed(code[j * 2]);
                boolean top = KeyboardDeviceImpl.this.isPressed(code[j * 2 + 1]);
                point[j] = bottom && top || !bottom && !top ? 0.0f : (bottom ? -1.0f : 1.0f);
                ++j;
            }
            return point;
        }

        float[] getHatswitchValueCurrent(ControllerElement hatswitch) {
            int[] code = this.conversion.getKeyCode(hatswitch);
            int dimension = hatswitch.getNumElements();
            int ways = code.length / dimension;
            float[] value = new float[dimension];
            float[] valueLast = (float[])this.stateMap.get(hatswitch);
            if (ways == 1) {
                int j = 0;
                while (j < dimension) {
                    value[j] = valueLast[j];
                    if (KeyboardDeviceImpl.this.isPressed(code[j])) {
                        if (!this.onewayHatswitchPushed) {
                            value[j] = value[j] + 1.5707964f;
                            if ((double)value[j] >= Math.PI * 2) {
                                value[j] = 0.0f;
                            }
                            this.onewayHatswitchPushed = true;
                        }
                    } else {
                        this.onewayHatswitchPushed = false;
                    }
                    ++j;
                }
            } else {
                int j = 0;
                while (j < dimension) {
                    value[j] = 0.0f;
                    int k = 0;
                    while (k < ways) {
                        if (KeyboardDeviceImpl.this.isPressed(code[j * ways + k])) {
                            value[j] = (float)(k * 2) * (float)Math.PI / (float)ways;
                            break;
                        }
                        ++k;
                    }
                    ++j;
                }
            }
            return value;
        }

        KeyboardDeviceConversion getConversion() {
            return this.conversion;
        }

        public void dispose() {
            KeyboardDeviceImpl.this.deleteController(this);
        }

        public void update() {
            this.updateInternal();
        }
    }
}

