package org.ow2.chameleon.fuchsia.core;


import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import java.util.Dictionary;
import java.util.Hashtable;

class LinkerBuilder {
    private final FuchsiaMediator fuchsiaMediator;
    private final String name;
    private Factory factory;
    private Filter importDeclarationFilter;

    LinkerBuilder(Factory defaultFactory, FuchsiaMediator fuchsiaMediator, String name) {
        this.factory = defaultFactory;
        this.fuchsiaMediator = fuchsiaMediator;
        this.name = name;
    }

    public LinkerBuilder fromFactory(Factory factory) {
        // FIXME : check that the components create by the factory provide the Linker interface ?
        this.factory = factory;
        return this;
    }

    public LinkerBuilder importDeclarationFilter(Filter filter) {
        this.importDeclarationFilter = filter;
        return this;
    }

    public LinkerBuilder importDeclarationFilter(String filter) throws InvalidSyntaxException {
        return importDeclarationFilter(FrameworkUtil.createFilter((String) filter));
    }

    public void build() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        Dictionary prop = new Hashtable();
        prop.put("instance.name", name);
        prop.put(Linker.PROPERTY_FILTER_IMPORTDECLARATION, importDeclarationFilter);
        ComponentInstance ci = factory.createComponentInstance(prop);
        fuchsiaMediator.addLinker(name, ci);
    }

}
