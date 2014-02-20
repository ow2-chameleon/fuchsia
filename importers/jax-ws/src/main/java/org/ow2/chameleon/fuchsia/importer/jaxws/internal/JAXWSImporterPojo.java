package org.ow2.chameleon.fuchsia.importer.jaxws.internal;


import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.jaxws.JAXWSImporter;

public class JAXWSImporterPojo {

    private String endpoint;
    private String clazz;

    private void JAXWSImporterPojo(){

    }

    public static JAXWSImporterPojo create(ImportDeclaration importer) throws BinderException{


        JAXWSImporterPojo pojo=new JAXWSImporterPojo();

        Object endpointObject=importer.getMetadata().get(JAXWSImporter.ENDPOINT_URL);
        Object classNameObject=importer.getMetadata().get(JAXWSImporter.CLASSNAME);

        if(endpointObject==null || classNameObject==null){
            throw new BinderException(String.format("Parameters missing endpoint=%s classname=%s",endpointObject,classNameObject).toString());
        }

        pojo.endpoint=(String) endpointObject;
        pojo.clazz=(String) classNameObject;

        return pojo;

    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getClazz() {
        return clazz;
    }
}
