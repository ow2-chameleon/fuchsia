package org.ow2.chameleon.fuchsia.core;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

import java.util.ArrayList;
import java.util.List;

public class FuchsiaUtils {

    public static Filter getFilter(Object filterObject) throws InvalidFilterException {
        Filter filter = null;
        if (filterObject instanceof String) {
            try {
                filter = FrameworkUtil.createFilter((String) filterObject);
            } catch (InvalidSyntaxException e) {
                throw new InvalidFilterException("The String given has filter doesn't respect the LDAP syntax", e);
            }
        } else if (filterObject instanceof Filter) {
            filter = (Filter) filterObject;
        } else {
            throw new InvalidFilterException("The given filter must be a String using LDAP syntax or an" +
                    "object org.osgi.framework.Filter");
        }
        return filter;
    }

    /**
     * Load the Class of name <code>klassName</code>
     * TODO : handle class version
     *
     * @param context   The BundleContext
     * @param klassName The Class name
     * @return The Class of name <code>klassName</code>
     * @throws ClassNotFoundException if we can't load the Class of name <code>klassName</code>
     */
    public static Class<?> loadClassNew(BundleContext context, String klassName) throws ClassNotFoundException {
        String packageName = klassName.substring(0, klassName.lastIndexOf(".")); // extract package name
        BundleCapability exportedPackage = getExportedPackage(context, packageName);
        if (exportedPackage == null) {
            throw new ClassNotFoundException("No package found with name " + packageName + " while trying to load the class "
                    + klassName + ".");
        }
        return exportedPackage.getRevision().getBundle().loadClass(klassName);
    }

    /**
     * Return the BundleCapability of a bundle exporting the package packageName.
     *
     * @param context     The BundleContext
     * @param packageName The package name
     * @return the BundleCapability of a bundle exporting the package packageName
     */
    private static BundleCapability getExportedPackage(BundleContext context, String packageName) {
        List<BundleCapability> packages = new ArrayList<BundleCapability>();
        for (Bundle bundle : context.getBundles()) {
            BundleRevision bundleRevision = bundle.adapt(BundleRevision.class);
            for (BundleCapability packageCapability : bundleRevision.getDeclaredCapabilities(BundleRevision.PACKAGE_NAMESPACE)) {
                String pName = (String) packageCapability.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);
                if (pName.equalsIgnoreCase(packageName)) {
                    packages.add(packageCapability);
                }
            }
        }

        Version max = Version.emptyVersion;
        BundleCapability maxVersion = null;
        for (BundleCapability aPackage : packages) {
            Version version = (Version) aPackage.getAttributes().get("version");
            if (max.compareTo(version) <= 0) {
                max = version;
                maxVersion = aPackage;
            }
        }

        return maxVersion;
    }

    /**
     * Load the Class of name <code>klassName</code>
     * TODO : handle class version
     *
     * @param context   The BundleContext
     * @param klassName The Class name
     * @return The Class of name <code>klassName</code>
     * @throws ClassNotFoundException if we can't load the Class of name <code>klassName</code>
     */
    public static Class<?> loadClass(BundleContext context, String klassName) throws ClassNotFoundException {
        ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
        try {
            PackageAdmin padmin = (PackageAdmin) context.getService(sref);
            String packageName = klassName.substring(0, klassName.lastIndexOf(".")); // extract package name
            ExportedPackage pkg = padmin.getExportedPackage(packageName);
            if (pkg == null) {
                throw new ClassNotFoundException("No package found with name " + packageName + " while trying to load the class "
                        + klassName + ".");
            }
            return pkg.getExportingBundle().loadClass(klassName);
        } finally {
            context.ungetService(sref);
        }
    }

    /**
     * Load the Classes of names <code>klassNames</code>
     * TODO : handle class version
     *
     * @param context   The BundleContext
     * @param klassNames The Classes names
     * @return The Classes of names <code>klassNames</code>
     * @throws ClassNotFoundException if we can't load one Class name of the list <code>klassNames</code>
     */
    public static List<Class<?>> loadClasses(BundleContext context, List<String> klassNames) throws ClassNotFoundException {
        ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
        List<Class<?>> klass = new ArrayList<Class<?>>(klassNames.size());

        if (sref == null) { // no package admin !
            return null;
        }
        try {
            PackageAdmin padmin = (PackageAdmin) context.getService(sref);
            for (String klassName : klassNames) {
                String packageName = klassName.substring(0, klassName.lastIndexOf(".")); // extract package name

                ExportedPackage pkg = padmin.getExportedPackage(packageName);
                if (pkg == null) {
                    throw new ClassNotFoundException("No package found with name " + packageName + " while trying to load the class "
                            + klassName + ".");
                }
                klass.add(pkg.getExportingBundle().loadClass(klassName));
            }
            return klass;
        } finally {
            context.ungetService(sref);
        }
    }
}
