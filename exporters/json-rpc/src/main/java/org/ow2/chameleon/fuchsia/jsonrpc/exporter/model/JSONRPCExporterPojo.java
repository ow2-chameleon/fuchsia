package org.ow2.chameleon.fuchsia.jsonrpc.exporter.model;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

public class JSONRPCExporterPojo {

    private String instanceName;
    private String instanceClass;
    private String urlContext;


    private JSONRPCExporterPojo(){}

    public static JSONRPCExporterPojo create(ExportDeclaration exportDeclaration){

        JSONRPCExporterPojo dto=new JSONRPCExporterPojo();

        dto.instanceClass=exportDeclaration.getMetadata().get("fuchsia.export.jsonrpc.class").toString();
        dto.instanceName=exportDeclaration.getMetadata().get("fuchsia.export.jsonrpc.instance").toString();


        String url=(String)exportDeclaration.getMetadata().get("fuchsia.export.jsonrpc.url.context");


        if(url!=null && url.equals("null")){
            dto.urlContext=url;
        }else {
            dto.urlContext="/JSONRPC";
        }

        return dto;

    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceClass() {
        return instanceClass;
    }

    public void setInstanceClass(String instanceClass) {
        this.instanceClass = instanceClass;
    }

    public String getUrlContext() {
        return urlContext;
    }

    public void setUrlContext(String urlContext) {
        this.urlContext = urlContext;
    }
}
