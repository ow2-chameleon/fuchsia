package org.ow2.chameleon.fuchsia.tools.grid.model;

/**
 * This is a transfer object used by the FreeMarker template.
 */
public class Edge {

    private String source;
    private String target;

    public Edge(String source,String target){
        this.source=source;
        this.target=target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

}
