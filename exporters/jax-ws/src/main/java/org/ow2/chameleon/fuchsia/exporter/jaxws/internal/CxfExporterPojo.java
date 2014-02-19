package org.ow2.chameleon.fuchsia.exporter.jaxws.internal;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

public class CxfExporterPojo {

    private String clazz;
    private String webcontext;
    private String filter;

    private CxfExporterPojo(){

    }

    public static CxfExporterPojo create(ExportDeclaration exportDeclaration) throws BinderException{

        CxfExporterPojo pojo=new CxfExporterPojo();

        Object clazzObject=exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_TYPE);
        Object webcontextObject=exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_WEB_CONTEXT);

        if(clazzObject==null || webcontextObject==null){
            throw new BinderException(String.format("Parameters missing class=%s webcontext=%s",clazzObject,webcontextObject).toString());
        }

        pojo.clazz=(String) clazzObject;
        pojo.webcontext=(String) webcontextObject;

        Object filterObject=exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_FILTER);

        if(filterObject!=null){
            pojo.filter = filterObject.toString();
        }


        return pojo;
    }

    public String getClazz() {
        return clazz;
    }

    public String getWebcontext() {
        return webcontext;
    }

    public String getFilter() {

        return filter;

    }
}
