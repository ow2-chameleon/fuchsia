package org.ow2.chameleon.fuchsia.philips.hue.importer;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;
import org.ow2.chameleon.fuchsia.philips.hue.importer.util.PhilipsImporterPojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(name = "PhilipsHueImporterFactory")
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class PhilipsHueImporter extends AbstractImporterComponent {

    private BundleContext context;

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private Map<String,ServiceRegistration> lamps=new HashMap<String, ServiceRegistration>();
    private Map<String,ServiceRegistration> bridges=new HashMap<String, ServiceRegistration>();

    @ServiceProperty(name = "target", value = "(discovery.philips.device.name=*)")
    private String filter;

    public PhilipsHueImporter(BundleContext context) {
        this.context = context;
    }

    @Validate
    public void validate() {
        log.info("Philips hue Importer RPC is up and running");
    }

    @Invalidate
    public void invalidate() {

        log.info("Cleaning up instances into Philips hue Importer");

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
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {

        log.info("philips hue importer triggered");

        PhilipsImporterPojo pojo=PhilipsImporterPojo.create(importDeclaration);

        try {

            FuchsiaUtils.loadClass(context,pojo.getType());

            Dictionary<String, Object> props = new Hashtable<String, Object>();

            ServiceRegistration lampService=context.registerService(pojo.getType(),pojo.getObject(),props);
            ServiceRegistration bridgeService=context.registerService(pojo.getBridgeType(),pojo.getBridgeObject(),props);

            lamps.put(pojo.getId(),lampService);
            bridges.put(pojo.getId(),lampService);

        } catch (ClassNotFoundException e) {
            log.error("Failed to load type {}, importing process aborted.",pojo.getType());
        }


    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {

        PhilipsImporterPojo pojo=PhilipsImporterPojo.create(importDeclaration);

        try {
            lamps.get(pojo.getId()).unregister();
        }catch(IllegalStateException e){
            log.error("failed unregistering lamp");
        }

        try {
            bridges.get(pojo.getId()).unregister();
        }catch(IllegalStateException e){
            log.error("failed unregistering bridge");
        }




    }



    public List<String> getConfigPrefix() {
        return null;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}

