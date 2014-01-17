package org.ow2.chameleon.fuchsia.jsonrpc.importer;

import org.jabsorb.client.Client;

public class ImporterClient extends Client {
    private final ImporterHTTPSession session;

    public ImporterClient(ImporterHTTPSession pSession) {
        super(pSession);
        session = pSession;
    }

    @Override
    public void closeProxy(Object proxy) {
        super.closeProxy(proxy);
        session.close();
    }
}
