package org.ow2.chameleon.fuchsia.jsonrpc.importer.it;

import org.jabsorb.JSONRPCBridge;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.swissbox.tinybundles.core.TinyBundles;
import org.osgi.framework.Constants;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.testing.ImporterComponentAbstractTest;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ow2.chameleon.fuchsia.core.component.ImporterService.TARGET_FILTER_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.*;

/**
 * Integration test for the JSON-RPC Importer component.
 *
 * @author Morgan Martinet
 */
public class ImporterTest extends ImporterComponentAbstractTest {
    protected static String HTTP_PORT = "8042";

    private static final String SERVLET_NAME = "/JSONRPC";
    private static final String IMPORTER_NAME = "json-importer";

    @Before
    public void setUpFinal() {
        Dictionary<String, String> conf = new Hashtable<String, String>();
        conf.put(INSTANCE_NAME_PROPERTY, IMPORTER_NAME);
        conf.put(TARGET_FILTER_PROPERTY, "(" + CONFIGS + "=jsonrpc)");
        ipojoHelper.createComponentInstance("Fuchsia-Importer:JSON-RPC", conf);
    }

    @After
    public void tearDownFinal() {
        ipojoHelper.getInstanceByName(IMPORTER_NAME).dispose();
    }

    @Override
    public Option[] getCustomOptions() {
        return CoreOptions.options(
                systemProperty("org.osgi.service.http.port").value(HTTP_PORT),
                getBundles(),
                buildTestBundle()
        );
    }

    public Option getBundles() {
        return CoreOptions.composite(
                CoreOptions.mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.http.jetty").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("org.jabsorb").artifactId("org.ow2.chameleon.commons.jabsorb").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpclient-osgi").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("org.ow2.chameleon.fuchsia.importer").artifactId("org.ow2.chameleon.fuchsia.importer.json-rpc").versionAsInProject()
        );
    }

    public Option buildTestBundle() {
        return CoreOptions.provision(
                TinyBundles.newBundle()
                        .add(JSONRPCServerActivator.class)
                        .set(Constants.BUNDLE_ACTIVATOR, JSONRPCServerActivator.class.getName())
                        .set(Constants.IMPORT_PACKAGE, "org.osgi.service.http"
                                + ",javax.servlet"
                                + ",org.osgi.util.tracker"
                                + ",org.osgi.framework"
                                + ",org.jabsorb"
                                + ",org.junit")
                        .set(Constants.BUNDLE_SYMBOLICNAME, "My dummy bundle")
                        .build());
    }

    @Override
    protected ImporterService getImporterService() {
        return fuchsiaHelper.getServiceObject(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=" + IMPORTER_NAME + ")");
    }

    @Override
    protected <T> ImportDeclaration createImportDeclaration(String endpointId, Class<T> klass, T object) {
        //A JsonServlet must be registered
        JSONRPCBridge.getGlobalBridge().registerObject(endpointId, object, klass);

        // Build associated ImportDeclaration
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ID, endpointId);
        props.put(URL, "http://localhost:" + HTTP_PORT + SERVLET_NAME);
        props.put(SERVICE_CLASS, klass.getName());
        props.put(CONFIGS, "jsonrpc");

        ImportDeclaration iDec = ImportDeclarationBuilder.fromMetadata(props).build();

        return iDec;
    }


}

