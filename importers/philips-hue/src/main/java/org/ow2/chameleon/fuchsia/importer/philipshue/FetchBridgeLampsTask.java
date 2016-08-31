/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
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
package org.ow2.chameleon.fuchsia.importer.philipshue;

import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.importer.philipshue.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FetchBridgeLampsTask extends TimerTask {

    private final Logger LOG = LoggerFactory.getLogger(FetchBridgeLampsTask.class);

    private final PHBridge bridge;

    private final BundleContext context;

    private final Map<String,ServiceRegistration> lamps;

    public FetchBridgeLampsTask(PHBridge bridge,final Map<String,ServiceRegistration> lamps,BundleContext context){
        this.bridge=bridge;
        this.lamps=lamps;
        this.context=context;
    }

    @Override
    public void run() {

        for(PHLight light:bridge.getResourceCache().getAllLights()){

            if( (light.getLastKnownLightState() != null) && (!light.getLastKnownLightState() .isReachable())){

                ServiceRegistration sr=lamps.remove(light.getIdentifier());
                if(sr!=null)
                    sr.unregister();

            }else if(!lamps.keySet().contains(light.getIdentifier())){

                Map<String, Object> metadata = new HashMap<String, Object>();

                metadata.put(org.ow2.chameleon.fuchsia.core.declaration.Constants.ID, light.getIdentifier());
                metadata.put(Constants.DISCOVERY_PHILIPS_DEVICE_NAME, light.getModelNumber());
                metadata.put(Constants.DISCOVERY_PHILIPS_DEVICE_TYPE, light.getClass().getName());
                metadata.put(Constants.DISCOVERY_PHILIPS_DEVICE_OBJECT, light);
                metadata.put(Constants.DISCOVERY_PHILIPS_DEVICE_BRIDGE, bridge);

                Dictionary metatableService=new Hashtable(metadata);

                ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

                ServiceRegistration sr=context.registerService(ImportDeclaration.class,declaration,metatableService);

                if(lamps.containsKey(light.getIdentifier())){
                    LOG.warn("Lamp with identifier {} already exists",light.getIdentifier());
                }

                lamps.put(light.getIdentifier(),sr);

            }
        }
    }
}
