package org.ow2.chameleon.fuchsia.importer.jsonrpc.it;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.testing.ImporterComponentAbstractTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ow2.chameleon.fuchsia.core.component.ImporterService.TARGET_FILTER_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.*;

/**
 * Integration test for the JSON-RPC Importer component.
 *
 * @author Morgan Martinet
 */
public class JSONRPCImporterTest extends ImporterComponentAbstractTest {
    protected static Integer HTTP_PORT = 8042;

    private static final String SERVLET_NAME = "/JSONRPC";
    private static final String IMPORTER_NAME = "json-importer";
    private HttpServer httpServer = null;


    @Before
    public void setUpFinal() {
        Dictionary<String, String> conf = new Hashtable<String, String>();
        conf.put(INSTANCE_NAME_PROPERTY, IMPORTER_NAME);
        conf.put(TARGET_FILTER_PROPERTY, "(" + CONFIGS + "=jsonrpc)");
        ipojoHelper.createComponentInstance("org.ow2.chameleon.fuchsia.importer.jsonrpc.JSONRPCImporter", conf);

        // create HttpServer
        try {
            httpServer = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();
    }

    @After
    public void tearDownFinal() {
        ipojoHelper.getInstanceByName(IMPORTER_NAME).dispose();
        httpServer.stop(0);
        httpServer = null;
    }

    @Override
    public Option[] getCustomOptions() {
        return CoreOptions.options(
                getBundles()
        );
    }

    public Option getBundles() {
        return CoreOptions.composite(
                // CoreOptions.vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
                mavenBundle().groupId("org.ow2.chameleon.fuchsia.base.json-rpc")
                        .artifactId("org.ow2.chameleon.fuchsia.base.json-rpc.json-rpc-bundle").versionAsInProject(),
                mavenBundle().groupId("com.fasterxml.jackson.core").artifactId("jackson-core").versionAsInProject(),
                mavenBundle().groupId("commons-io").artifactId("commons-io").versionAsInProject(),
                mavenBundle().groupId("com.fasterxml.jackson.core").artifactId("jackson-databind").versionAsInProject(),
                mavenBundle().groupId("com.fasterxml.jackson.core").artifactId("jackson-annotations").versionAsInProject(),
                mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").versionAsInProject(),
                mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpclient-osgi").versionAsInProject(),
                wrappedBundle(mavenBundle("org.apache.httpcomponents", "httpcore-nio").versionAsInProject()),
                mavenBundle().groupId("org.ow2.chameleon.fuchsia.importer").artifactId("org.ow2.chameleon.fuchsia.importer.json-rpc").versionAsInProject(),
                mavenBundle().groupId("javax.portlet").artifactId("portlet-api").versionAsInProject(),
                mavenBundle().groupId("javax.servlet").artifactId("javax.servlet-api").versionAsInProject(),
                systemPackages("com.sun.net.httpserver", "sun.misc", "com.sun.net.httpserver.spi")

        );
    }

    @Override
    protected ImporterService getImporterService() {
        return fuchsiaHelper.getServiceObject(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=" + IMPORTER_NAME + ")");
    }

    @Override
    protected <T> ImportDeclaration createImportDeclaration(String endpointId, Class<T> klass, T object) {
        //A JsonServlet must be registered
        final JsonRpcServer jsonRpcServer = new JsonRpcServer(object, klass);
        httpServer.createContext(SERVLET_NAME + "/" + endpointId, new HttpHandler() {
            public void handle(HttpExchange httpExchange) throws IOException {
                // Get InputStream for reading the request body.
                // After reading the request body, the stream is close.
                InputStream is = httpExchange.getRequestBody();
                // Get OutputStream to send the response body.
                // When the response body has been written, the stream must be closed to terminate the exchange.
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


                jsonRpcServer.handle(is, byteArrayOutputStream);
                byteArrayOutputStream.close();

                int size = byteArrayOutputStream.size();
                // send response header
                httpExchange.sendResponseHeaders(200, size);

                // write response to real outputStream
                OutputStream realOs = httpExchange.getResponseBody();
                realOs.write(byteArrayOutputStream.toByteArray(), 0, size);
                realOs.close();
            }
        });

        // Build associated ImportDeclaration
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ID, endpointId);
        props.put(URL, "http://localhost:" + HTTP_PORT + SERVLET_NAME + "/" + endpointId);
        props.put(SERVICE_CLASS, klass.getName());
        props.put(CONFIGS, "jsonrpc");

        return ImportDeclarationBuilder.fromMetadata(props).build();
    }

}


