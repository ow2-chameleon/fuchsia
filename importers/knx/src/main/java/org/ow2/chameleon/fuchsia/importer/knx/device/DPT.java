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

import org.ow2.chameleon.fuchsia.importer.knx.device.exception.InvalidDPTException;

import java.util.regex.Pattern;

public enum DPT {

    UNKNOWN("0.000"),
    SWITCH("1.001"),
    STEP("1.007"),
    BOOL("1.002"),
    STRING("16.001"),
    FLOAT("9.002"),
    UCOUNT("5.010"),
    ANGLE("5.003"),
    PERCENT("5.004");

    private String dptid;
    private static final Pattern dptIdPattern = Pattern.compile("\\d{1,3}\\.\\d{3}");

    DPT(String id){
        this.dptid =id;
    }

    public String getDPTID(){
        return dptid;
    }

    public String getDPTName(){
        return this.name().toLowerCase();
    }

    public static DPT getDPTFromName(String name) throws InvalidDPTException {

        for(DPT dpt:values()){
            if(name.equals(dpt.getDPTName())){
                return dpt;
            }
        }

        throw new InvalidDPTException(String.format("DPT NAME %s is not supported by this platform",name));

    }

    public static DPT getDPTFromId(String id) throws InvalidDPTException{

        for(DPT dpt:values()){
            if(id.equals(dpt.dptid)){
                return dpt;
            }
        }

        throw new InvalidDPTException(String.format("DPT ID %s is not supported by this platform",id));

    }

    /**
     * Based on the value given as parameter, tries to figure out if its the name or the id or the dpt
     * @param dptIDorName
     * @return DPT enum that contains the ID and the Name of the DPT
     * @throws InvalidDPTException case its not possible to find a DPT ID or NAME equivalent to the one passed by parameter
     */
    public static DPT getDPT(String dptIDorName) throws InvalidDPTException{

        if(dptIdPattern.matcher(dptIDorName).matches()){
            return getDPTFromId(dptIDorName);
        }else {
            return getDPTFromName(dptIDorName);
        }

    }

}
