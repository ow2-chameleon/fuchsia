package org.ow2.chameleon.fuchsia.exporter.protobuffer.internal;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

public class ProtobufferExporterPojo {

    String id;
    String address;
    String clazz;
    String message;
    String filter;
    String service;

    private ProtobufferExporterPojo(){

    }

    public static ProtobufferExporterPojo create(ExportDeclaration exportDeclaration) throws BinderException{

        ProtobufferExporterPojo declaration=new ProtobufferExporterPojo();

        Object idObject=exportDeclaration.getMetadata().get("id");
        Object addressObject=exportDeclaration.getMetadata().get("rpc.export.address");
        Object clazzObject=exportDeclaration.getMetadata().get("rpc.export.class");
        Object messageObject=exportDeclaration.getMetadata().get("rpc.export.message");
        Object serviceObject=exportDeclaration.getMetadata().get("rpc.export.service");
        Object filterObject=exportDeclaration.getMetadata().get("rpc.export.filter");

        if(idObject==null
                ||addressObject==null
                ||clazzObject==null
                ||messageObject==null
                ||serviceObject==null){
            throw new BinderException("Not enough information in the metadata to be used by the protobuffer export");
        }

        declaration.id = idObject.toString();
        declaration.address = addressObject.toString();
        declaration.clazz = clazzObject.toString();
        declaration.message = messageObject.toString();
        declaration.service = serviceObject.toString();

        if(filterObject==null){
            declaration.filter=null;
        }else {
            declaration.filter = filterObject.toString();
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

    public String getService() {
        return service;
    }
}
