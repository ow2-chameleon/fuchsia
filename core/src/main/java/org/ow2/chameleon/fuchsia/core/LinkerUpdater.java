package org.ow2.chameleon.fuchsia.core;


import org.apache.felix.ipojo.ComponentInstance;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import java.util.Dictionary;
import java.util.Hashtable;

public class LinkerUpdater {

    private final ComponentInstance componentInstance;
    private Filter importDeclarationFilter;

    LinkerUpdater(ComponentInstance componentInstance) {
        this.componentInstance = componentInstance;
    }

    public LinkerUpdater importDeclarationFilter(Filter filter) {
        this.importDeclarationFilter = filter;
        return this;
    }

    public LinkerUpdater importDeclarationFilter(String filter) throws InvalidSyntaxException {
        return importDeclarationFilter(FrameworkUtil.createFilter((String) filter));
    }

    public void update() {
        Dictionary prop = new Hashtable();
        prop.put(Linker.PROPERTY_FILTER_IMPORTDECLARATION, importDeclarationFilter);
        componentInstance.reconfigure(prop);
    }
}
