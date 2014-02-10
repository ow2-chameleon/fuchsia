package org.ow2.chameleon.fuchsia.tools.shell;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.fuchsia.core.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


@Component(immediate = true)
@Instantiate
@Provides(specifications = FuchsiaGogoCommand.class)
/**
 * {@link FuchsiaGogoCommand} is basic shell command set
 * Gogo {@link http://felix.apache.org/site/apache-felix-gogo.html} is used as base for this command
 *
 * @author jander nascimento (botelho at imag.fr)
 */
public class FuchsiaGogoCommand {

    @ServiceProperty(name = "osgi.command.scope", value = "fuchsia")
    String m_scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[]{"declaration", "linker", "discovery","importer","exporter","sendmessage"};

    @Requires
    EventAdmin eventAdmin;

    private Logger log= LoggerFactory.getLogger(this.getClass());

    private BundleContext m_context = null;

    public FuchsiaGogoCommand(BundleContext context) {
        this.m_context = context;
    }

    @Descriptor("Gets info about the importation declaration available")
    public void declaration(@Descriptor("declaration [--type import|export]") String... parameters) {

        String filter = null;
        String type=getArgumentValue("--type",parameters);

        try {

            if(type==null||type.equals("import")){

                ServiceReference[] importDeclarationsRef = m_context.getAllServiceReferences(ImportDeclaration.class.getName(), filter);

                System.out.println("--- Import declaration ---");

                if (importDeclarationsRef!=null) {

                    List<ServiceReference> references = Arrays.asList(importDeclarationsRef);

                    for (ServiceReference reference : references) {

                        ImportDeclaration declaration=(ImportDeclaration) m_context.getService(reference);

                        displayDeclarationMetadata(reference, declaration.getMetadata());

                    }
                } else {
                    System.out.println("\tNo declarations available.");
                }


            }

            if(type==null||type.equals("export")){

                ServiceReference[] exportDeclarationsRef = m_context.getAllServiceReferences(ExportDeclaration.class.getName(), filter);

                System.out.println("--- Export declaration ---");

                if (exportDeclarationsRef!=null) {

                    List<ServiceReference> references=Arrays.asList(exportDeclarationsRef);

                    for (ServiceReference reference : references) {

                        ExportDeclaration declaration= (ExportDeclaration) m_context.getService(reference);

                        displayDeclarationMetadata(reference, declaration.getMetadata());

                    }
                } else {
                    System.out.println("\tNo declarations available.");
                }

            }

        } catch (Exception e) {
            log.error("failed to execute command",e);
            System.out.println("failed to execute the command");
        }

    }

    @Descriptor("Gets the importation/exportation linker available")
    public void linker(@Descriptor("linker [-(import|export)] [ID name]") String... parameters) {

        String filter = null;

        try {
            ServiceReference[] exportationLinkerRef = m_context.getAllServiceReferences(ExportationLinker.class.getName(), filter);
            ServiceReference[] importationLinkerRef = m_context.getAllServiceReferences(ImportationLinker.class.getName(), filter);

            if (exportationLinkerRef!=null || importationLinkerRef!=null) {

                if(exportationLinkerRef!=null)
                    for (ServiceReference reference : exportationLinkerRef) {

                        displayServiceInfo("Exportation Linker", reference);

                        displayServiceProperties(reference);

                    }

                if(importationLinkerRef!=null)
                    for (ServiceReference reference : importationLinkerRef) {

                        displayServiceInfo("Importation Linker", reference);

                        displayServiceProperties(reference);

                    }

            } else {
                System.out.println("No linkers available.");
            }

        } catch (InvalidSyntaxException e) {
            log.error("invalid LDAP filter syntax",e);
            System.out.println("failed to execute the command");
        }

    }

    @Descriptor("Gets the discovery available in the platform")
    public void discovery(@Descriptor("discovery [discovery name]") String... parameters) {

        String filter = null;

        try {
            ServiceReference[] discoveryRef = m_context.getAllServiceReferences(DiscoveryService.class.getName(), filter);

            if (discoveryRef != null) {
                for (ServiceReference reference : discoveryRef) {

                    displayServiceInfo("Discovery", reference);

                    displayServiceProperties(reference);

                }
            } else {
                System.out.println("No discovery available.");
            }

        } catch (InvalidSyntaxException e) {
            log.error("invalid ldap filter syntax",e);
            System.out.println("failed to execute the command");
        }

    }

