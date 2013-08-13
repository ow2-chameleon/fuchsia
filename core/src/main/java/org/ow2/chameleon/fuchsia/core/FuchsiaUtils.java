package org.ow2.chameleon.fuchsia.core;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

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
            throw new InvalidFilterException("The given filter  must be a String using LDAP syntax or an" +
                    "object org.osgi.framework.Filter");
        }
        return filter;
    }
}
