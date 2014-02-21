package org.ow2.chameleon.fuchsia.core.component.manager;

import com.sun.swing.internal.plaf.synth.resources.synth_sv;
import org.apache.felix.ipojo.util.SystemPropertiesSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.ow2.chameleon.fuchsia.core.FuchsiaUtils.getFilter;
import static org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder.TARGET_FILTER_PROPERTY;

/**
 * <p/>
 * Provides methods to add/remove/retrieve informations/do operations on the DeclarationBinders.
 * <p/>
 * This class doesn't use the DeclarationBinder objects but the ServiceReference of the DeclarationBinder.
 */
public class LinkerBinderManager<D extends Declaration, S extends DeclarationBinder<D>> {

    private final BundleContext bundleContext;
    private final LinkerManagement<D, S> linkerManagement;
    private final Map<ServiceReference<S>, BinderDescriptor> declarationBinders;
    private Filter binderServiceFilter;

    public LinkerBinderManager(BundleContext bundleContext, LinkerManagement linkerManagement, Filter binderServiceFilter) {
        this.bundleContext = bundleContext;
        this.linkerManagement = linkerManagement;
        declarationBinders = new HashMap<ServiceReference<S>, BinderDescriptor>();
        this.binderServiceFilter = binderServiceFilter;
    }

    /**
     * Add the declarationBinderRef to the ImportersManager, create the corresponding
     * BinderDescriptor.
     *
     * @param declarationBinderRef the ServiceReference<DeclarationBinder> of the DeclarationBinder
     * @throws InvalidFilterException
     */
    public void add(ServiceReference<S> declarationBinderRef) throws InvalidFilterException {
        BinderDescriptor binderDescriptor = new BinderDescriptor(declarationBinderRef);
        declarationBinders.put(declarationBinderRef, binderDescriptor);
    }

    /**
     * Remove the declarationBinderRef of the ImportersManager
     *
     * @param declarationBinderRef the ServiceReference<DeclarationBinder> of the DeclarationBinder
     */
    public void remove(ServiceReference<S> declarationBinderRef) {
        declarationBinders.remove(declarationBinderRef);
    }

    /**
     * Update the BinderDescriptor of the  declarationBinderRef
     *
     * @param declarationBinderRef the ServiceReference<DeclarationBinder> of the DeclarationBinder
     */
    public void modified(ServiceReference<S> declarationBinderRef) throws InvalidFilterException {
        declarationBinders.get(declarationBinderRef).update(declarationBinderRef);
    }

    /**
     * Return true if the DeclarationBinder has match the ImportDeclarationFilter or false other.
     *
     * @param declarationBinderRef the ServiceReference<DeclarationBinder> of the DeclarationBinder
     * @return true if the DeclarationBinder has match the ImportDeclarationFilter, false otherwise.
     */
    public Boolean matched(ServiceReference<S> declarationBinderRef) {
        return declarationBinders.get(declarationBinderRef).match;
    }

    /**
     * Return the DeclarationBinder of the declarationBinderRef
     *
     * @param declarationBinderRef the ServiceReference<DeclarationBinder> of the DeclarationBinder
     * @return the DeclarationBinder of the declarationBinderRef
     */
    public S getDeclarationBinder(ServiceReference<S> declarationBinderRef) {
        return bundleContext.getService(declarationBinderRef);
    }

    /**
     * Return the Target Filter of the DeclarationBinder
     *
     * @param declarationBinderRef the ServiceReference<DeclarationBinder> of the DeclarationBinder
     * @return the Target Filter of the DeclarationBinder.
     */
    public Filter getTargetFilter(ServiceReference<S> declarationBinderRef) {
        return declarationBinders.get(declarationBinderRef).targetFilter;
    }

    /**
     * Create all the links possible between the DeclarationBinder and all the ImportDeclaration matching the
     * ImportDeclarationFilter of the Linker.
     *
     * @param declarationBinderRef the ServiceReference<DeclarationBinder> of the DeclarationBinder
     */
    public void createLinks(ServiceReference<S> declarationBinderRef) {
        for (D declaration : linkerManagement.getMatchedDeclaration()) {
            if (linkerManagement.canBeLinked(declaration, declarationBinderRef)) {
                linkerManagement.link(declaration, declarationBinderRef);
            }
        }
    }

