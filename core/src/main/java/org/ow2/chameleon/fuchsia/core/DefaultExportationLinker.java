package org.ow2.chameleon.fuchsia.core;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.FuchsiaUtils.getFilter;

/**
 * The {@link DefaultExportationLinker} component is the default implementation of the interface
 * {@link ExportationLinker}.
 * <p/>
 * The {@link DefaultExportationLinker} component take as mandatory ServiceProperty a filter on the
 * {@link ExportDeclaration} named {@literal {@link #FILTER_EXPORTDECLARATION_PROPERTY }} and a filter on
 * {@link ExporterService} named {@literal {@link #FILTER_EXPORTERSERVICE_PROPERTY }}.
 * <p/>
 * The filters are String with the LDAP syntax OR {@link org.osgi.framework.Filter}.
 * <p/>
 * An optional ServiceProperty @literal {@link #UNIQUE_EXPORTATION_PROPERTY}} can disallow the DefaultExportationLinker
 * to give an {@link ExportDeclaration} to more than one {@link ExporterService}. This Property is set to False by
 * default.
 * WARNING : this property is actually based on the number of {@link ExporterService} actually bind to the
 * {@link ExportDeclaration}, if an other {@link ExportationLinker} has already bind the {@link ExportDeclaration} to an
 * {@link ExporterService}, the {@link DefaultExportationLinker} will not give the
 * {@link ExportDeclaration} to any of its {@link ExporterService}. The others {@link ExportationLinker} can bind theirs
 * {@link ExportDeclaration} to many {@link ExporterService} if they are not configured for an unique export by
 * {@link ExportDeclaration}.
 *
 * @author Morgan Martinet
 */
@Component(name = FuchsiaConstants.DEFAULT_EXPORTATION_LINKER_FACTORY_NAME)
@Provides(specifications = ExportationLinker.class)
public class DefaultExportationLinker implements ExportationLinker {

    //@Controller
    private boolean state;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String linkerName;

    @ServiceProperty(name = FILTER_EXPORTDECLARATION_PROPERTY, mandatory = true)
    private Object exportDeclarationFilterProperty;

    @ServiceProperty(name = FILTER_EXPORTERSERVICE_PROPERTY, mandatory = true)
    private Object exporterServiceFilterProperty;

    @ServiceProperty(name = UNIQUE_EXPORTATION_PROPERTY, mandatory = false)
    private boolean uniqueExportationProperty = false;

    private Filter exportDeclarationFilter;

    private final BundleContext bundleContext;

    @Property(name = FILTER_EXPORTDECLARATION_PROPERTY, mandatory = true)
    public void computeExportDeclarationFilter(Object filterProperty) {
        if (!state) {
            return;
        }
        try {
            exportDeclarationFilter = getFilter(filterProperty);
            state = true;
        } catch (InvalidFilterException invalidFilterException) {
            LOGGER.debug("The value of the Property " + FILTER_EXPORTDECLARATION_PROPERTY + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
        }
    }

    private final Object lock = new Object();

    private final Map<ExporterService, Filter> exporterServices = new HashMap<ExporterService, Filter>();

    private final Map<ServiceReference, ExporterService> exporterServiceReferences = new HashMap<ServiceReference, ExporterService>();

    private final Set<ExportDeclaration> exportDeclarations = new HashSet<ExportDeclaration>();

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExportationLinker.class);

    @Validate
    public void start() {
        LOGGER.debug(linkerName + " starting");
    }

    @Invalidate
    public void stop() {
        LOGGER.debug(linkerName + " stopping");
    }

    public DefaultExportationLinker(BundleContext context) {
        this.bundleContext=context;
    }

    /**
     * Bind all the {@link ExporterService} matching the exporterServiceFilter.
     * <p/>
     * Foreach {@link ExporterService}, check all the already bound exportDeclarations.
     * If the metadata of the {@link ExportDeclaration} match the filter exposed by the exporter
     * bind the {@link ExportDeclaration}  to the exporter
     */
    @Bind(id = "exporterServices", specification = "org.ow2.chameleon.fuchsia.core.component.ExporterService", optional = true, aggregate = true)
    void bindExporterService(ServiceReference<ExporterService> serviceReference) throws InvalidFilterException {

        ExporterService exporterService=bundleContext.getService(serviceReference);

        Map<String,Object> properties=new HashMap<String, Object>();

        for(String property:serviceReference.getPropertyKeys()){
            properties.put(property,serviceReference.getProperty(property));
        }

        if (!getFilter(exporterServiceFilterProperty).matches(properties)) {
            return;
        }

        LOGGER.debug(linkerName + " : Bind the ExporterService " + exporterService);

        Filter filter = null;
        try {
            filter = getFilter(properties.get("target"));
        } catch (InvalidFilterException invalidFilterException) {
            LOGGER.error("The ServiceProperty \"target\" of the ExporterService " + exporterService
                    + " doesn't provides a valid Filter."
                    + " To be used, it must provides a correct \"target\" ServiceProperty.", invalidFilterException);
            return;
        }

        synchronized (lock) {
            LOGGER.debug(linkerName + " : Add the ExporterService " + exporterService
                    + " with filter " + filter.toString());

            exporterServices.put(exporterService, filter);
            exporterServiceReferences.put(serviceReference, exporterService);
            for (ExportDeclaration exportDeclaration : exportDeclarations) {
                tryToBind(exportDeclaration, serviceReference);
            }
        }
    }

