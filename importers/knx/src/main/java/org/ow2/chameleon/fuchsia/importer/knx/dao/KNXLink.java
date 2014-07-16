/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.fuchsia.importer.knx.dao;

public class KNXLink {

    private String local;
    private String gateway;

    public KNXLink(String local, String gateway){

        this.local=local;
        this.gateway=gateway;

    }

    @Override
    public int hashCode() {
        int result = local.hashCode();
        result = 31 * result + gateway.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        try {

            KNXLink hook=(KNXLink)obj;

            return hook.local.equals(this.local) && hook.gateway.equals(this.gateway);

        }catch(ClassCastException e){
            return false;
        }

    }

    public String getLocal() {
        return local;
    }

    public String getGateway() {
        return gateway;
    }
}
