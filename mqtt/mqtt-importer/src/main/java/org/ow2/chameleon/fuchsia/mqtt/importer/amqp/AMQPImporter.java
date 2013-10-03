package org.ow2.chameleon.fuchsia.mqtt.importer.amqp;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(name="AMQPImporterFactory")
@Provides
public class AMQPImporter extends AbstractImporterComponent {

    @ServiceProperty(name = "instance.name")
    private String name;

    @ServiceProperty(name = "target", value = "(id=*)")
    private String filter;

    @Requires(filter = "(factory.name=AMQPJointFactory)")
    Factory jointFactory;

    Map<String,InstanceManager> managedInstances=new HashMap<String, InstanceManager>();

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {

        try {

            Properties instanceProperties=new Properties();

            for(Map.Entry<String,Object> element:importDeclaration.getMetadata().entrySet()){
                instanceProperties.put(element.getKey(), element.getValue());
            }

            InstanceManager im=(InstanceManager)jointFactory.createComponentInstance(instanceProperties);

            String id=(String)importDeclaration.getMetadata().get(Constants.ID);

            managedInstances.put(id, im);

        } catch (Exception e) {
            throw new ImporterException(e);
        }

    }

    @Invalidate
    public void stop(){

        for(Map.Entry<String,InstanceManager> entry:managedInstances.entrySet()){

            entry.getValue().dispose();

        }

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {

        String id=(String)importDeclaration.getMetadata().get(Constants.ID);

        InstanceManager instance=managedInstances.get(id);

        if(instance!=null){
            instance.dispose();
        }else {
            getLogger().warn("Failed to destroy managed instance {}, such instance was not registered by this importer",id);
        }

    }

    public List<String> getConfigPrefix() {
        return new ArrayList<String>(){{add("fuchsia.amqp");}};
    }

    public String getName() {
        return name;
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

}
