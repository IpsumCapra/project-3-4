import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;


import javax.swing.*;

public class KeypadListener extends Thread {
    private final static String[] NUMPAD_CONTENT = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#" };
    JButton[] buttons;
    boolean deactivate = false;

    public KeypadListener(JButton[] buttons) {
        this.buttons = buttons;
    }

    public void run() {
        byte[] receivedData = new byte[5];
        GpioController controller = GpioFactory.getInstance();
        GpioPinDigitalInput updateTrigger = controller.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);
        updateTrigger.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if (event.getState() == PinState.HIGH) {
                    try {
                        I2CBus bus = I2CFactory.getInstance(1);
                        I2CDevice device = bus.getDevice(0x08);
                        device.read(receivedData, 0, 5);
                        char pressedKey = (char) receivedData[4];
                        System.out.println("Last key pressed: " + receivedData[4]);
                        for (int i = 0; i < NUMPAD_CONTENT.length; i++) {
                            if (String.valueOf(pressedKey).equals(NUMPAD_CONTENT[i])) {
                                buttons[i].doClick();
                            }
                        }
                    } catch (Exception ex) {

                    }
                }
            }
        });

        while (!deactivate) {

        }
    }

    public void deactivate() {
        deactivate = true;
    }
}
