package org.ow2.chameleon.fuchsia.importer.protobuffer.internal;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

public class ProtobufferImporterPojo {

    private String id;
    private String address;
    private String clazz;
    private String service;
    private String message;

    private ProtobufferImporterPojo(){

    }

    public static ProtobufferImporterPojo create(ImportDeclaration importDeclaration) throws BinderException {

        Object idObject = importDeclaration.getMetadata().get("id");
        Object addressObject = importDeclaration.getMetadata().get("rpc.server.address");
        Object clazzObject = importDeclaration.getMetadata().get("rpc.proto.class");
        Object serviceObject = importDeclaration.getMetadata().get("rpc.proto.service");
        Object messageObject = importDeclaration.getMetadata().get("rpc.proto.message");

        ProtobufferImporterPojo declaration=new ProtobufferImporterPojo();

        if(idObject==null
                ||addressObject==null
                ||clazzObject==null
                ||serviceObject==null
                ||messageObject==null){
            throw new BinderException("Not enough information in the metadata to be used by the protobuffer importer");
        }

        declaration.id = idObject.toString();
        declaration.address = addressObject.toString();
        declaration.clazz = clazzObject.toString();
        declaration.service = serviceObject.toString();
        declaration.message = messageObject.toString();

        return declaration;

    }

    public String getAddress() {
        return address;
    }

    public String getClazz() {
        return clazz;
    }

    public String getService() {
        return service;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }
}
