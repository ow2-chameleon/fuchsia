package org.ow2.chameleon.fuchsia.jsonrpc.exporter.model;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

/**
 * Created with IntelliJ IDEA.
 * User: jnascimento
 * Date: 27/01/14
 * Time: 17:54
 * To change this template use File | Settings | File Templates.
 */
public class JSONRPCExporterPojo {

    private String instanceName;
    private String instanceClass;


    private JSONRPCExporterPojo(){}

    public static JSONRPCExporterPojo create(ExportDeclaration exportDeclaration){

        JSONRPCExporterPojo dto=new JSONRPCExporterPojo();

        dto.instanceClass=exportDeclaration.getMetadata().get("fuchsia.export.jsonrpc.class").toString();
        dto.instanceName=exportDeclaration.getMetadata().get("fuchsia.export.jsonrpc.instance").toString();

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
}
