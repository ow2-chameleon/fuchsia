package org.ow2.chameleon.fuchsia.tools.shell;

import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.fuchsia.core.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.felix.ipojo.Factory.*;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.PROTOCOL_NAME;


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
    String[] m_function = new String[]{"declarations", "declaration", "linker", "discovery", "importer", "exporter", "sendmessage"};

    @Requires
    EventAdmin eventAdmin;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private BundleContext m_context = null;

    public FuchsiaGogoCommand(BundleContext context) {
        this.m_context = context;
    }

    // ---------------- DECLARATION

    @Descriptor("Gets info about the declarations available")
    public void declarations(@Descriptor("declarations [--type import|export]") String... parameters) {
        String type = getArgumentValue("--type", parameters);
        try {
            if (type == null || type.equals("import")) {
                List<ServiceReference> allServiceRef = getAllServiceRefs(ImportDeclaration.class);
                displayDeclarationList(allServiceRef);
            }

            if (type == null || type.equals("export")) {
                List<ServiceReference> allServiceRef = getAllServiceRefs(ExportDeclaration.class);
                displayDeclarationList(allServiceRef);
            }
        } catch (Exception e) {
            log.error("failed to execute command", e);
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
        String scope = (String) declaration.getMetadata().get("scope");
        String protocol = (String) declaration.getMetadata().get(PROTOCOL_NAME);
        String id = (String) declaration.getMetadata().get(ID);

        return type + '.' + scope + '.' + protocol + '.' + id;
    }

    private void displayDeclarationList(List<ServiceReference> references) {
        for (ServiceReference reference : references) {
            Declaration declaration = (Declaration) m_context.getService(reference);
            String state;
            if (declaration.getStatus().isBound()) {
                state = " BOUND ";
            } else {
                state = "UNBOUND";
            }
            String identifier = getIdentifier(declaration);
            System.out.printf("[%s]\t%s\n", state, identifier);
        }
    }


    @Descriptor("Gets info about the declaration")
    public void declaration(@Descriptor("declaration declaration_identifier") String... parameters) {
        if (parameters.length != 1) {
            System.err.println("1 parameter");
            return;
        }
        String identifier = parameters[0];

        String[] split = identifier.split("\\.");
        String filterString = String.format("(&(scope=%s)(%s=%s)(%s=%s))", split[1], PROTOCOL_NAME, split[2], ID, split[3]);
        Filter filter = null;
        try {
            filter = FrameworkUtil.createFilter(filterString);
        } catch (InvalidSyntaxException e) {
            log.error("Failed to create the appropriate filter.", e);
            return;
        }

        HashMap<ServiceReference, Declaration> declarations = null;
        if (split[0].equals("import")) {
            declarations = new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(ImportDeclaration.class));
        } else if (split[0].equals("export")) {
            declarations = new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(ExportDeclaration.class));
        }

        if (declarations == null) {
            System.err.println("No declarations found.");
            return;
        }
        for (Map.Entry<ServiceReference, Declaration> declaration : declarations.entrySet()) {
            if (filter.matches(declaration.getValue().getMetadata())) {
                displayDeclaration(identifier, declaration.getKey(), declaration.getValue());
                return;
            }
        }
        System.err.println("No declarations found with the identifier " + identifier + ".");
    }

    private void displayDeclaration(String identifier, ServiceReference reference, Declaration declaration) {
        System.out.println("Declaration : " + identifier);
        Map<String, Object> metadata = declaration.getMetadata();
        if (metadata.size() == 0) {
            log.error("Malformated declaration, metadata are empty.");
        } else {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                System.out.println(String.format("\t%s\t\t= %s", entry.getKey(), entry.getValue()));
            }
        }
        System.out.println("---");
        displayServiceProperties("Declaration ", reference, "\t");
    }


    // ---------------- LINKER

    @Descriptor("Gets the importation/exportation linker available")
    public void linker(@Descriptor("linker [-(import|export)] [ID name]") String... parameters) {
        String filter = null;
        try {
            ServiceReference[] exportationLinkerRef = m_context.getAllServiceReferences(ExportationLinker.class.getName(), filter);
            ServiceReference[] importationLinkerRef = m_context.getAllServiceReferences(ImportationLinker.class.getName(), filter);
            if (exportationLinkerRef != null || importationLinkerRef != null) {
                if (exportationLinkerRef != null)
                    for (ServiceReference reference : exportationLinkerRef) {
                        displayServiceInfo("Exportation Linker", reference);
                        displayServiceProperties("Exportation Linker", reference, "\t\t");
                    }
                if (importationLinkerRef != null)
                    for (ServiceReference reference : importationLinkerRef) {
                        displayServiceInfo("Importation Linker", reference);
                        displayServiceProperties("Importation Linker", reference, "\t\t");
                    }
            } else {
                System.out.println("No linkers available.");
            }
        } catch (InvalidSyntaxException e) {
            log.error("invalid LDAP filter syntax", e);
            System.out.println("failed to execute the command");
        }
    }

    // ---------------- DISCOVERY

    @Descriptor("Gets the discovery available in the platform")
    public void discovery(@Descriptor("discovery [discovery name]") String... parameters) {
        String filter = null;
        try {
            ServiceReference[] discoveryRef = m_context.getAllServiceReferences(DiscoveryService.class.getName(), filter);
            if (discoveryRef != null) {
                for (ServiceReference reference : discoveryRef) {
                    displayServiceInfo("Discovery", reference);
                    displayServiceProperties("Discovery", reference, "\t\t");
                }
            } else {
                System.out.println("No discovery available.");
            }
        } catch (InvalidSyntaxException e) {
            log.error("invalid ldap filter syntax", e);
            System.out.println("failed to execute the command");
        }
    }

    // ---------------- IMPORTER

    @Descriptor("Gets the importer available in the platform")
    public void importer(@Descriptor("importer [importer name]") String... parameters) {
        String filter = null;
        try {
            ServiceReference[] discoveryRef = m_context.getAllServiceReferences(ImporterService.class.getName(), filter);
            if (discoveryRef != null) {
                for (ServiceReference reference : discoveryRef) {
                    displayServiceInfo("Importer", reference);
                    ImporterService is = (ImporterService) m_context.getService(reference);
                    System.out.println(String.format("\t*importer name = %s", is.getName()));
                    displayServiceProperties("Importer", reference, "\t\t");
                }
            } else {
                System.out.println("No importers available.");
            }
        } catch (InvalidSyntaxException e) {
            log.error("invalid ldap filter syntax", e);
            System.out.println("failed to execute the command");
        }
    }

    // ---------------- EXPORTER

    @Descriptor("Gets the exporter available in the platform")
    public void exporter(@Descriptor("exporter [exporter name]") String... parameters) {
        String filter = null;
        try {
            ServiceReference[] discoveryRef = m_context.getAllServiceReferences(ExporterService.class.getName(), filter);
            if (discoveryRef != null) {
                for (ServiceReference reference : discoveryRef) {
                    displayServiceInfo("Exporter", reference);
                    ExporterService es = (ExporterService) m_context.getService(reference);
                    System.out.println(String.format("\t*exporter name = %s", es.getName()));
                    displayServiceProperties("Exporter", reference, "\t\t");
                }
            } else {
                System.out.println("No exporter available.");
            }
        } catch (InvalidSyntaxException e) {
            log.error("invalid ldap filter syntax", e);
            System.out.println("failed to execute the command");
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
            importDeclarationsRef = m_context.getAllServiceReferences(klass.getName(), null);
        } catch (InvalidSyntaxException e) {
            log.error("Failed to retrieved services " + klass.getName(), e);
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
            services.add((T) m_context.getService(sr));
        }
        return services;
    }

    private <T> Map<ServiceReference, T> getAllServiceRefsAndServices(Class<T> klass) {
        Map<ServiceReference, T> services = new HashMap<ServiceReference, T>();
        for (ServiceReference sr : getAllServiceRefs(klass)) {
            services.put(sr, (T) m_context.getService(sr));
        }
        return services;
    }

    // ---------------- UTILS DISPLAY

    private static void displayServiceProperties(String prolog, ServiceReference reference, String prefixTabulation) {
        System.out.printf("%s Service Properties\n", prolog);
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


