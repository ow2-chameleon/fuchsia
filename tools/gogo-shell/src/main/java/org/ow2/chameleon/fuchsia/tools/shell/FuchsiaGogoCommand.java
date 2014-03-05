package org.ow2.chameleon.fuchsia.tools.shell;

import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.fuchsia.core.component.*;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;


@Component(immediate = true)
@Instantiate
@Provides(specifications = FuchsiaGogoCommand.class)
@SuppressWarnings("PMD.SystemPrintln")
/**
 * {@link FuchsiaGogoCommand} is basic shell command set
 * Gogo {@link http://felix.apache.org/site/apache-felix-gogo.html} is used as base for this command
 *
 * @author jander nascimento (botelho at imag.fr)
 */
public class FuchsiaGogoCommand {

    private static final Logger LOG = LoggerFactory.getLogger(FuchsiaGogoCommand.class);

    private final BundleContext context;

    @ServiceProperty(name = "osgi.command.scope", value = "fuchsia")
    private String scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    private String[] function = new String[]{"declarations", "declaration", "linker", "discovery", "importer", "exporter", "sendmessage"};

    @Requires
    private EventAdmin eventAdmin;

    public FuchsiaGogoCommand(BundleContext context) {
        this.context = context;
    }

    // ---------------- DECLARATION

    @Descriptor("Gets info about the declarations available")
    public void declarations(@Descriptor("declarations [--type import|export]") String... parameters) {
        String type = getArgumentValue("--type", parameters);
        try {
            if ((type == null) || "import".equals(type)) {
                List<ServiceReference> allServiceRef = getAllServiceRefs(ImportDeclaration.class);
                displayDeclarationList(allServiceRef);
            }

            if ((type == null) || "export".equals(type)) {
                List<ServiceReference> allServiceRef = getAllServiceRefs(ExportDeclaration.class);
                displayDeclarationList(allServiceRef);
            }
        } catch (Exception e) {
            LOG.error("failed to execute command", e);
            System.out.println("failed to execute the command");
        }
    }

    private String getIdentifier(Declaration declaration) {
        String type = null;
        if (declaration instanceof ImportDeclaration) {
            type = "import";
        } else if (declaration instanceof ExportDeclaration) {
            type = "export";
        }
        return (String) declaration.getMetadata().get(ID);
    }

    private void displayDeclarationList(List<ServiceReference> references) {
        for (ServiceReference reference : references) {
            Declaration declaration = (Declaration) context.getService(reference);
            String state;
            if (declaration.getStatus().isBound()) {
                state = " BOUND ";
            } else {
                state = "UNBOUND";
            }
            String identifier = getIdentifier(declaration);
            System.out.printf("[%s]\t%s%n", state, identifier);
        }
    }


    @Descriptor("Gets info about the declaration")
    public void declaration(@Descriptor("declaration [-f LDAP_FILTER] [DECLARATION_ID]") String... parameters) {

        Filter filter = null;

        try {

            String explicitFilterArgument = getArgumentValue("-f", parameters);

            if (explicitFilterArgument == null) {

                String idFilterArgument = getArgumentValue(null, parameters);

                if (idFilterArgument == null) {
                    filter = null;
                } else {
                    filter = FrameworkUtil.createFilter(String.format("(id=%s)", idFilterArgument));
                }

            } else {
                filter = FrameworkUtil.createFilter(getArgumentValue(null, parameters));
            }

        } catch (InvalidSyntaxException e) {
            LOG.error("Failed to create the appropriate filter.", e);
            return;
        }

        String type = getArgumentValue("-t", parameters);
        Map<ServiceReference, Declaration> declarations = getDeclarations(type);

        displayDeclarations(declarations, filter);
    }


