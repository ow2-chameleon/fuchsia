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
package org.ow2.chameleon.fuchsia.importer.knx.device;

public enum DPT {

    UNKNOWN("0.000"),
    SWITCH("1.001"),
    STEP("1.007"),
    BOOL("1.002"),
    STRING("16.001"),
    FLOAT("9.002"),
    UCOUNT("5.010"),
    ANGLE("5.003");

    private String dptid;

    DPT(String id){
        this.dptid =id;
    }

    public String getDPTID(){
        return dptid;
    }

    public String getDPTIDFromName(String name){

        return getDPTFromName(name).getDPTID();
    }

    public DPT getDPTFromName(String name){

        return valueOf(name.toUpperCase());

    }

}
