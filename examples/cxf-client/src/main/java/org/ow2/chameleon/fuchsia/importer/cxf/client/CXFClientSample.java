package org.ow2.chameleon.fuchsia.importer.cxf.client;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.exporter.cxf.examples.base.PojoSampleToBeExportedIface;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component
@Instantiate
public class CXFClientSample {

    BundleContext context;

    @Requires
    PojoSampleToBeExportedIface serviceRemote;

    public CXFClientSample(BundleContext context){

        this.context=context;

    }

    @Validate
    public void start(){

        System.out.println("**** in the client:"+serviceRemote.getMessage2());

    }

}
