package org.ow2.chameleon.fuchsia.exporter.protobuffer.internal;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

public class ProtobufferExporterPojo {

    String id;
    String address;
    String clazz;
    String message;
    String filter;


    private ProtobufferExporterPojo(){}

    public static ProtobufferExporterPojo create(ExportDeclaration exportDeclaration){

        ProtobufferExporterPojo declaration=new ProtobufferExporterPojo();

        declaration.id = exportDeclaration.getMetadata().get("id").toString();
        declaration.address = exportDeclaration.getMetadata().get("rpc.export.address").toString();
        declaration.clazz = exportDeclaration.getMetadata().get("rpc.export.class").toString();
        declaration.message = exportDeclaration.getMetadata().get("rpc.export.message").toString();

        Object filterRaw=exportDeclaration.getMetadata().get("rpc.export.filter");

        if(filterRaw==null){
            declaration.filter=null;
        }else {
            declaration.filter = filterRaw.toString();
        }

        return declaration;

    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getClazz() {
        return clazz;
    }

    public String getMessage() {
        return message;
    }

    public String getFilter() {
        return filter;
    }
}
