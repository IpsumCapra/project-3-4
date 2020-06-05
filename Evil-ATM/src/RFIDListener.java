import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RFIDListener extends Thread {
    ActionListener listener;
    private boolean blocked = false;

    public RFIDListener(ActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        while (!blocked) {
            GpioController controller = GpioFactory.getInstance();
            GpioPinDigitalInput updateTrigger = controller.provisionDigitalInputPin(RaspiPin.GPIO_00,
                    PinPullResistance.PULL_DOWN);
            updateTrigger.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    if (event.getState() == PinState.HIGH) {
                        try {
                            String IBAN = "";
                            byte[] receivedData = new byte[17];
                            I2CBus bus = I2CFactory.getInstance(1);
                            I2CDevice device = bus.getDevice(0x08);
                            device.read(receivedData, 0, 17);
                            for (int i = 0; i < 16; i++) {
                                IBAN += (char) receivedData[i];
                            }
                            listener.actionPerformed(new ActionEvent(this, 0, IBAN));
                        } catch (Exception ex) {

                        }
                    }
                }
            });
        }
    }

    public void setRFIDBlock(boolean block) {
        this.blocked = block;
    }
}