    /**
     * Unbind the {@link ExporterService}.
     */
    @Unbind(id = "exporterServices")
    void unbindExporterService(ServiceReference serviceReference) {


        ExporterService exporterService=(ExporterService)bundleContext.getService(serviceReference);

        LOGGER.debug(linkerName + " : Unbind the ExporterService " + exporterService);
        synchronized (lock) {
            for (ExportDeclaration exportDeclaration : exportDeclarations) {
                if (exportDeclaration.getStatus().getServiceReferencesBounded().contains(exporterService)) {
                    tryToUnbind(exportDeclaration, serviceReference);
                }
            }
            exporterServices.remove(exporterService);
            exporterServiceReferences.remove(serviceReference);
        }
    }

    /**
     * Bind all the {@link ExportDeclaration} matching the filter exportDeclarationFilter.
     * <p/>
     * Foreach {@link ExportDeclaration}, check if metadata match the filter given exposed by the
     * {@link ExporterService} bound.
     */
    @Bind(id = "exportDeclarations", specification = "org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration", aggregate = true, optional = true)
    void bindExportDeclaration(ExportDeclaration exportDeclaration) throws InvalidFilterException {

        exportDeclarationFilter = getFilter(exportDeclarationFilterProperty);

        if (!exportDeclarationFilter.matches(exportDeclaration.getMetadata())) {
            return;
        }
        LOGGER.debug(linkerName + " : Bind the ExportDeclaration " + exportDeclaration);

        synchronized (lock) {
            exportDeclarations.add(exportDeclaration);
            for (ServiceReference serviceReference : exporterServiceReferences.keySet()) {
                tryToBind(exportDeclaration, serviceReference);
            }

        }
    }

    /**
     * Unbind the {@link ExportDeclaration}.
     */
    @Unbind(id = "exportDeclarations")
    void unbindExportDeclaration(ExportDeclaration exportDeclaration) {
        LOGGER.debug(linkerName + " : Unbind the ExportDeclaration " + exportDeclaration);
        synchronized (lock) {
            for (ServiceReference serviceReference : exportDeclaration.getStatus().getServiceReferencesBounded()) {
                tryToUnbind(exportDeclaration, serviceReference);
            }
            exportDeclarations.remove(exportDeclaration);
        }
    }

    /**
     * Try to bind the {@link ExportDeclaration}  with the {@link ExporterService}  referenced by the
     * {@link ServiceReference}, return true if they have been bind together, false otherwise.
     *
     * @param exportDeclaration        The ExportDeclaration
     * @param exporterServiceReference The ServiceReference of the ExporterService
     * @return true if they have been bind together, false otherwise.
     */
    private boolean tryToBind(ExportDeclaration exportDeclaration, ServiceReference exporterServiceReference) {
        // if the uniqueExportationProperty is set to true and the exportDeclaration is already bind, just return.
        if (uniqueExportationProperty && exportDeclaration.getStatus().isBound()) {
            return false;
        }
        ExporterService exporterService = exporterServiceReferences.get(exporterServiceReference);
        Filter filter = exporterServices.get(exporterService);
        if (filter.matches(exportDeclaration.getMetadata())) {
            try {
                exporterService.addExportDeclaration(exportDeclaration);
            } catch (Exception e) {
                LOGGER.debug(exporterService + " throw an exception with giving to it the ExportDeclaration "
                        + exportDeclaration, e);
                return false;
            }
            LOGGER.debug(exportDeclaration + " match the filter of " + exporterService + " : bind them together");
            exportDeclaration.bind(exporterServiceReference);
            return true;
        }
        LOGGER.debug(exportDeclaration + " doesn't match the filter of " + exporterService
                + "(" + exportDeclaration.getMetadata().toString() + ")");
        return false;
    }

    /**
     * Try to unbind the {@link ExportDeclaration} from the {@link ExporterService} referenced by the
     * {@link ServiceReference}, return true if they have been cleanly unbind, false otherwise.
     *
     * @param exportDeclaration        The ExportDeclaration
     * @param exporterServiceReference The ServiceReference of the ExporterService
     * @return true if they have been cleanly unbind, false otherwise.
     */
    private boolean tryToUnbind(ExportDeclaration exportDeclaration, ServiceReference exporterServiceReference) {
        ExporterService exporterService = exporterServiceReferences.get(exporterServiceReference);
        exportDeclaration.unbind(exporterServiceReference);
        try {
            exporterService.removeExportDeclaration(exportDeclaration);
        } catch (Exception e) {
            LOGGER.debug(exporterService + " throw an exception with removing of it the ExportDeclaration "
                    + exportDeclaration, e);
            return false;
        }
        return true;
    }

    public String getName() {
        return linkerName;
    }

    /**
     * Return the exporterServices linked this DefaultExportationLinker
     *
     * @return the exporterServices linked to this DefaultExportationLinker
     */
    public Set<ExporterService> getLinkedExporters() {
        return new HashSet<ExporterService>(exporterServices.keySet());
    }

    /**
     * Return the exportDeclarations bind by this DefaultExportationLinker
     *
     * @return the exportDeclarations bind by this DefaultExportationLinker
     */
    public Set<ExportDeclaration> getExportDeclarations() {
        return new HashSet<ExportDeclaration>(exportDeclarations);
    }
}
