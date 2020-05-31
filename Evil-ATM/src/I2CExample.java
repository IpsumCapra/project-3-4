/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  I2CExample.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2019 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;

import javax.activation.UnsupportedDataTypeException;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class I2CExample {
    public static void main(String[] args) throws InterruptedException, IOException, UnsupportedDataTypeException {
        byte[] receivedData = new byte[5];
        String uid = "";
        boolean locked = false;
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
                        String tempUid = "";
                        for (int i = 0; i < 4; i++) {
                            tempUid += String.format("%x", receivedData[i]);
                        }

                        System.out.println("UID: "+tempUid);
                        System.out.println("Last key pressed: " + (char) receivedData[4]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        while (true) {

        }
    }
}