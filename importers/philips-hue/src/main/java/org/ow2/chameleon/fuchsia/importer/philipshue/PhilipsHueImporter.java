package org.ow2.chameleon.fuchsia.importer.philipshue;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.philipshue.util.PhilipsImporterPojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class PhilipsHueImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(PhilipsHueImporter.class);

    private final BundleContext context;

    private ServiceReference serviceReference;

    private Map<String,ServiceRegistration> lamps=new HashMap<String, ServiceRegistration>();
    private Map<String,ServiceRegistration> bridges=new HashMap<String, ServiceRegistration>();

    @ServiceProperty(name = "target", value = "(discovery.philips.device.name=*)")
    private String filter;

    public PhilipsHueImporter(BundleContext context) {
        this.context = context;
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }

    @Validate
    public void validate() {
        LOG.info("Philips hue Importer RPC is up and running");
    }

    @Invalidate
    public void invalidate() {

        LOG.info("Cleaning up instances into Philips hue Importer");

    }

    private void cleanup(){

        for(Map.Entry<String,ServiceRegistration> lampEntry:lamps.entrySet()){
            lampEntry.getValue().unregister();
        }

        for(Map.Entry<String,ServiceRegistration> bridgeEntry:bridges.entrySet()){
            bridgeEntry.getValue().unregister();
        }

    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("philips hue importer triggered");

        PhilipsImporterPojo pojo=PhilipsImporterPojo.create(importDeclaration);

        try {

            FuchsiaUtils.loadClass(context,pojo.getType());

            Dictionary<String, Object> props = new Hashtable<String, Object>();

            ServiceRegistration lampService=context.registerService(pojo.getType(),pojo.getObject(),props);
            ServiceRegistration bridgeService=context.registerService(pojo.getBridgeType(),pojo.getBridgeObject(),props);

            importDeclaration.handle(serviceReference);

            lamps.put(pojo.getId(),lampService);
            bridges.put(pojo.getId(),bridgeService);

        } catch (ClassNotFoundException e) {
            LOG.error("Failed to load type {}, importing process aborted.", pojo.getType(), e);
        }


    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        PhilipsImporterPojo pojo=PhilipsImporterPojo.create(importDeclaration);

        try {
            lamps.get(pojo.getId()).unregister();
        }catch(IllegalStateException e){
            LOG.error("failed unregistering lamp", e);
        }

        try {
            bridges.get(pojo.getId()).unregister();
        }catch(IllegalStateException e){
            LOG.error("failed unregistering bridge", e);
        }
        importDeclaration.unhandle(serviceReference);
    }


    public String getName() {
        return this.getClass().getSimpleName();
    }
}

