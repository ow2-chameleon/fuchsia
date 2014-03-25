package org.ow2.chameleon.fuchsia.core.component.manager;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class LinkerManagement<D extends Declaration, S extends DeclarationBinder<D>> {

    private static final Logger LOG = LoggerFactory.getLogger(LinkerManagement.class);

    private final LinkerBinderManager<D, S> bindersManager;

    private final LinkerDeclarationsManager<D, S> declarationsManager;

    public LinkerManagement(BundleContext bundleContext, Filter importerServiceFilter, Filter declarationFilter) {
        this.bindersManager = new LinkerBinderManager<D, S>(bundleContext, this, importerServiceFilter);
        this.declarationsManager = new LinkerDeclarationsManager<D, S>(bundleContext, this, declarationFilter);
    }

    /**
     * Return true if the Declaration can be linked to the ImporterService
     *
     * @param declaration          The Declaration
     * @param declarationBinderRef The ServiceReference<ImporterService> of the ImporterService
     * @return true if the Declaration can be linked to the ImporterService
     */
    public boolean canBeLinked(D declaration, ServiceReference<S> declarationBinderRef) {
        // Evaluate the target filter of the ImporterService on the Declaration
        Filter filter = bindersManager.getTargetFilter(declarationBinderRef);
        return filter.matches(declaration.getMetadata());
    }

    /**
     * Try to link the declaration with the importerService referenced by the ServiceReference,
     * return true if they have been link together, false otherwise.
     *
     * @param declaration          The Declaration
     * @param declarationBinderRef The ServiceReference<S> of S
     * @return true if they have been link together, false otherwise.
     */
    public boolean link(D declaration, ServiceReference<S> declarationBinderRef) {
        S declarationBinder = bindersManager.getDeclarationBinder(declarationBinderRef);
        LOG.debug(declaration + " match the filter of " + declarationBinder + " : bind them together");
        declaration.bind(declarationBinderRef);
        try {
            declarationBinder.addDeclaration(declaration);
        } catch (BinderException e) {
            declaration.unbind(declarationBinderRef);
            LOG.debug(declarationBinder + " throw an exception when giving to it the Declaration "
                    + declaration, e);
            return false;
        }
        return true;
    }

    /**
     * Try to unlink the declaration from the importerService referenced by the ServiceReference,
     * return true if they have been cleanly unlink, false otherwise.
     *
     * @param declaration          The Declaration
     * @param declarationBinderRef The ServiceReference of the ImporterService
     * @return true if they have been cleanly unlink, false otherwise.
     */
    public boolean unlink(D declaration, ServiceReference<S> declarationBinderRef) {
        S declarationBinder = bindersManager.getDeclarationBinder(declarationBinderRef);
        try {
            declarationBinder.removeDeclaration(declaration);
        } catch (BinderException e) {
            LOG.debug(declarationBinder + " throw an exception when removing of it the Declaration "
                    + declaration, e);
            declaration.unhandle(declarationBinderRef);
            return false;
        } finally {
            declaration.unbind(declarationBinderRef);
        }
        return true;
    }

    public Set<ServiceReference<S>> getMatchedBinderServiceRef() {
        return bindersManager.getMatchedBinderServiceRef();
    }

    public Set<D> getMatchedDeclaration() {
        return declarationsManager.getMatchedDeclaration();
    }

    public LinkerDeclarationsManager<D, S> getDeclarationsManager() {
        return declarationsManager;
    }

    public LinkerBinderManager<D, S> getBindersManager() {
        return bindersManager;
    }
}
