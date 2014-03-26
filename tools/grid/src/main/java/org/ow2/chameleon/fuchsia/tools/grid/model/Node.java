package org.ow2.chameleon.fuchsia.tools.grid.model;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Tool Grid
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


/**
 * This is a transfer object used by the FreeMarker template
 */
public class Node {

    private String id;
    private String label;

    public Node(String name){
        this.id=name;
        this.label=name;
    }

    public Node(String id,String label){
        this.id=id;
        this.label=label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
