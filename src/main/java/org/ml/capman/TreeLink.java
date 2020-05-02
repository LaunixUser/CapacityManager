package org.ml.capman;

/**
 * @author Dr. Matthias Laux
 */
public class TreeLink {

    private String parentNodeID;
    private String childNodeID;
    private String parentEmployeeID;

    public TreeLink(String childNodeID, String parentNodeID, String parentEmployeeID) {
        if (childNodeID == null) {
            throw new IllegalArgumentException("childNodeID may not be null");
        }
        if (parentNodeID == null) {
            throw new IllegalArgumentException("parentNodeID may not be null");
        }

        if (parentEmployeeID == null) {
            throw new IllegalArgumentException("parentEmployeeID may not be null");
        }
        this.childNodeID = childNodeID;
        this.parentNodeID = parentNodeID;
        this.parentEmployeeID = parentEmployeeID;
    }

    /**
     * @return the parentImporterID
     */
    public String getParentNodeID() {
        return parentNodeID;
    }

    /**
     * @return the childImporterID
     */
    public String getChildNodeID() {
        return childNodeID;
    }

    /**
     * @return the parentEmployeeID
     */
    public String getParentEmployeeID() {
        return parentEmployeeID;
    }

    /**
     * An aggregate information for the this instance which can be usefule for
     * informational output
     *
     * @return
     */
    public String getSummary() {
        return "parentNodeID = '" + parentNodeID + "' | childNodeID = '" + childNodeID + "' | parentEmployeeID = '" + parentEmployeeID + "'";
    }

}
