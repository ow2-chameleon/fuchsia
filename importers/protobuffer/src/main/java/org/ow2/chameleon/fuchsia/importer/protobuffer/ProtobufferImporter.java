package org.ow2.chameleon.fuchsia.importer.protobuffer;

import com.google.code.cxf.protobuf.binding.ProtobufBindingFactory;
import com.google.code.cxf.protobuf.client.SimpleRpcChannel;
import com.google.protobuf.Message;
import com.google.protobuf.RpcChannel;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.protobuffer.internal.ProtobufferImporterPojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private ServiceReference serviceReference;

    private Map<String,ServiceRegistration> registeredImporter=new HashMap<String,ServiceRegistration>();

    @ServiceProperty(name = "instance.name")
    private String name;

    @ServiceProperty(name = "target", value = "(id=*)")
    private String filter;

    public ProtobufferImporter(BundleContext context) {
        this.context = context;
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }

    @Validate
    public void start() {
        super.start();
        LOG.info("Protobuffer Importer RPC is up and running");
    }

    @Invalidate
    public void stop() {
        super.stop();

        for(Map.Entry<String,ServiceRegistration> item:registeredImporter.entrySet()){
            item.getValue().unregister();
            registeredImporter.remove(item.getKey());
        }

        LOG.info("Protobuffer Importer RPC was stopped");
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        ProtobufferImporterPojo pojo=ProtobufferImporterPojo.create(importDeclaration);

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

            ServiceRegistration sr=context.registerService(bufferService.getName(), service, serviceProperties);

            registeredImporter.put(pojo.getId(),sr);

            importDeclaration.handle(serviceReference);

        } catch (Exception e) {
            LOG.error("Fail to import Protobuffer RPC service with the message '{}'", e.getMessage(), e);
        }

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        importDeclaration.unhandle(serviceReference);

        ProtobufferImporterPojo pojo=ProtobufferImporterPojo.create(importDeclaration);

        ServiceRegistration sr=registeredImporter.get(pojo.getId());

        if(sr!=null){
            LOG.info("unregistering service with id:" + pojo.getId());
            sr.unregister();
        }else {
            LOG.warn("no service found to be unregistered with id:" + pojo.getId());
        }


    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}

