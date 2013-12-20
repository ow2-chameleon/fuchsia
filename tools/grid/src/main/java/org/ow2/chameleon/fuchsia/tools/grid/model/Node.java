package org.ow2.chameleon.fuchsia.tools.grid.model;


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
