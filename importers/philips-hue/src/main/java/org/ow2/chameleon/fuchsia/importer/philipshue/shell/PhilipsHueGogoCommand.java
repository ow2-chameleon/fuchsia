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
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.ow2.chameleon.fuchsia.tools.shell.util.FuchsiaGogoUtil;

import java.util.Hashtable;
import java.util.List;

import static org.ow2.chameleon.fuchsia.tools.shell.util.FuchsiaGogoUtil.getArgumentValue;

@Component(immediate = true)
@Instantiate
@Provides
public class PhilipsHueGogoCommand {

    @ServiceProperty(name = "osgi.command.scope", value = "philips")
    private String scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    private String[] function = new String[]{"lamp"};

    @Requires(optional = true)
    PHBridge bridge;

    public void print(String message) {
        System.out.println(message);
    }

    private String reproduceChar(char character, int amount) {

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < amount; x++) {
            sb.append(character);
        }

        return sb.toString();
    }

    @Descriptor(value = "List all lights available, or change its state")
    public void lamp(@Descriptor("set [-name NAME] [-on true|false] [-i 0<=INTENSITY<=255] [-tt TRANSITION_TIME_IN_DECISECONDS] [-r 0<=RED<=255] [-g 0<=GREEN<=255] [-b 0<=BLUE<=255]") String... parameters) {

        String setValue = getArgumentValue("set", parameters);

        if(bridge==null||bridge.getResourceCache()==null){
            StringBuilder deviceStr=new StringBuilder();
            deviceStr.append("No bridge present.");
            print(FuchsiaGogoUtil.createASCIIBox("", deviceStr).toString());
            return;
        }

        try{
            if(setValue!=null){
                setLampState(parameters);
            }else {
                listLamps(parameters);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void listLamps(String... parameters){
        StringBuilder sb=new StringBuilder();
        for (PHLight light : bridge.getResourceCache().getAllLights()) {

            StringBuilder deviceStr=new StringBuilder();
            deviceStr.append(String.format("id = %s\n",light.getIdentifier()));
            deviceStr.append(String.format("name = %s\n",light.getName()));
            deviceStr.append(String.format("model = %s\n",light.getModelNumber()));
            deviceStr.append(String.format("type = %s\n",light.getLightType()));
            deviceStr.append(String.format("state = %s",light.getLastKnownLightState().isOn() ? "ON" : "OFF"));

            sb.append(FuchsiaGogoUtil.createASCIIBox(light.getIdentifier(), deviceStr));

        }
        print(sb.toString());
    }

    private void setLampState(String... parameters) {

        String valueStr = getArgumentValue("-on", parameters);
        String nameStr = getArgumentValue("-name", parameters);
        String ttStr = getArgumentValue("-tt", parameters);
        String iStr = getArgumentValue("-i", parameters);
        String rStr = getArgumentValue("-r", parameters);
        String gStr = getArgumentValue("-g", parameters);
        String bStr = getArgumentValue("-b", parameters);

        Boolean value = Boolean.valueOf(valueStr);

        for (PHLight light : bridge.getResourceCache().getAllLights()) {
            if (nameStr == null || nameStr.equals(light.getName())) {
                PHLightState lightState = new PHLightState();

                if (value != null) {
                    lightState.setOn(value);
                }

                if (ttStr != null) {
                    print("transition time:" + ttStr);
                    lightState.setTransitionTime(Integer.parseInt(ttStr));
                }

                if (iStr != null) {
                    print("brightness:" + iStr);
                    lightState.setBrightness(Integer.parseInt(iStr));
                }

                if (rStr != null || gStr != null || bStr != null) {

                    int r = getColorValue(rStr);
                    int g = getColorValue(gStr);
                    int b = getColorValue(bStr);

                    print(String.format("color red:%s green:%s blue:%s", r, g, b));

                    float[] xy = PHUtilities.calculateXYFromRGB(r, g, b, light.getIdentifier());

                    lightState.setX(xy[0]);
                    lightState.setY(xy[1]);

                }
                bridge.updateLightState(light, lightState);
            }
        }
    }

    private int getColorValue(String value) {
        return (value != null) ? Integer.parseInt(value) : 0;
    }
}
