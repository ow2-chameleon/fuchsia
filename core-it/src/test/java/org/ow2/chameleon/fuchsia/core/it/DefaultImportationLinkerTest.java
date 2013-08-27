package org.ow2.chameleon.fuchsia.core.it;


import org.apache.felix.ipojo.ComponentInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.testing.helpers.BaseTest;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ow2.chameleon.fuchsia.core.FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME;

public class DefaultImportationLinkerTest extends BaseTest {

    private final String linkerInstanceName = "linker";

    //Tested Object
    private ComponentInstance defaultLinkerCI;

    private BundleContext context;

    @Override
    protected Option[] getCustomOptions() {
        return options(
                mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-core").versionAsInProject(),
                wrappedBundle(mavenBundle("org.assertj", "assertj-core").versionAsInProject())
        );
    }

    @Before
    public void setUp() {
        super.commonSetUp();
        context = super.getContext();
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(INSTANCE_NAME_PROPERTY, linkerInstanceName);
        props.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "(*=*)");
        props.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(*=*)");
        defaultLinkerCI = ipojoHelper.createComponentInstance(DEFAULT_IMPORTATION_LINKER_FACTORY_NAME, linkerInstanceName, props);
    }

    @After
    public void tearDown() {
        super.commonTearDown();
    }

    @Override
    public boolean deployTestBundle() {
        return false;
    }

    protected ServiceRegistration<ImportDeclaration> registerImportDeclaration(ImportDeclaration importDeclaration) {
        Dictionary<String, Object> props = new Hashtable<String, Object>(importDeclaration.getMetadata());
        return context.registerService(ImportDeclaration.class, importDeclaration, props);
    }

    /**
     * Test that ImportDeclaration are binded
     *
     * @throws InvalidSyntaxException
     */
    @Test
    public void testBindImportDeclaration() throws InvalidSyntaxException {
        ServiceReference sr = ipojoHelper.getServiceReferenceByName(ImportationLinker.class.getName(), linkerInstanceName);
        ImportationLinker importationLinker = (ImportationLinker) osgiHelper.getServiceObject(sr);

        assertThat(importationLinker.getImportDeclarations().size()).isZero();

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(ImportDeclaration.IMPORTATION_PROTOCOL_NAME, "test");
        metadata.put("n", 0);
        metadata.put(INSTANCE_NAME_PROPERTY, "importdec-" + 0);
        ImportDeclaration iD = ImportDeclarationBuilder.fromMetadata(metadata).build();

        ServiceRegistration reg = registerImportDeclaration(iD);
        assertThat(reg).isNotNull();
        ImportDeclaration serviceID = osgiHelper.waitForService(ImportDeclaration.class, "(" + INSTANCE_NAME_PROPERTY + "=importdec*)", 0);
        assertThat(serviceID).isNotNull();
        assertThat(importationLinker.getImportDeclarations().size()).isEqualTo(1);

        reg.unregister();
        assertThat(importationLinker.getImportDeclarations().size()).isZero();
    }


    /**
     * TODO : Test that ImporterService are binded
     */
    public void testBindImporterServices() {

    }

    /**
     * TODO : Test that ImportDeclaration are given to matching ImporterService
     */
    public void testX() {

    }


}
