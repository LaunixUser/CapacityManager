/**
 * This assembles different elements with employee data (from different sources) into a single tree
 */
package org.ml.capman;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Dr. Matthias Laux
 */
public class TreeLinkData {

    private final Map<String, TreeLink> treeLinks = new TreeMap<>();
    private String rootNodeID;
    private final Set<String> childNodeIDs = new HashSet<>();

    /**
     * @param rootNodeID
     * @param linkData
     */
    public TreeLinkData(String rootNodeID, Map<String, String> linkData) {
        if (rootNodeID == null) {
            throw new IllegalArgumentException("rootNodeID may not be null");
        }
        if (linkData == null) {
            throw new IllegalArgumentException("linkData may not be null");
        }
        this.rootNodeID = rootNodeID;

        //.... First parse the complete data (which may contain more tree link data than we actually wanto to consu8me here for this rootImporterID)
        Map<String, TreeLink> allTreeLinks = new TreeMap<>();
        for (String complexId : linkData.keySet()) {

            //.... This is a hack: the complexId string ensures that the order of the includes makes sure trees lower in the hierarchy are included FIRST ... this should really be done via recursion
            String[] t = complexId.split(":");
            String id = t[0];
            String childNodeID = t[1];

            //.... Parse the link information            
            String[] d = linkData.get(complexId).split("\\|");
            String parentNodeID = d[0];
            String parentEmployeeId = d[1];

            allTreeLinks.put(id, new TreeLink(childNodeID, parentNodeID, parentEmployeeId));

        }

        //.... Now strip down the list to the subtree starting with the rootNodeID
        Set<String> retainIDs = new HashSet<>();
        recurse(allTreeLinks, retainIDs, rootNodeID);
        for (String id : retainIDs) {
            childNodeIDs.add(allTreeLinks.get(id).getChildNodeID());
            treeLinks.put(id, allTreeLinks.get(id));
        }

    }

    /**
     * Recursive analysis to find the subtree information in a grander set - if
     * such a set has been supplied
     *
     * @param links
     * @param retainIDs
     * @param parentNodeID
     */
    private void recurse(Map<String, TreeLink> links, Set<String> retainIDs, String parentNodeID) {
        for (String id : links.keySet()) {
            TreeLink link = links.get(id);
            if (link.getParentNodeID().equals(parentNodeID)) {
                retainIDs.add(id);
                recurse(links, retainIDs, link.getChildNodeID());
            }
        }
    }

    /**
     * @return
     */
    public String getRootNodeID() {
        return rootNodeID;
    }

    /**
     * @return
     */
    public Set<String> getChildNodeIDs() {
        return childNodeIDs;
    }

    /**
     * @return
     */
    public Map<String, TreeLink> getLinkData() {
        return treeLinks;
    }
}
