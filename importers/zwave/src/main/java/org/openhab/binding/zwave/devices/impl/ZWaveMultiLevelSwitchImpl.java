/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2015 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.openhab.binding.zwave.devices.impl;

import org.apache.felix.ipojo.annotations.*;
import org.openhab.binding.zwave.ZWaveImporter;
import org.openhab.binding.zwave.devices.ZWaveMultiLevelSwitch;
import org.openhab.binding.zwave.internal.commandclass.ZWaveMultiLevelSwitchCommandClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;

/**
 * Provides an abstraction for accessing ZWave Switch device
 * @Author Jander Botelho do Nascimento (botelho@imag.fr)
 */
@Component
@Provides(properties = { @StaticServiceProperty(name = "type", type="java.lang.String", value = "multilevelswitch",immutable = true),
        @StaticServiceProperty(name = "protocol", type="java.lang.String", value = "zwave", immutable = true)})
public class ZWaveMultiLevelSwitchImpl implements ZWaveMultiLevelSwitch {

    //@Requires
    ZWaveController controller;

    @Property
    Integer node;

    @Property
    Integer endpoint;

    @Property
    String port;

    ZWaveMultiLevelSwitchCommandClass switchCommand;

    @Validate
    public void validate(){

        controller= ZWaveImporter.getControllers().get(this.port);

    }

    private void execute(){

        ZWaveEndpoint endpoint=new ZWaveEndpoint(this.endpoint);

        ZWaveNode node=controller.getNode(this.node);

        if(switchCommand==null){
            switchCommand=new ZWaveMultiLevelSwitchCommandClass(node,controller,endpoint);
        }
        controller.sendData(switchCommand.getValueMessage());
    }

    public void minimum() {
        switchCommand.setValueMessage(getMinimumValue());
        execute();
    }

    public void stepUp() {
        int newLevel=switchCommand.getLevel()+10;

        if(newLevel<=getMaximumValue())
            switchCommand.setValueMessage(newLevel);
        execute();
    }

    public Integer getMinimumValue() {
        return 0;
    }

    public Integer getMaximumValue() {
        return 99;
    }

    public void setValue(Integer value) {
        switchCommand.setValueMessage(value);
        execute();
    }

    public Integer getValue() {
        return switchCommand.getLevel();
    }

    public void stepDown() {
        int newLevel=switchCommand.getLevel()-10;

        if(newLevel>getMinimumValue())
            switchCommand.setValueMessage(newLevel);
        execute();
    }

    public void maximum() {
        switchCommand.setValueMessage(getMaximumValue());
        execute();
    }
}
