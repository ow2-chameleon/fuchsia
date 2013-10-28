package org.ow2.chameleon.fuchsia.tools.shell;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.handlers.dependency.DependencyDescription;
import org.apache.felix.ipojo.handlers.dependency.DependencyHandlerDescription;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Collection;
import java.util.Map;

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
    String[] m_function = new String[]{"declaration", "linker", "discovery","importer"};

    private BundleContext m_context = null;

    public FuchsiaGogoCommand(BundleContext context) {
        this.m_context = context;
    }

    @Descriptor("Gets info about the importation declaration available")
    public void declaration(@Descriptor("declaration [ID name]") String... parameters) {

        String filter = null;

        try {
            ServiceReference[] importDeclarationsRef = m_context.getAllServiceReferences(ImportDeclaration.class.getName(), filter);

            if (importDeclarationsRef != null) {
                for (ServiceReference reference : importDeclarationsRef) {

                    displayServiceInfo("Declaration",reference);

                    displayServiceProperties(reference);

                    System.out.println("Metadata");

                    ImportDeclaration declaration= (ImportDeclaration) m_context.getService(reference);


                    Map<String, Object> metadata = declaration.getMetadata();

                    for(Map.Entry<String,Object> entry:metadata.entrySet()){
                        System.out.println(String.format("\t%s=%s",entry.getKey(),entry.getValue()));
                    }

                    if(metadata.entrySet().size()==0){
                        System.out.println("\tEMPTY");
                    }

                }
            } else {
                System.out.println("No declarations available.");
            }

        } catch (InvalidSyntaxException e) {
            System.out.println("failed to execute the command with the message: " + e.getMessage());
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
            System.out.println("failed to execute the command with the message: " + e.getMessage());
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
            System.out.println("failed to execute the command with the message: " + e.getMessage());
        }

    }

    @Descriptor("Gets the discovery available in the platform")
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
            System.out.println("failed to execute the command with the message: " + e.getMessage());
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


