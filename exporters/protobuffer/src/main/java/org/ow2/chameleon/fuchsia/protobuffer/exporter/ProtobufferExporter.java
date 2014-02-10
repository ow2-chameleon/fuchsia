package org.ow2.chameleon.fuchsia.protobuffer.exporter;

import com.google.code.cxf.protobuf.ProtobufServerFactoryBean;
import com.google.code.cxf.protobuf.binding.ProtobufBindingFactory;
import com.google.protobuf.Service;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.felix.ipojo.annotations.*;
import org.eclipse.jetty.util.component.Container;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractExporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Component(name = "ProtobufferExporterFactory")
@Provides(specifications = {ExporterService.class})
public class ProtobufferExporter extends AbstractExporterComponent {

    private Bus cxfbus;

    private static final Logger log= LoggerFactory.getLogger(ProtobufferExporter.class);

    @Requires
    HttpService http;

    @ServiceProperty(name = "target")
    private String filter;

    BundleContext context;

    public ProtobufferExporter(BundleContext context) {
        this.context = context;
    }

    @Override
    protected void useExportDeclaration(ExportDeclaration exportDeclaration) {

        log.info("initiating exportation...");

        String id = exportDeclaration.getMetadata().get("id").toString();
        String server = exportDeclaration.getMetadata().get("rpc.export.address").toString();
        String exporterClass = exportDeclaration.getMetadata().get("rpc.export.class").toString();
        String exporterMessage = exportDeclaration.getMetadata().get("rpc.export.message").toString();

        try {

            Class inter = FuchsiaUtils.loadClass(context, exporterClass);
            Class messageClass = FuchsiaUtils.loadClass(context, exporterMessage);

            Class<? extends Service> interpar = inter.asSubclass(Service.class);

            Collection<ServiceReference<Service>> protobuffReferences = context.getServiceReferences(inter, null);

            if (protobuffReferences.size() == 0) {

                log.info("nothing to be exported was found");

            } else if (protobuffReferences.size() == 1) {

                for (ServiceReference<Service> sr : protobuffReferences) {

                    Service protobufferService = context.getService(sr);

                    Bus cxfbus = BusFactory.getThreadDefaultBus();
                    BindingFactoryManager mgr = cxfbus.getExtension(BindingFactoryManager.class);
                    mgr.registerBindingFactory(ProtobufBindingFactory.PROTOBUF_BINDING_ID, new ProtobufBindingFactory(cxfbus));

                    ProtobufServerFactoryBean serverFactoryBean = new ProtobufServerFactoryBean();
                    //serverFactoryBean.setAddress("http://localhost:8889/AddressBookService");

                    //serverFactoryBean.setBus(cxfbus);

                    serverFactoryBean.setAddress(server);

                    //serverFactoryBean.setAddress("http://localhost:8889/cxf/AddressBookService/");

                    //serverFactoryBean.setServiceBean(new AddressBookProtos.AddressBookServiceImpl());
                    serverFactoryBean.setServiceBean(inter.cast(protobufferService));

                    serverFactoryBean.setMessageClass(messageClass);

                    ClassLoader loader = Thread.currentThread().getContextClassLoader();

                    Thread.currentThread().setContextClassLoader(Container.class.getClassLoader());

                    serverFactoryBean.create();

                    Thread.currentThread().setContextClassLoader(loader);

                    log.info("exporting the service with the id:" + id);


                }

            } else if (protobuffReferences.size() > 1) {
                log.info("more than one were found to be exported");
            }

        } catch (InvalidSyntaxException e) {
            log.error("Invalid filter exception",e);
        } catch (ClassNotFoundException e) {
            log.error("Class not found",e);
        }

    }

    @Validate
    public void start() {

        System.setProperty("org.apache.cxf.nofastinfoset", "true");

    }

    @Override
    protected void denyExportDeclaration(ExportDeclaration exportDeclaration) {
        // Don't care papa
    }

    public String getName() {
        return this.getClass().getName();
    }
}
