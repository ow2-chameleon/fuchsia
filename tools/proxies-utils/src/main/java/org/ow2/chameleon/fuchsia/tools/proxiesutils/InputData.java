package org.ow2.chameleon.fuchsia.tools.proxiesutils;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Proxies Utils
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InputData {

    private final String actionName;

    private final Map<String, Object> actionsValues;

    InputData(String actionName, Map<String, Object> actionsValues) {
        this.actionName = actionName;
        this.actionsValues = Collections.unmodifiableMap(actionsValues);
    }

    public String getActionName() {
        return actionName;
    }

    public Map<String, Object> getActionsValues() {
        return actionsValues;
    }

    @Override
    public String toString() {
        return "InputData{" +
                "actionName='" + actionName + '\'' +
                ", actionsValues=" + actionsValues +
                '}';
    }

    public static DeviceOutputBuilder build(String actionName) {
        return new DeviceOutputBuilder(actionName);
    }

    public static class DeviceOutputBuilder {
        private String actionName;
        private Map<String, Object> actionsValuesMap;

        public DeviceOutputBuilder(String actionName) {
            this.actionName = actionName;
            actionsValuesMap = new HashMap<String, Object>();
        }

        public DeviceOutputBuilder actionValue(String name, Object value) {
            actionsValuesMap.put(name, value);
            return this;
        }

        public DeviceOutputBuilder actionsValues(Map<String, Object> avs) {
            this.actionsValuesMap.putAll(avs);
            return this;
        }

        public InputData create() {
            return new InputData(actionName, actionsValuesMap);
        }
    }


}