    @Descriptor("Gets the importer available in the platform")
    public void importer(@Descriptor("importer [importer name]") String... parameters) {

        String filter = null;

        try {
            ServiceReference[] discoveryRef = m_context.getAllServiceReferences(ImporterService.class.getName(), filter);

            if (discoveryRef != null) {
                for (ServiceReference reference : discoveryRef) {

                    displayServiceInfo("Importer", reference);

                    ImporterService is=(ImporterService)m_context.getService(reference);

                    System.out.println(String.format("\t*importer name = %s", is.getName()));

                    displayServiceProperties(reference);

                }
            } else {
                System.out.println("No importers available.");
            }

        } catch (InvalidSyntaxException e) {
            log.error("invalid ldap filter syntax",e);
            System.out.println("failed to execute the command");
        }

    }

    @Descriptor("Gets the exporter available in the platform")
    public void exporter(@Descriptor("exporter [exporter name]") String... parameters) {

        String filter = null;

        try {
            ServiceReference[] discoveryRef = m_context.getAllServiceReferences(ExporterService.class.getName(), filter);

            if (discoveryRef != null) {
                for (ServiceReference reference : discoveryRef) {

                    displayServiceInfo("Exporter", reference);

                    ExporterService es=(ExporterService)m_context.getService(reference);

                    System.out.println(String.format("\t*exporter name = %s", es.getName()));

                    displayServiceProperties(reference);

                }
            } else {
                System.out.println("No exporter available.");
            }

        } catch (InvalidSyntaxException e) {
            log.error("invalid ldap filter syntax",e);
            System.out.println("failed to execute the command");
        }

    }

    @Descriptor("Send event admin messages")
    public void sendmessage(@Descriptor("sendmessage BUS [KEY=VALUE ]*") String... parameters){

        assert parameters[0]!=null;

        String bus=parameters[0];

        Dictionary eventAdminPayload=new Hashtable();

        for(String m:parameters){
            if(m.contains("=")){
                StringTokenizer st = new StringTokenizer(m,"=");

                assert st.countTokens()==2;

                String key=st.nextToken();
                String value=st.nextToken();

                eventAdminPayload.put(key,value);

            }
        }

        Event eventAdminMessage = new Event(bus,eventAdminPayload);

        System.out.println(String.format("Sending message to the bus %s with the arguments %s", bus, eventAdminPayload));

        System.out.println("Event admin message sent");

        eventAdmin.sendEvent(eventAdminMessage);

    }

    private void displayDeclarationMetadata(ServiceReference reference, Map<String, Object> metadata){

        displayServiceProperties(reference);

        System.out.println("\tMetadata");

        for(Map.Entry<String,Object> entry:metadata.entrySet()){
            System.out.println(String.format("\t\t%s=%s",entry.getKey(),entry.getValue()));
        }

        if(metadata.entrySet().size()==0){
            System.out.println("\tEMPTY");
        }

    }

    private void displayServiceProperties(ServiceReference reference){
        System.out.println("\tService properties:");
        for (String propertyKey : reference.getPropertyKeys()) {
            System.out.println(String.format("\t\t%s = %s", propertyKey, reference.getProperty(propertyKey)));
        }

        if (reference.getPropertyKeys().length==0){
            System.out.println("\t\t EMPTY");
        }
    }

    private void displayServiceInfo(String prolog,ServiceReference reference){

        System.out.println(String.format("%s [%s] provided by bundle %s (%s)", prolog,reference.getProperty(Factory.INSTANCE_NAME_PROPERTY), reference.getBundle().getSymbolicName(), reference.getBundle().getBundleId()));

    }

    private String getArgumentValue(String option, String... params) {

        boolean found = false;
        String value = null;

        for (int i = 0; i < params.length; i++) {
            if (i < (params.length - 1) && params[i].equals(option)) {
                found = true;
                value = params[i + 1];
                break;
            }
        }

        if (found)
            return value;

        return null;
    }

}


