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

        Object idObject=importDeclaration.getMetadata().get("id");
        Object nameObject=importDeclaration.getMetadata().get("discovery.philips.device.name");
        Object deviceType=importDeclaration.getMetadata().get("discovery.philips.device.type");
        Object deviceObject=importDeclaration.getMetadata().get("discovery.philips.device.object");
        Object bridgeType=importDeclaration.getMetadata().get("discovery.philips.bridge.type");
        Object bridgeObject=importDeclaration.getMetadata().get("discovery.philips.bridge.object");

        if(idObject==null
                || nameObject==null
                || deviceType==null
                || deviceObject==null
                || bridgeType==null
                || bridgeObject==null){
            throw new BinderException("Missing information");
        }

        dto.id=idObject.toString();
        dto.name=nameObject.toString();
        dto.type=deviceType.toString();
        dto.object=deviceObject;
        dto.bridgeType=bridgeType.toString();
        dto.bridgeObject=bridgeObject;

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
