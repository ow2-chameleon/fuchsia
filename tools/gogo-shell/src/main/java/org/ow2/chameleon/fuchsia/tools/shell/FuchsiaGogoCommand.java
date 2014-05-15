package org.ow2.chameleon.fuchsia.tools.shell;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Tool Gogo command
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;


@Component(immediate = true)
@Instantiate
@Provides(specifications = FuchsiaGogoCommand.class)
@SuppressWarnings("PMD.SystemPrintln")
/**
 * {@link FuchsiaGogoCommand} is basic shell command set.
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


    public void print(String message) {
        System.out.println(message);
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
            print("failed to execute the command");
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

        StringBuilder sb = displayDeclarations(declarations, filter);

        print(sb.toString());

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

    private StringBuilder displayDeclarations(Map<ServiceReference, Declaration> declarations, Filter filter) {

        StringBuilder sb = new StringBuilder();

        if (declarations.isEmpty()) {
            sb.append("No declarations found.");
        }

        for (Map.Entry<ServiceReference, Declaration> declaration : declarations.entrySet()) {
            if (filter == null || filter.matches(declaration.getValue().getMetadata())) {

                StringBuilder boxedDeclaration = createASCIIBox("Declaration", displayDeclaration(getIdentifier(declaration.getValue()), declaration.getKey(), declaration.getValue()));
                sb.append(boxedDeclaration);

            }
        }

        return sb;
    }

    private StringBuilder displayDeclaration(String identifier, ServiceReference reference, Declaration declaration) {

        StringBuilder completeOutput = new StringBuilder();

        completeOutput.append("Declaration binded to ")
                .append(declaration.getStatus().getServiceReferencesBounded().size())
                .append(" services.\n");
        completeOutput.append("Declaration handled by ")
                .append(declaration.getStatus().getServiceReferencesHandled().size())
                .append(" services.\n");

        StringBuilder sgMetadata = new StringBuilder();
        for (Map.Entry<String, Object> entry : declaration.getMetadata().entrySet()) {
            sgMetadata.append(String.format("%s = %s\n", entry.getKey(), entry.getValue()));
        }

        completeOutput.append(createASCIIBox("Metadata", sgMetadata));

        StringBuilder sgExtraMetadata = new StringBuilder();
        for (Map.Entry<String, Object> entry : declaration.getExtraMetadata().entrySet()) {
            sgExtraMetadata.append(String.format("%s = %s\n", entry.getKey(), entry.getValue()));
        }

        completeOutput.append(createASCIIBox("Extra-Metadata", sgExtraMetadata));

        StringBuilder sgProperties = new StringBuilder();
        for (String propertyKey : reference.getPropertyKeys()) {
            sgProperties.append(String.format("%s = %s\n", propertyKey, reference.getProperty(propertyKey)));
        }
        if (reference.getPropertyKeys().length == 0) {
            sgProperties.append("EMPTY\n");
        }

        completeOutput.append(createASCIIBox("Service Properties", sgProperties));


        return completeOutput;

    }


    // ---------------- LINKER

    @Descriptor("Gets the importation/exportation linker available")
    public void linker(@Descriptor("linker [-(import|export)] [ID name]") String... parameters) {
        List<ServiceReference> exportationLinkerRef = getAllServiceRefs(ExportationLinker.class);
        List<ServiceReference> importationLinkerRef = getAllServiceRefs(ImportationLinker.class);
        StringBuilder sbFinal = new StringBuilder();
        if (exportationLinkerRef.isEmpty() && importationLinkerRef.isEmpty()) {
            sbFinal.append("No linkers available.\n");
        } else {
            if (!exportationLinkerRef.isEmpty()) {
                for (ServiceReference reference : exportationLinkerRef) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(displayServiceInfo(reference));
                    sb.append(createASCIIBox("Properties", displayServiceProperties(reference)));
                    sbFinal.append(createASCIIBox("Exportation Linker", sb));
                }
            }
            if (!importationLinkerRef.isEmpty()) {
                for (ServiceReference reference : importationLinkerRef) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(displayServiceInfo(reference));
                    sb.append(createASCIIBox("Properties", displayServiceProperties(reference)));
                    sbFinal.append(createASCIIBox("Importation Linker", sb));
                }
            }
        }
        print(sbFinal.toString());
    }

    // ---------------- DISCOVERY

    @Descriptor("Gets the discovery available in the platform")
    public void discovery(@Descriptor("discovery [discovery name]") String... parameters) {
        List<ServiceReference> discoveryRef = getAllServiceRefs(DiscoveryService.class);
        StringBuilder sbFinal = new StringBuilder();
        if (discoveryRef.isEmpty()) {
            sbFinal.append("No discovery available.\n");
        } else {
            for (ServiceReference reference : discoveryRef) {
                StringBuilder sb = new StringBuilder();
                sb.append(displayServiceInfo(reference));
                sb.append(createASCIIBox("Properties", displayServiceProperties(reference)));

                sbFinal.append(createASCIIBox("Discovery", sb));
            }
        }

        print(sbFinal.toString());
    }

    // ---------------- IMPORTER

    @Descriptor("Gets the importer available in the platform")
    public void importer(@Descriptor("importer [importer name]") String... parameters) {
        Map<ServiceReference, ImporterService> importerRefsAndServices = getAllServiceRefsAndServices(ImporterService.class);
        StringBuilder sbFinal = new StringBuilder();
        if (importerRefsAndServices.isEmpty()) {
            sbFinal.append("No importers available.\n");
        } else {
            for (Map.Entry<ServiceReference, ImporterService> e : importerRefsAndServices.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(displayServiceInfo(e.getKey()));
                sb.append(String.format("importer name = %s\n", e.getValue().getName()));
                sb.append(createASCIIBox("Properties", displayServiceProperties(e.getKey())));
                sbFinal.append(createASCIIBox("Importer", sb));
            }
        }
        print(sbFinal.toString());
    }

    // ---------------- EXPORTER

    @Descriptor("Gets the exporter available in the platform")
    public void exporter(@Descriptor("exporter [exporter name]") String... parameters) {
        Map<ServiceReference, ExporterService> exporterRefsAndServices = getAllServiceRefsAndServices(ExporterService.class);
        StringBuilder sbFinal = new StringBuilder();
        if (exporterRefsAndServices.isEmpty()) {
            print("No exporter available.");
        } else {
            for (Map.Entry<ServiceReference, ExporterService> e : exporterRefsAndServices.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(displayServiceInfo(e.getKey()));
                sb.append(String.format("exporter name = %s\n", e.getValue().getName()));
                sb.append(createASCIIBox("Properties", displayServiceProperties(e.getKey())));
                sbFinal.append(createASCIIBox("Exporter", sb));
            }
        }
        print(sbFinal.toString());
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
        print(String.format("Sending message to the bus %s with the arguments %s", bus, eventAdminPayload));
        print("Event admin message sent");
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

    private static StringBuilder displayServiceProperties(ServiceReference reference) {

        StringBuilder sb = new StringBuilder();

        for (String propertyKey : reference.getPropertyKeys()) {
            sb.append(String.format("%s = %s\n", propertyKey, reference.getProperty(propertyKey)));
        }
        if (reference.getPropertyKeys().length == 0) {
            sb.append("EMPTY");
        }

        return sb;
    }

    private static StringBuilder displayServiceInfo(ServiceReference reference) {
        StringBuilder sb = new StringBuilder();
        sb.append("name:" + reference.getProperty(INSTANCE_NAME_PROPERTY) + "\n");
        sb.append("bundle:" + reference.getBundle().getSymbolicName() + "[" + reference.getBundle().getBundleId() + "]" + "\n");

        return sb;
    }

    // ---------------- UTILS GOGO ARGUMENTS

    private static String getArgumentValue(String option, String... params) {
        boolean found = false;
        String value = null;

        for (int i = 0; i < params.length; i++) {

            /**
             * In case of a Null option, returns the last parameter.
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


    private static String reproduceChar(String ch, Integer amount) {

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < amount; x++) {
            sb.append(ch);
        }

        return sb.toString();
    }

    private static StringBuilder createASCIIBox(String prolog, StringBuilder sb) {
        StringBuilder result = new StringBuilder();

        StringReader sr = new StringReader(sb.toString());

        List<Integer> sizeColums = new ArrayList<Integer>();

        String line;
        try {

            BufferedReader br = new BufferedReader(sr);
            while ((line = br.readLine()) != null) {
                sizeColums.add(line.length());
            }

            Collections.sort(sizeColums);
            Collections.reverse(sizeColums);

            Integer maxColumn = sizeColums.isEmpty() ? 0 : sizeColums.get(0);
            if (maxColumn > 45) maxColumn = 45;
            Integer prologSize = prolog.length();

            result.append(reproduceChar(" ", prologSize)).append(".").append(reproduceChar("_", maxColumn)).append("\n");

            sr = new StringReader(sb.toString());
            br = new BufferedReader(sr);
            int lineIndex = 0;
            while ((line = br.readLine()) != null) {

                if (lineIndex == ((Integer) (sizeColums.size() / 2))) {
                    result.append(prolog);
                } else {
                    result.append(reproduceChar(" ", prologSize));
                }

                result.append("|" + line + "\n");
                lineIndex++;
            }

            result.append(reproduceChar(" ", prologSize)).append("|").append(reproduceChar("_", maxColumn)).append("\n");

        } catch (IOException e) {
            LOG.error("Problem while creating a box", e);
        }

        return result;

    }

}


