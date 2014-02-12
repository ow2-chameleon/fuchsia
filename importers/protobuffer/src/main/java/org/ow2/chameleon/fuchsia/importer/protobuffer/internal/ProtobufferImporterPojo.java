package org.ow2.chameleon.fuchsia.importer.protobuffer.internal;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

public class ProtobufferImporterPojo {

    private String id;
    private String address;
    private String clazz;
    private String service;
    private String message;

    private ProtobufferImporterPojo(){}

    public static ProtobufferImporterPojo create(ImportDeclaration importDeclaration){


        ProtobufferImporterPojo declaration=new ProtobufferImporterPojo();

        declaration.id = importDeclaration.getMetadata().get("id").toString();
        declaration.address = importDeclaration.getMetadata().get("rpc.server.address").toString();
        declaration.clazz = importDeclaration.getMetadata().get("rpc.proto.class").toString();
        declaration.service = importDeclaration.getMetadata().get("rpc.proto.service").toString();
        declaration.message = importDeclaration.getMetadata().get("rpc.proto.message").toString();

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
