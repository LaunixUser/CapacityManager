package org.ml.capman;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.ml.table.content.UrlContent;

/**
 * This class hold all information for a given employee
 *
 * @author mlaux
 */
public class Employee extends DataContainer {

    private final String ID;
    private final EmployeeCapacity capacity = new EmployeeCapacity();
    private boolean root = false;
    private Employee manager;
    private final Map<String, Employee> employees = new TreeMap<>();
    private IType nameType;
    private IType sortNameType;
    private final Map<EmployeeUrl, UrlContent> urls = new HashMap<>();

    /**
     * Types of URL data that can be stored for an employee. Currently, this is
     * not extensible or configurable.
     */
    public enum EmployeeUrl {
        //.... URL data that can be used when the name of the employee is used / displayed to point to the actual data for the employee
        DATA_NAME,
        //.... URL data that can be used when the ID of the employee is used / displayed to point to the actual data for the employee
        DATA_ID,
        //.... URL data that can be used when just some placeholder string is used / displayed to point to the actual data for the employee
        DATA_X,
        //.... URL data that can be used when the name of the employee is used / displayed to point to the org chart for the employee
        ORGA_NAME,
        //.... URL data that can be used when the ID of the employee is used / displayed to point to the org chart for the employee
        ORGA_ID,
        //.... URL data that can be used when just some placeholder string is used / displayed to point to the org chart for the employee
        ORGA_X
    }

    /**
     * @param ID
     */
    public Employee(String ID) {
        this.ID = ID.replaceAll("\\s+", "_").replaceAll("\\.", "_");
        for (EmployeeUrl url : EmployeeUrl.values()) {
            urls.put(url, new UrlContent("", ID));
        }
    }   

    /**
     * @return
     */
    public EmployeeCapacity getCapacity() {
        return capacity;
    }

    /**
     * @param employeeUrl
     * @param urlContent
     */
    public void setUrl(EmployeeUrl employeeUrl, UrlContent urlContent) {
        if (employeeUrl == null) {
            throw new NullPointerException("employeeUrl may not be null");
        }
        if (urlContent == null) {
            throw new NullPointerException("urlContent may not be null");
        }
        urls.put(employeeUrl, urlContent);
    }

    /**
     * @param employeeUrl
     * @return
     */
    public UrlContent getUrl(EmployeeUrl employeeUrl) {
        if (employeeUrl == null) {
            throw new NullPointerException("employeeUrl may not be null");
        }
        return urls.get(employeeUrl);
    }

    /**
     * Set the IType which holds the name for the employee
     *
     * @param nameType The IType to be used to identify the employee by name. If
     * the sortNameType is null (i. e. undefined) when this method is invoked,
     * it is set to the same IType value as a reasonable default, therefore in
     * the case both ITypes should be equal, a separate call to
     * setSortNameType() is not actually necessary
     */
    public void setNameType(IType nameType) {
        if (nameType == null) {
            throw new NullPointerException("nameType may not be null");
        }
        this.nameType = nameType;
        if (sortNameType == null) {
            this.sortNameType = nameType;
        }
    }

    /**
     * Set the IType which is to be used for sorting this employee
     *
     * @param sortNameType
     */
    public void setSortNameType(IType sortNameType) {
        if (sortNameType == null) {
            throw new NullPointerException("sortNameType may not be null");
        }
        this.sortNameType = sortNameType;
    }

    /**
     * @param capacityType
     * @return
     */
    public double getCapacity(EmployeeCapacity.CapacityType capacityType) {
        if (capacityType == null) {
            throw new NullPointerException("capacityType may not be null");
        }
        return capacity.get(capacityType);
    }

    /**
     * @param capacityType
     * @param value
     */
    public void setCapacity(EmployeeCapacity.CapacityType capacityType, double value) {
        if (capacityType == null) {
            throw new NullPointerException("capacityType may not be null");
        }
        capacity.set(capacityType, value);
    }

    /**
     * @return
     */
    public String getID() {
        return ID;
    }

    /**
     * @return
     */
    public Comparable getName() {
        if (nameType == null) {
            throw new UnsupportedOperationException("NameType has not been set");
        }
        return get(nameType);
    }

    /**
     * @return
     */
    public Comparable getSortName() {
        if (sortNameType == null) {
            throw new UnsupportedOperationException("SortNameType has not been set");
        }
        return get(sortNameType);
    }

    /**
     * The size of the organisation (i. e. all reports)
     *
     * @return
     */
    public int organisationSize() {
        if (employees.isEmpty()) {
            return 0;
        } else {
            int count = getEmployees().size();
            for (Employee employee : getEmployees().values()) {
                count += employee.organisationSize();
            }
            return count;
        }
    }

    /**
     * The HC number of the organisation, taking into account that for some of
     * the staff, HC = 0
     *
     * @return
     */
    public int organisationSizeByHC() {
        if (employees.isEmpty()) {
            return 0;
        } else {
            int count = 0;
            for (Employee employee : getEmployees().values()) {
                if (employee.getCapacity().getAsInt(EmployeeCapacity.CapacityType.HC) == 1) {
                    count++;
                }
            }
            for (Employee employee : getEmployees().values()) {
                count += employee.organisationSizeByHC();
            }
            return count;
        }
    }

    /**
     * @return the manager
     */
    public Employee getManager() {
        return manager;
    }

    /**
     * @return
     */
    public boolean hasManager() {
        return manager != null;
    }

    /**
     * @param manager the manager to set
     */
    public void setManager(Employee manager) {
        if (manager == null) {
            throw new IllegalArgumentException("manager may not be null");
        }
        this.manager = manager;
    }

    /**
     * @return the staff
     */
    public Map<String, Employee> getEmployees() {
        return employees;
    }

    /**
     * Add a direct report for this employee
     *
     * @param employee
     */
    public void addEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("employee may not be null");
        }
        employees.put(employee.getID(), employee);
        employee.setManager(this);
    }

    /**
     * @return the root
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(boolean root) {
        this.root = root;
    }

    /**
     * @return
     */
    public String getReportingHierarchy() {
        Employee p = this;
        StringBuilder hierarchy = new StringBuilder(p.getID());
        while (p.hasManager()) {
            hierarchy.insert(0, p.getManager().getID() + " <- ");
            p = p.getManager();
        }
        return hierarchy.toString();
    }

    /**
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Employee)) {
            return false;
        }
        return getSortSignature().equals(((Employee) other).getSortSignature());
    }

    /**
     * @return
     */
    @Override
    public int hashCode() {
        return getSortSignature().hashCode();
    }

    /**
     * @return
     */
    String getSortSignature() {
        return getSortName() + "-" + getID();
    }

}
