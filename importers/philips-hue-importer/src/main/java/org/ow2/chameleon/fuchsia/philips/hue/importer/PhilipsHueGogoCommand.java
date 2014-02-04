package org.ow2.chameleon.fuchsia.philips.hue.importer;

import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.BundleContext;

import java.util.Random;
import java.util.Set;

@Component(immediate = true)
@Instantiate
@Provides
public class PhilipsHueGogoCommand {

    @ServiceProperty(name = "osgi.command.scope", value = "philips")
    String m_scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[]{"phlist","phset"};

    @Requires(specification = "com.philips.lighting.model.PHLight")
    Set<PHLight> lights;

    private static final int MAX_HUE=65535;

    @Requires
    PHBridge bridge;

    private BundleContext m_context = null;

    public PhilipsHueGogoCommand(BundleContext context) {
        this.m_context = context;
    }

    @Descriptor  (value = "phlist")
    public void phlist(@Descriptor("phlist") String... parameters) {
        for(PHLight light:lights){
            System.out.println(light.toString());
            System.out.println("state:"+light.getLastKnownLightState().isOn());
        }
    }

    @Descriptor  (value = "phset")
    public void phset(@Descriptor("phset") String... parameters) {

        String valueStr=getArgumentValue("-value",parameters);
        String nameStr=getArgumentValue("-name",parameters);
        String ttStr=getArgumentValue("-tt",parameters);
        String iStr=getArgumentValue("-i",parameters);
        String rStr=getArgumentValue("-r",parameters);
        String gStr=getArgumentValue("-g",parameters);
        String bStr=getArgumentValue("-b",parameters);

        Boolean value=new Boolean(valueStr);

        for(PHLight light:lights){

            if(nameStr==null || nameStr.equals(light.getName())){

                PHLightState lightState = new PHLightState();

                if(value!=null){
                    lightState.setOn(value);
                }

                if(ttStr!=null){
                    System.out.println("transition time:"+ttStr);
                    lightState.setTransitionTime(new Integer(ttStr));
                }

                if(iStr!=null){
                    System.out.println("brightness:"+iStr);
                    lightState.setBrightness(new Integer(iStr));
                }

                if(rStr!=null||gStr!=null||bStr!=null){

                    int r=rStr!=null?new Integer(rStr):0;
                    int g=gStr!=null?new Integer(gStr):0;
                    int b=bStr!=null?new Integer(bStr):0;

                    System.out.println(String.format("color %s %s %s",r,g,b));

                    System.out.println("model:"+light.getModelNumber());

                    float[] xy= PHUtilities.calculateXYFromRGB(r, g, b, light.getIdentifier());

                    lightState.setHue(PHUtilities.colorFromXY(xy,light.getIdentifier()));
                    lightState.setColorMode(PHLight.PHLightColorMode.COLORMODE_HUE_SATURATION);

                    Random rand = new Random();
                    lightState.setHue(rand.nextInt(MAX_HUE));

                }


                bridge.updateLightState(light,lightState);

            }
        }
    }

    private String getArgumentValue(String option, String... params) {

        boolean found = false;
        String value = null;

        for (int i = 0; i < params.length; i++) {
            if (i < (params.length - 1) && params[i].equals(option)) {
                found = true;
                value = params[i + 1];
                break;
            }
        }

        if (found)
            return value;

        return null;
    }

}