    private Map<ServiceReference, Declaration> getDeclarations(String type) {
        Map<ServiceReference, Declaration> declarations;

        if ((type != null) && "import".equals(type)) {
            declarations = new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(ImportDeclaration.class));
        } else if ((type != null) && "export".equals(type)) {
            declarations = new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(ExportDeclaration.class));
        } else {
            declarations = new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(Declaration.class));
            declarations.putAll(new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(ImportDeclaration.class)));
            declarations.putAll(new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(ExportDeclaration.class)));
        }
        return declarations;
    }

    private void displayDeclarations(Map<ServiceReference, Declaration> declarations, Filter filter) {
        if (declarations.isEmpty()) {
            System.err.println("No declarations found.");
            return;
        }

        for (Map.Entry<ServiceReference, Declaration> declaration : declarations.entrySet()) {
            if (filter == null || filter.matches(declaration.getValue().getMetadata())) {
                displayDeclaration(getIdentifier(declaration.getValue()), declaration.getKey(), declaration.getValue());
            }
        }
    }

    private void displayDeclaration(String identifier, ServiceReference reference, Declaration declaration) {

        StringBuilder sg = new StringBuilder();
        sg.append("Declaration Metadata : %n");
        for (Map.Entry<String, Object> entry : declaration.getMetadata().entrySet()) {
            sg.append(String.format("\t%s = %s%n", entry.getKey(), entry.getValue()));
        }
        sg.append("Declaration ExtraMetadata : %n");
        for (Map.Entry<String, Object> entry : declaration.getExtraMetadata().entrySet()) {
            sg.append(String.format("\t%s = %s%n", entry.getKey(), entry.getValue()));
        }

        sg.append("Service Properties%n");
        for (String propertyKey : reference.getPropertyKeys()) {
            sg.append(String.format("\t%s = %s%n", propertyKey, reference.getProperty(propertyKey)));
        }
        if (reference.getPropertyKeys().length == 0) {
            sg.append("\tEMPTY%n");
        }

        sg.append("Declaration binded to ")
                .append(declaration.getStatus().getServiceReferencesBounded().size())
                .append(" services.%n");
        sg.append("Declaration handled by ")
                .append(declaration.getStatus().getServiceReferencesHandled().size())
                .append(" services.%n");

        System.out.printf(sg.toString());

    }


    // ---------------- LINKER

    @Descriptor("Gets the importation/exportation linker available")
    public void linker(@Descriptor("linker [-(import|export)] [ID name]") String... parameters) {
        List<ServiceReference> exportationLinkerRef = getAllServiceRefs(ExportationLinker.class);
        List<ServiceReference> importationLinkerRef = getAllServiceRefs(ImportationLinker.class);
        if (exportationLinkerRef.isEmpty() && importationLinkerRef.isEmpty()) {
            System.out.println("No linkers available.");
        } else {
            if (!exportationLinkerRef.isEmpty()) {
                for (ServiceReference reference : exportationLinkerRef) {
                    displayServiceInfo("Exportation Linker", reference);
                    displayServiceProperties("Exportation Linker", reference, "\t\t");
                }
            }
            if (!importationLinkerRef.isEmpty()) {
                for (ServiceReference reference : importationLinkerRef) {
                    displayServiceInfo("Importation Linker", reference);
                    displayServiceProperties("Importation Linker", reference, "\t\t");
                }
            }
        }
    }

    // ---------------- DISCOVERY

    @Descriptor("Gets the discovery available in the platform")
    public void discovery(@Descriptor("discovery [discovery name]") String... parameters) {
        List<ServiceReference> discoveryRef = getAllServiceRefs(DiscoveryService.class);
        if (discoveryRef.isEmpty()) {
            System.out.println("No discovery available.");
        } else {
            for (ServiceReference reference : discoveryRef) {
                displayServiceInfo("Discovery", reference);
                displayServiceProperties("Discovery", reference, "\t\t");
            }
        }
    }

    // ---------------- IMPORTER

    @Descriptor("Gets the importer available in the platform")
    public void importer(@Descriptor("importer [importer name]") String... parameters) {
        Map<ServiceReference, ImporterService> importerRefsAndServices = getAllServiceRefsAndServices(ImporterService.class);
        if (importerRefsAndServices.isEmpty()) {
            System.out.println("No importers available.");
        } else {
            for (Map.Entry<ServiceReference, ImporterService> e : importerRefsAndServices.entrySet()) {
                displayServiceInfo("Importer", e.getKey());
                System.out.println(String.format("\t*importer name = %s", e.getValue().getName()));
                displayServiceProperties("Importer", e.getKey(), "\t\t");
            }
        }
    }

    // ---------------- EXPORTER

    @Descriptor("Gets the exporter available in the platform")
    public void exporter(@Descriptor("exporter [exporter name]") String... parameters) {
        Map<ServiceReference, ExporterService> exporterRefsAndServices = getAllServiceRefsAndServices(ExporterService.class);
        if (exporterRefsAndServices.isEmpty()) {
            System.out.println("No exporter available.");
        } else {
            for (Map.Entry<ServiceReference, ExporterService> e : exporterRefsAndServices.entrySet()) {
                displayServiceInfo("Exporter", e.getKey());
                System.out.println(String.format("\t*exporter name = %s", e.getValue().getName()));
                displayServiceProperties("Exporter", e.getKey(), "\t\t");
            }
        }
    }

    // ---------------- SEND MESSAGE

    @Descriptor("Send event admin messages")
    public void sendmessage(@Descriptor("sendmessage BUS [KEY=VALUE ]*") String... parameters) {
        assert parameters[0] != null;
        String bus = parameters[0];
        Dictionary eventAdminPayload = new Hashtable();
        for (String m : parameters) {
            if (m.contains("=")) {
                StringTokenizer st = new StringTokenizer(m, "=");
                assert st.countTokens() == 2;
                String key = st.nextToken();
                String value = st.nextToken();
                eventAdminPayload.put(key, value);
            }
        }
        Event eventAdminMessage = new Event(bus, eventAdminPayload);
        System.out.println(String.format("Sending message to the bus %s with the arguments %s", bus, eventAdminPayload));
        System.out.println("Event admin message sent");
        eventAdmin.sendEvent(eventAdminMessage);
    }


    // ---------------- UTILS SERVICES

    private <T> List<ServiceReference> getAllServiceRefs(Class<T> klass) {
        ServiceReference[] importDeclarationsRef;
        try {
            importDeclarationsRef = context.getAllServiceReferences(klass.getName(), null);
        } catch (InvalidSyntaxException e) {
            LOG.error("Failed to retrieved services " + klass.getName(), e);
            return new ArrayList<ServiceReference>();
        }
        if (importDeclarationsRef != null) {
            return Arrays.asList(importDeclarationsRef);
        }
        return new ArrayList<ServiceReference>();
    }

    private <T> List<T> getAllServices(Class<T> klass) {
        List<T> services = new ArrayList<T>();
        for (ServiceReference sr : getAllServiceRefs(klass)) {
            services.add((T) context.getService(sr));
        }
        return services;
    }

    private <T> Map<ServiceReference, T> getAllServiceRefsAndServices(Class<T> klass) {
        Map<ServiceReference, T> services = new HashMap<ServiceReference, T>();
        for (ServiceReference sr : getAllServiceRefs(klass)) {
            services.put(sr, (T) context.getService(sr));
        }
        return services;
    }

    // ---------------- UTILS DISPLAY

    private static void displayServiceProperties(String prolog, ServiceReference reference, String prefixTabulation) {
        System.out.printf("%s Service Properties%n", prolog);
        for (String propertyKey : reference.getPropertyKeys()) {
            System.out.println(String.format(prefixTabulation + "%s\t\t = %s", propertyKey, reference.getProperty(propertyKey)));
        }
        if (reference.getPropertyKeys().length == 0) {
            System.out.println(prefixTabulation + "EMPTY");
        }
    }

    private static void displayServiceInfo(String prolog, ServiceReference reference) {
        System.out.println(String.format("%s [%s] provided by bundle %s (%s)", prolog, reference.getProperty(INSTANCE_NAME_PROPERTY), reference.getBundle().getSymbolicName(), reference.getBundle().getBundleId()));
    }

    // ---------------- UTILS GOGO ARGUMENTS

    private static String getArgumentValue(String option, String... params) {
        boolean found = false;
        String value = null;

        for (int i = 0; i < params.length; i++) {

            /**
             * In case of a Null option, returns the last parameter
             */
            if (option == null) {
                return params[params.length - 1];
            }

            if (i < (params.length - 1) && params[i].equals(option)) {
                found = true;
                value = params[i + 1];
                break;
            }
        }

        if (found) {
            return value;
        }
        return null;
    }

}


