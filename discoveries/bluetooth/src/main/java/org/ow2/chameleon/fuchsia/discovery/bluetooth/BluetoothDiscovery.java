/*
 * Copyright 2013 OW2 Chameleon
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ow2.chameleon.fuchsia.discovery.bluetooth;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Discovery Bluetooth
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

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.bluetooth.BluetoothController;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.bluetooth.RemoteDevice;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;
import static org.ow2.chameleon.fuchsia.discovery.bluetooth.BluetoothConstants.BLUETOOTH_DEVICE_ADDRESS;
import static org.ow2.chameleon.fuchsia.discovery.bluetooth.BluetoothConstants.BLUETOOTH_DEVICE_FRIENDLYNAME;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.PROTOCOL_NAME;

/**
 * @author Morgan Martinet
 */
@Component
@Provides(specifications = DiscoveryService.class)
public class BluetoothDiscovery extends AbstractDiscoveryComponent {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BluetoothDiscovery.class);

    @ServiceProperty(name = "instance.name")
    private String name;

    @Requires
    private BluetoothController bluetoothController;

    private final Map<String, ImportDeclaration> bluetoothDevices;

    public BluetoothDiscovery(BundleContext context) {
        super(context);
        bluetoothDevices = new HashMap<String, ImportDeclaration>();
    }

    @Bind(aggregate = true, optional = true)
    public void bindRemoteNamedDevice(RemoteDevice device) {
        ImportDeclaration iDec = null;
        String ba = null;
        String name = null;
        try {
            ba = device.getBluetoothAddress();
            name = device.getFriendlyName(false);
            LOG.debug("Building declaration for device " + ba);

            iDec = ImportDeclarationBuilder.empty()
                    .key(ID).value(ba)
                    .key(PROTOCOL_NAME).value("bluetooth")
                    .key(BLUETOOTH_DEVICE_ADDRESS).value(ba)
                    .key(BLUETOOTH_DEVICE_FRIENDLYNAME).value(name)
                    // FIXME scope metadata
                    .key("scope").value("generic")
                    .build();

        } catch (IOException e) {
            LOG.error("Can't get description from the device, maybe is already gone.");
            return;
        }

        LOG.debug("Add declaration for the device " + ba + "(" + name + ")");

        registerImportDeclaration(iDec);
        bluetoothDevices.put(ba, iDec);
    }

    @Unbind
    public void unbindRemoteNamedDevice(RemoteDevice device) {
        String ba = device.getBluetoothAddress();
        LOG.debug("Remove declaration for the device " + ba);

        unregisterImportDeclaration(bluetoothDevices.remove(ba));
    }


    /**
     *
     */
    @Validate
    public void start() {
        LOG.debug("Starting Bluetooth Discovery...");

        if (bluetoothDevices.size() > 0) {
            // FIXME : complete message
            throw new IllegalStateException();
        }
    }

    /**
     *
     */
    @Invalidate
    public void stop() {
        LOG.debug("Stopping Bluetooth Discovery...");
        for (ImportDeclaration iDec : bluetoothDevices.values()) {
            unregisterImportDeclaration(iDec);
        }
        bluetoothDevices.clear();
    }


    public String getName() {
        return name;
    }
}
