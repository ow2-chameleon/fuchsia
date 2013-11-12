package org.ow2.chameleon.fuchsia.exporter.cxf.examples.base;

public class PojoSampleToBeExported implements PojoSampleToBeExportedIface{

    public void showMessage2(){
        System.out.println("ok");
    }

    public String getMessage2(){
        return "ok";
    }

}
