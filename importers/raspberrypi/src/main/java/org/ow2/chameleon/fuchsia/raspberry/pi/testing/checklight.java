package org.ow2.chameleon.fuchsia.raspberry.pi.testing;

import com.pi4j.io.gpio.*;
import com.pi4j.wiringpi.Gpio;
/*
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
*/

//@Component(immediate = true)
//@Instantiate
public class checklight extends Thread {

    public checklight(){

        //GpioFactory.getDefaultProvider().setPullResistance(RaspiPin.GPIO_18,PinPullResistance.PULL_DOWN);

    }

    Boolean active=false;

    //@Validate
    public void validate(){

        active=true;

    }

    //@Invalidate
    public void invalidate(){

        active=false;

    }

    public void run(){


        String read="18";


        final GpioController io = GpioFactory.getInstance();


        //GpioPinDigitalMultipurpose pin=io.provisionDigitalMultipurposePin(RaspiPin.GPIO_18,PinMode.DIGITAL_INPUT); //InputPin(RaspiPin.GPIO_18);

        //while(active){

        /*

            pin.export(PinMode.DIGITAL_OUTPUT);
            pin.setState(PinState.LOW);
            pin.low();
            pin.export(PinMode.DIGITAL_INPUT);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


*/

            //GpioPinDigitalInput input=io.provisionDigitalInputPin(RaspiPin.GPIO_18, PinPullResistance.PULL_DOWN);

            /*
            Pin pin = new PinImpl(RaspiGpioProvider.NAME, Integer.valueOf(read), "GPIO "+read,
                    EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                    PinPullResistance.all());

            io.provisionDigitalOutputPin(pin).low();

             */



            //System.out.println("value:"+input.getState().getValue());

           /*
            while(pin.getState()==PinState.LOW){
                counter++;
            };
            */

            //input.setShutdownOptions(true,input.getState());
            //io.pulse(,input);
            //value=input.getState().getValue();


            //io.getProvisionedPins().remove(pin);
            //io.shutdown();


            //}

            /*
            p.addListener(new GpioPinListenerDigital(){

                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    System.out.println("Ping changed");
                }
            });
            */
        //}


        int port=18;

        com.pi4j.wiringpi.Gpio.wiringPiSetupGpio();
        com.pi4j.wiringpi.Gpio.piHiPri(0);

        com.pi4j.wiringpi.Gpio.delay(0);
        com.pi4j.wiringpi.Gpio.pullUpDnControl(port,Gpio.PUD_DOWN);
        com.pi4j.wiringpi.Gpio.pinMode(com.pi4j.wiringpi.Gpio.wpiPinToGpio(port), Gpio.INPUT);


        for (;;)
        {
            // here we want to control the multi-purpose GPIO pin
            // so we must reconfigure the pin mode first
            //pin.setMode(PinMode.DIGITAL_OUTPUT);

            /*
            com.pi4j.wiringpi.Gpio.delay(50);
            com.pi4j.wiringpi.Gpio.delayMicroseconds(50);
            com.pi4j.wiringpi.Gpio.piHiPri(99);

            com.pi4j.wiringpi.Gpio.wiringPiSetup();

            com.pi4j.wiringpi.Gpio.pinMode(18, Gpio.OUTPUT);
            com.pi4j.wiringpi.Gpio.digitalWrite(18,0);

            com.pi4j.wiringpi.Gpio.pinMode(18, Gpio.INPUT);

            // reconfigure the pin back to an input pin
            //pin.setMode(PinMode.DIGITAL_INPUT);

              */


            //long t1=Calendar.getInstance().getTimeInMillis();

            //com.pi4j.wiringpi.Gpio.waitForInterrupt(6,40000);
            //System.out.println("value:"+Gpio.digitalRead(com.pi4j.wiringpi.Gpio.wpiPinToGpio(18)));

            int counter=0;

            while(Gpio.digitalRead(com.pi4j.wiringpi.Gpio.wpiPinToGpio(port))==0){
                //System.out.println(GpioFactory.getDefaultProvider().getState(RaspiPin.GPIO_18));
                ++counter;
            }


            //long t2=Calendar.getInstance().getTimeInMillis();

            System.out.println("***"+counter);


        }

    }

    public static void main(String[] args) throws InterruptedException {

        checklight c=new checklight();

        c.run();

    }

}