    /**
     * Update all the links of the DeclarationBinder.
     *
     * @param declarationBinderRef the ServiceReference<DeclarationBinder> of the DeclarationBinder
     */
    public void updateLinks(ServiceReference<S> serviceReference) {
        System.err.println("UPDATE LINKS");
        for (D declaration : linkerManagement.getMatchedDeclaration()) {
            boolean isAlreadyLinked = declaration.getStatus().getServiceReferencesBounded().contains(serviceReference);
            boolean canBeLinked = linkerManagement.canBeLinked(declaration, serviceReference);
            if (isAlreadyLinked && !canBeLinked) {
                System.err.println("UNLINK");
                linkerManagement.unlink(declaration, serviceReference);
            } else if (!isAlreadyLinked && canBeLinked) {
                System.err.println("NEW LINK");
                linkerManagement.link(declaration, serviceReference);
            }else{
                System.err.println("NOTHING isAlreadyLinked="+isAlreadyLinked+" ,canBeLinked="+canBeLinked);
            }
        }
    }

    /**
     * Remove all the existing links of the DeclarationBinder.
     *
     * @param declarationBinderRef the ServiceReference<DeclarationBinder> of the DeclarationBinder
     */
    public void removeLinks(ServiceReference<S> declarationBinderRef) {
        for (D declaration : linkerManagement.getMatchedDeclaration()) {
            if (declaration.getStatus().getServiceReferencesBounded().contains(declarationBinderRef)) {
                linkerManagement.unlink(declaration, declarationBinderRef);
            }
        }
    }

    /**
     * Return a set of all DeclarationBinder matching the DeclarationBinderFilter of the Linker.
     *
     * @return a Set of all DeclarationBinder matching the DeclarationBinderFilter of the Linker.
     */
    public Set<S> getMatchedDeclarationBinder() {
        Set<S> bindedSet = new HashSet<S>();
        for (Map.Entry<ServiceReference<S>, BinderDescriptor> e : declarationBinders.entrySet()) {
            if (e.getValue().match) {
                bindedSet.add(getDeclarationBinder(e.getKey()));
            }
        }
        return bindedSet;
    }

    /**
     * Return a set of all ServiceReference<DeclarationBinder> matching the DeclarationBinderFilter of the
     * Linker.
     *
     * @return a Set of all ServiceReference<DeclarationBinder> matching the DeclarationBinderFilter of the
     * Linker.
     */
    public Set<ServiceReference<S>> getMatchedBinderServiceRef() {
        Set<ServiceReference<S>> bindedSet = new HashSet<ServiceReference<S>>();
        for (Map.Entry<ServiceReference<S>, BinderDescriptor> e : declarationBinders.entrySet()) {
            if (e.getValue().match) {
                bindedSet.add(e.getKey());
            }
        }
        return bindedSet;
    }

    /**
     * Compute and apply all the modifications bring by the modification of the DeclarationBinderFilter.
     * <p/>
     * Find all the DeclarationBinder that are now matching the filter and all that are no more matching the filter.
     * <ul>
     * <li>Remove all the links of the ones which are no more matching the DeclarationBinderFilter.</li>
     * <li>Create the links of the ones which are now matching the DeclarationBinderFilter.</li>
     * </ul>
     * @param binderServiceFilter
     */
    public void applyFilterChanges(Filter binderServiceFilter) {
        this.binderServiceFilter = binderServiceFilter;

        Set<ServiceReference<S>> added = new HashSet<ServiceReference<S>>();
        Set<ServiceReference<S>> removed = new HashSet<ServiceReference<S>>();

        for (Map.Entry<ServiceReference<S>, BinderDescriptor> e : declarationBinders.entrySet()) {
            boolean matchFilter = this.binderServiceFilter.matches(e.getValue().properties);
            if (matchFilter != e.getValue().match && matchFilter) {
                added.add(e.getKey());
            } else if (matchFilter != e.getValue().match && !matchFilter) {
                removed.add(e.getKey());
            }
            e.getValue().match = matchFilter;
        }
        for (ServiceReference<S> binderReference : removed) {
            removeLinks(binderReference);
        }
        for (ServiceReference<S> binderReference : added) {
            createLinks(binderReference);
        }
    }


    /**
     * Stock some informations processed/retrieved from the DeclarationBinder
     */
    private class BinderDescriptor {
        Filter targetFilter;
        boolean match;
        Map<String, Object> properties;

        private BinderDescriptor(ServiceReference<S> serviceReference) throws InvalidFilterException {
            properties = new HashMap<String, Object>();
            update(serviceReference);
        }

        /**
         * Extract the information of the declarationBinderRef to update the BinderDescriptor
         * information.
         *
         * @param declarationBinderRef the ServiceReference<DeclarationBinder> of the DeclarationBinder
         * @throws InvalidFilterException
         */
        private void update(ServiceReference<S> declarationBinderRef) throws InvalidFilterException {
            properties.clear();
            for (String key : declarationBinderRef.getPropertyKeys()) {
                properties.put(key, declarationBinderRef.getProperty(key));
            }
            match = binderServiceFilter.matches(properties);
            targetFilter = getFilter(properties.get(TARGET_FILTER_PROPERTY));
        }
    }
}
