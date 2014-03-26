package org.ow2.chameleon.fuchsia.tools.proxiesutils;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Proxies Utils
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public interface ProxyFacetInvokable extends FuchsiaProxy  {

    /**
     * Sync.
     *
     * @param method
     * @param args
     * @return
     */
    Object invoke(String method, Object... args) throws ProxyInvokationException;

    /**
     * Async.
     *
     * @param method
     * @param transactionID
     * @param callback
     * @param args
     */
    void invoke(String method, Integer transactionID, Object callback, Object... args) throws ProxyInvokationException;

}
