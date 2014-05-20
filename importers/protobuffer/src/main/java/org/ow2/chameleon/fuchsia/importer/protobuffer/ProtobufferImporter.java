package org.ow2.chameleon.fuchsia.importer.protobuffer;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer Protobuffer
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

import com.google.code.cxf.protobuf.binding.ProtobufBindingFactory;
import com.google.code.cxf.protobuf.client.SimpleRpcChannel;
import com.google.protobuf.Message;
import com.google.protobuf.RpcChannel;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.protobuffer.internal.ProtobufferImportDeclarationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class ProtobufferImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(ProtobufferImporter.class);

    private final BundleContext context;

    private Map<String, ServiceRegistration> registeredImporter = new HashMap<String, ServiceRegistration>();

    @ServiceProperty(name = "instance.name")
    private String name;

    @ServiceProperty(name = "target", value = "(&(rpc.server.address=*)(rpc.proto.class=*)(rpc.proto.service=*)(rpc.proto.message=*)(scope=generic))")
    private String filter;

    public ProtobufferImporter(BundleContext context) {
        this.context = context;
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Validate
    public void start() {
        super.start();
        LOG.info("Protobuffer Importer RPC is up and running");
    }

    @Invalidate
    public void stop() {
        super.stop();

        for (Map.Entry<String, ServiceRegistration> item : registeredImporter.entrySet()) {
            item.getValue().unregister();
            registeredImporter.remove(item.getKey());
        }

        LOG.info("Protobuffer Importer RPC was stopped");
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        ProtobufferImportDeclarationWrapper pojo = ProtobufferImportDeclarationWrapper.create(importDeclaration);

        LOG.info("Importing declaration with ID {}", pojo.getId());

        try {

            Bus cxfbus = BusFactory.getThreadDefaultBus();
            BindingFactoryManager mgr = cxfbus.getExtension(BindingFactoryManager.class);
            mgr.registerBindingFactory(ProtobufBindingFactory.PROTOBUF_BINDING_ID, new ProtobufBindingFactory(cxfbus));

            Class<?> bufferService = FuchsiaUtils.loadClass(context, String.format("%s$%s", pojo.getClazz(), pojo.getService()));

            Class<?> bufferMessage = FuchsiaUtils.loadClass(context, String.format("%s$%s", pojo.getClazz(), pojo.getMessage()));

            Class<? extends Message> generic = bufferMessage.asSubclass(Message.class);

            RpcChannel channel = new SimpleRpcChannel(pojo.getAddress(), generic);

            Method method = bufferService.getMethod("newStub", RpcChannel.class);

            Object service = method.invoke(bufferService, channel);

            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>();
            serviceProperties.put("fuchsia.importer.id", pojo.getId());

            ServiceRegistration sr = context.registerService(bufferService.getName(), service, serviceProperties);

            registeredImporter.put(pojo.getId(), sr);

            super.handleImportDeclaration(importDeclaration);

        } catch (ClassNotFoundException e) {
            LOG.error("Fail to import Protobuffer RPC service with the message", e);
        } catch (EndpointException e) {
            LOG.error("Fail to import Protobuffer RPC service with the message", e);
        } catch (InvocationTargetException e) {
            LOG.error("Fail to import Protobuffer RPC service with the message", e);
        } catch (IllegalAccessException e) {
            LOG.error("Fail to import Protobuffer RPC service with the message", e);
        } catch (NoSuchMethodException e) {
            LOG.error("Fail to import Protobuffer RPC service with the message", e);
        }

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        unhandleImportDeclaration(importDeclaration);

        ProtobufferImportDeclarationWrapper pojo = ProtobufferImportDeclarationWrapper.create(importDeclaration);

        ServiceRegistration sr = registeredImporter.remove(pojo.getId());

        if (sr != null) {
            LOG.info("unregistering service with id:" + pojo.getId());
            sr.unregister();
        } else {
            LOG.warn("no service found to be unregistered with id:" + pojo.getId());
        }


    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}

