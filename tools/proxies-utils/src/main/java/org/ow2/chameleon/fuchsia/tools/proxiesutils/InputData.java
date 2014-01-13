package org.ow2.chameleon.fuchsia.tools.proxiesutils;

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
        private Map<String, Object> actionsValues;

        public DeviceOutputBuilder(String actionName) {
            this.actionName = actionName;
            actionsValues = new HashMap<String, Object>();
        }

        public DeviceOutputBuilder actionValue(String name, Object value) {
            actionsValues.put(name, value);
            return this;
        }

        public InputData create() {
            return new InputData(actionName, actionsValues);
        }
    }


}
