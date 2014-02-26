package org.ow2.chameleon.fuchsia.importer.philipshue.util;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

public class PhilipsImporterPojo {

    private String id;
    private String name;
    private String type;
    private Object object;
    private String bridgeType;
    private Object bridgeObject;

    private PhilipsImporterPojo(){

    }

    public static PhilipsImporterPojo create(ImportDeclaration importDeclaration) throws BinderException {

        PhilipsImporterPojo dto=new PhilipsImporterPojo();

        try{
            dto.id=importDeclaration.getMetadata().get("id").toString();
            dto.name=importDeclaration.getMetadata().get("discovery.philips.device.name").toString();
            dto.type=importDeclaration.getMetadata().get("discovery.philips.device.type").toString();
            dto.object=importDeclaration.getMetadata().get("discovery.philips.device.object");
            dto.bridgeType=importDeclaration.getMetadata().get("discovery.philips.bridge.type").toString();
            dto.bridgeObject=importDeclaration.getMetadata().get("discovery.philips.bridge.object");
        }catch(NullPointerException npe){
            throw new BinderException(npe);
        }

        return dto;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }


    public Object getObject() {
        return object;
    }


    public String getBridgeType() {
        return bridgeType;
    }

    public Object getBridgeObject() {
        return bridgeObject;
    }
}
