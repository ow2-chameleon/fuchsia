package org.ow2.chameleon.fuchsia.philips.hue.examples;

import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import org.apache.felix.ipojo.annotations.*;

import java.util.List;
import java.util.Set;

@Component
//@Instantiate
public class PhilipsHueExampleBlink {

    @Requires(specification = "com.philips.lighting.model.PHLight")
    Set<PHLight> lights;

    @Requires
    PHBridge bridge;

    boolean active=false;

    @Validate
    public void start(){

        active=true;


        Thread t1=new Thread(){

            public void run(){
                boolean state=false;
                while(active){
                    System.out.println("philips client just started, lights :" +lights.size());

                    PHBridgeResourcesCache cache = bridge.getResourceCache();

                    for(PHLight light:lights){

                        PHLightState lightState = new PHLightState();

                        lightState.setOn(!light.getLastKnownLightState().isOn());

                        bridge.updateLightState(light, lightState);
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        //Dont care
                    }
                }

            }

        };
        t1.setDaemon(true);
        t1.start();


    }

    @Invalidate
    public void stop(){
        active=false;
    }

}
