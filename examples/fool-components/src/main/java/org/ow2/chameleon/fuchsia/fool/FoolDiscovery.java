package org.ow2.chameleon.fuchsia.fool;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Example Fool Components
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

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

/**
 * Because it's a fool factory it just create and destroy ImportDeclaration using threads
 * and pre-created ImportDeclarations.
 * It don't really get what it does, but it's ok.
 */
@Component(name = "Fuchsia-FoolDiscovery-Factory")
@Provides(specifications = DiscoveryService.class)
@Instantiate(name = "Fuchsia-FoolDiscovery")
public class FoolDiscovery extends AbstractDiscoveryComponent {

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    private final ScheduledThreadPoolExecutor pool_register = new ScheduledThreadPoolExecutor(1);
    private final ScheduledThreadPoolExecutor pool_unregister = new ScheduledThreadPoolExecutor(1);

    private final List<ImportDeclaration> importDeclarations = new ArrayList<ImportDeclaration>();
    private Integer index_importDeclaration_register;
    private Integer index_importDeclaration_unregister;

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FoolDiscovery.class);

    public FoolDiscovery(BundleContext bundleContext) {
        super(bundleContext);
        LOG.debug("Creating fool discovery !");
    }

    @Validate
    public void start() {
        super.start();
        LOG.debug("Start fool discovery !");
        for (int i = 0; i < 2; i++) {
            Map<String, Object> metadata = new HashMap<String, Object>();
            metadata.put("fool", "fool");
            metadata.put("fool-number", i);
            importDeclarations.add(ImportDeclarationBuilder.fromMetadata(metadata).build());
        }
        index_importDeclaration_register = -1;
        index_importDeclaration_unregister = -1;

        pool_register.execute(new Registrator());
        pool_unregister.execute(new Unregistrator());

    }

    @Invalidate
    public void stop() {
        super.stop();
        pool_unregister.shutdown();
        pool_register.shutdown();

        importDeclarations.clear();
    }

    public String getName() {
        return name;
    }


    protected class Registrator implements Runnable {

        final Random random = new Random();

        public void run() {
            while (index_importDeclaration_register < importDeclarations.size()) {
                try {
                    Thread.sleep((random.nextInt(5) + 1) * 3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LOG.debug("Registrator : " + index_importDeclaration_register);
                registerImportDeclaration(importDeclarations.get(index_importDeclaration_register + 1));
                index_importDeclaration_register = index_importDeclaration_register + 1;
            }

        }
    }

    protected class Unregistrator implements Runnable {

        final Random random = new Random();

        public void run() {
            while (index_importDeclaration_unregister < importDeclarations.size()) {
                while (index_importDeclaration_unregister.equals(index_importDeclaration_register)) {
                    try {
                        Thread.sleep((random.nextInt(5) + 1) * 2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                LOG.debug("Unregistrator : " + index_importDeclaration_unregister);

                unregisterImportDeclaration(importDeclarations.get(index_importDeclaration_unregister + 1));
                index_importDeclaration_unregister = index_importDeclaration_unregister + 1;
            }

        }
    }
}
