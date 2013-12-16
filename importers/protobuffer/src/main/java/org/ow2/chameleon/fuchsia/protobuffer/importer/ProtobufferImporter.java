package org.ow2.chameleon.fuchsia.protobuffer.importer;

import com.google.code.cxf.protobuf.client.SimpleRpcChannel;
import com.google.protobuf.Message;
import com.google.protobuf.RpcChannel;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

@Component(name = "ProtobufferImporterFactory")
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class ProtobufferImporter extends AbstractImporterComponent {

    private BundleContext context;

    private Logger log=LoggerFactory.getLogger(this.getClass().getName());

    @ServiceProperty(name = "instance.name")
    private String name;
    @ServiceProperty(name = "target", value = "(id=*)")
    private String filter;

    public ProtobufferImporter(BundleContext context){
        this.context=context;
    }

    @Validate
    public void validate(){
        log.info("Protobuffer Importer RPC is up and running");
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {

        String id=importDeclaration.getMetadata().get("id").toString();
        String serverHostname=importDeclaration.getMetadata().get("rpc.server.address").toString();
        String protoclass=importDeclaration.getMetadata().get("rpc.proto.class").toString();
        String protoservice=importDeclaration.getMetadata().get("rpc.proto.service").toString();
        String protomessage=importDeclaration.getMetadata().get("rpc.proto.message").toString();

        log.info("Importing declaration with ID {}", id);

        try {

            Class<?> bufferService=FuchsiaUtils.loadClass(context,String.format("%s$%s",protoclass,protoservice));

            Class<?> bufferMessage = FuchsiaUtils.loadClass(context, String.format("%s$%s", protoclass, protomessage));

            Class<? extends Message> generic=bufferMessage.asSubclass(Message.class);

            RpcChannel channel=new SimpleRpcChannel(serverHostname,generic);

            Method method=bufferService.getMethod("newStub",RpcChannel.class);

            Object service=method.invoke(bufferService, channel);

            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>();

            ServiceRegistration registrationTicket=context.registerService(bufferService.getName(),service,serviceProperties);

        } catch (Exception e) {
            log.error("Fail to import Protobuffer RPC service with the message '{}'",e.getMessage());
        }

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {

       // Don't care mama!

    }

    public List<String> getConfigPrefix() {
        return null;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}

