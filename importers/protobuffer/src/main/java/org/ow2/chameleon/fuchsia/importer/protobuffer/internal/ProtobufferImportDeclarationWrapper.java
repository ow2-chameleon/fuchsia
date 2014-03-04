package org.ow2.chameleon.fuchsia.importer.protobuffer.internal;

import org.osgi.framework.Filter;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

import java.util.Map;

import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;
import static org.ow2.chameleon.fuchsia.importer.protobuffer.internal.Constants.*;

public class ProtobufferImportDeclarationWrapper {

    private static Filter declarationFilter = buildFilter();

    private String id;
    private String address;
    private String clazz;
    private String service;
    private String message;

    private ProtobufferImportDeclarationWrapper() {

    }

    private static Filter buildFilter() {
        Filter filter;
        String stringFilter = String.format("(&(%s=*)(%s=*)(%s=*)(%s=*)(%s=*))",
                ID, RPC_SERVER_ADDRESS, RPC_PROTO_CLASS, RPC_PROTO_SERVICE, RPC_PROTO_MESSAGE);
        try {
            filter = FuchsiaUtils.getFilter(stringFilter);
        } catch (InvalidFilterException e) {
            throw new IllegalStateException(e);
        }
        return filter;
    }

    public static ProtobufferImportDeclarationWrapper create(ImportDeclaration importDeclaration) throws BinderException {
        Map<String, Object> metadata = importDeclaration.getMetadata();

        if (!declarationFilter.matches(metadata)) {
            throw new BinderException("Not enough information in the metadata to be used by the protobuffer importer");
        }

        ProtobufferImportDeclarationWrapper wrapper = new ProtobufferImportDeclarationWrapper();

        wrapper.id = (String) metadata.get(ID);
        wrapper.address = (String) metadata.get(RPC_SERVER_ADDRESS);
        wrapper.clazz = (String) metadata.get(RPC_PROTO_CLASS);
        wrapper.service = (String) metadata.get(RPC_PROTO_SERVICE);
        wrapper.message = (String) metadata.get(RPC_PROTO_MESSAGE);

        return wrapper;
    }

    public String getAddress() {
        return address;
    }

    public String getClazz() {
        return clazz;
    }

    public String getService() {
        return service;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }
}
