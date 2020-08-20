package org.ml.capman.reporting;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ml.capman.Constraint;

import org.ml.tools.logging.LoggerFactory;
import org.ml.capman.Employee;
import org.ml.capman.EmployeeData;
import org.ml.capman.TreeLink;
import org.ml.capman.TreeLinkData;
import org.ml.pf.step.AbstractTransferProcessStep;

/**
 * @author mlaux
 */
public class EmployeeDataAssembleStep extends AbstractTransferProcessStep<Map<String, EmployeeData<Employee>>, EmployeeData<Employee>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeDataAssembleStep.class.getName());
    private TreeLinkData treeLinkData;

    /**
     * @param id
     * @param treeLinkData
     */
    public EmployeeDataAssembleStep(String id, TreeLinkData treeLinkData) {
        super(id);
        if (treeLinkData == null) {
            throw new NullPointerException("treeLinkData may not be null");
        }
        this.treeLinkData = treeLinkData;
    }

    /**
     * @param treeLinkData
     */
    public EmployeeDataAssembleStep(TreeLinkData treeLinkData) {
        super();
        if (treeLinkData == null) {
            throw new NullPointerException("treeLinkData may not be null");
        }
        this.treeLinkData = treeLinkData;
    }

    /**
     * @param employeeDataMap
     * @return
     */
    @Override
    public EmployeeData<Employee> convert(Map<String, EmployeeData<Employee>> employeeDataMap) {
        if (employeeDataMap == null) {
            throw new IllegalArgumentException("employeeDataMap may not be null");
        }

        for (String key : employeeDataMap.keySet()) {
            LOGGER.log(Level.INFO, "employeeDataMap key = ''{0}'' - employeeData size = ''{1}", new Object[]{key, employeeDataMap.get(key).getEmployees().size()});
        }
        if (employeeDataMap.isEmpty()) {
            LOGGER.log(Level.SEVERE, "employeeDataMap is empty - something must be very wrong");
        }

        EmployeeData<Employee> output;

        //.... If we have several elements to assemble to a tree, start here
        if (treeLinkData.getLinkData().size() > 0) {

            LOGGER.log(Level.INFO, "Starting tree assembly");

            //.... Evaluate and treat all links (a tree needs to be linked into another tree)
            String rootNodeID = treeLinkData.getRootNodeID();
            Map<String, String> linkedParentNodes = new HashMap<>();
            for (String id : treeLinkData.getLinkData().keySet()) {

                TreeLink treeLink = treeLinkData.getLinkData().get(id);
                LOGGER.log(Level.INFO, "Working on treeLink: ''{0}''", treeLink.getSummary());

                String childNodeID = treeLink.getChildNodeID();
                String parentNodeID = treeLink.getParentNodeID();
                String parentEmployeeId = treeLink.getParentEmployeeID();

                if (!employeeDataMap.containsKey(childNodeID)) {
                    throw new IllegalArgumentException("Unknown child node ID: " + childNodeID);
                }
                if (!employeeDataMap.containsKey(parentNodeID)) {
                    throw new IllegalArgumentException("Unknown parent node ID: " + parentNodeID);
                }
                if (linkedParentNodes.containsKey(parentNodeID)) {
                    throw new UnsupportedOperationException("Severe problem: the data pulled in by node '" + childNodeID + "' is supposed to hook into '" + parentNodeID + "' which was already hooked into the tree as child of '" + linkedParentNodes.get(parentNodeID) + "' - this leads to an incomplete tree");
                }

                //.... Now link the two together
                LOGGER.log(Level.INFO, "Linking child data for node ID {0} into {1} (employee {2})", new Object[]{childNodeID, parentNodeID, parentEmployeeId});
                EmployeeData<Employee> parentEmployeeData = employeeDataMap.get(parentNodeID);
                if (parentEmployeeData.getEmployee(parentEmployeeId) == null) {
                    throw new IllegalArgumentException("Parent employee ID not found: " + parentEmployeeId + " in parent node " + parentNodeID);
                }
                linkedParentNodes.put(childNodeID, parentNodeID);

                //.... Hook the root employee of the child into the desired parent employee
                Employee newParent = parentEmployeeData.getEmployee(parentEmployeeId);
                EmployeeData<Employee> childEmployeeData = employeeDataMap.get(childNodeID);
                childEmployeeData.getRootEmployee().setManager(newParent);
                newParent.addEmployee(childEmployeeData.getRootEmployee());

                //.... Copy over all child employees to the parent as well
                for (Employee employee : childEmployeeData.getEmployees()) {
                    //.... If the employee is already in there, chances are we're looking at caching importers
                    if (parentEmployeeData.getEmployee(employee.getID()) != null) {
                        LOGGER.log(Level.INFO, "Skipping copy process to parent tree for employee: {0}", employee.getID());
                        continue;
                    }
                    LOGGER.log(Level.INFO, "Copying employee to parent tree: {0} (parent importer ID: {1})", new Object[]{employee.getID(), parentNodeID});
                    parentEmployeeData.addEmployee(employee);
                }

                //.... Merge constraints
                parentEmployeeData.mergeConstraintHandler(childEmployeeData.getConstraintHandler());
            }

            if (!employeeDataMap.containsKey(rootNodeID)) {
                throw new IllegalArgumentException("Unknown root node ID: " + rootNodeID);
            }

            LOGGER.log(Level.INFO, "Finished tree assembly for root node {0}", rootNodeID);

            output = employeeDataMap.get(rootNodeID);

            //.... In this case, there is no tree to assemble, then only one node can be there
        } else {

            LOGGER.log(Level.INFO, "No tree to assemble defined");

            if (employeeDataMap.size() > 1) {
                throw new UnsupportedOperationException("As no tree linkage information is provided, there can be only one node providing employee data. Here there are " + employeeDataMap.size() + " nodes being used");
            }
            //.... Less than elegant ... 
            EmployeeData<Employee> employeeData = null;
            for (EmployeeData<Employee> e : employeeDataMap.values()) {
                employeeData = e;
            }

            output = employeeData;

        }

        return output;
    }

}
