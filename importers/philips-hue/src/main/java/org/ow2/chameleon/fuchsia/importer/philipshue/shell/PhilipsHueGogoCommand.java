package org.ow2.chameleon.fuchsia.importer.philipshue.shell;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer Philips Hue
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;

import java.util.Set;

@Component(immediate = true)
@Instantiate
@Provides
public class PhilipsHueGogoCommand {

    @ServiceProperty(name = "osgi.command.scope", value = "philips")
    private String scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    private String[] function = new String[]{"phlist","phset"};

    @Requires(specification = "com.philips.lighting.model.PHLight")
    Set<PHLight> lights;

    @Requires
    PHBridge bridge;

    public PhilipsHueGogoCommand() {

    }

    @Descriptor  (value = "List all lights available")
    public void phlist(@Descriptor("phlist") String... parameters) {
        for(PHLight light:lights){
            System.out.println(light.toString());
            System.out.println("state:"+light.getLastKnownLightState().isOn());
        }
    }

    @Descriptor  (value = "Change light parameters, for a specific lamp or for all light plugged into the bridge")
    public void phset(@Descriptor("[-name NAME] [-on true|false] [-i 0<=INTENSITY<=255] [-tt TRANSITION_TIME_IN_DECISECONDS] [-r 0<=RED<=255] [-g 0<=GREEN<=255] [-b 0<=BLUE<=255]") String... parameters) {

        String valueStr = getArgumentValue("-on", parameters);
        String nameStr = getArgumentValue("-name", parameters);
        String ttStr = getArgumentValue("-tt", parameters);
        String iStr = getArgumentValue("-i", parameters);
        String rStr = getArgumentValue("-r", parameters);
        String gStr = getArgumentValue("-g", parameters);
        String bStr = getArgumentValue("-b", parameters);

        Boolean value = Boolean.valueOf(valueStr);

        for (PHLight light : lights) {
            if (nameStr == null || nameStr.equals(light.getName())) {
                PHLightState lightState = new PHLightState();

                if (value != null) {
                    lightState.setOn(value);
                }

                if (ttStr != null) {
                    System.out.println("transition time:" + ttStr);
                    lightState.setTransitionTime(Integer.valueOf(ttStr));
                }

                if (iStr != null) {
                    System.out.println("brightness:" + iStr);
                    lightState.setBrightness(Integer.valueOf(iStr));
                }

                if (rStr != null || gStr != null || bStr != null) {

                    int r = getColorValue(rStr);
                    int g = getColorValue(gStr);
                    int b = getColorValue(bStr);

                    System.out.println(String.format("color red:%s green:%s blue:%s", r, g, b));

                    float[] xy = PHUtilities.calculateXYFromRGB(r, g, b, light.getIdentifier());

                    lightState.setX(xy[0]);
                    lightState.setY(xy[1]);

                }
                bridge.updateLightState(light, lightState);
            }
        }
    }

    private int getColorValue(String value) {
        return value != null ? Integer.valueOf(value) : 0;
    }

    /**
     * Look up for the value of a parameter in a sequence of string.
     * @param option the option that we are looking for. e.g. '-k'
     * @param params the list of parameters
     * @return the value associated with the parameter or Null in case it is not present
     */
    private String getArgumentValue(String option, String... params) {
        for (int i = 0; i < params.length; i++) {
            if (i < (params.length - 1) && params[i].equals(option)) {
                return params[i + 1];
            }
        }

        return null;
    }

}
