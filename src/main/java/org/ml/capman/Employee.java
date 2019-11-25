/*
 * The MIT License
 *
 * Copyright 2019 Dr. Matthias Laux.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.ml.capman;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.ml.table.content.UrlContent;

/**
 * @author mlaux
 */
public class Employee extends DataContainer {

    private String ID = "";
    private EmployeeCapacity capacity = new EmployeeCapacity();
    private boolean root = false;
    private Employee manager = null;
    private final Map<String, Employee> employees = new TreeMap<>();
    private IType nameType = null;
    private IType sortNameType = null;
    private Map<EmployeeUrl, UrlContent> urls = new HashMap<>();

    /**
     *
     */
    public enum EmployeeUrl {
        dataName, dataID, dataX, orgaName, orgaID, orgaX
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
     * @param nameType
     */
    public void setNameType(IType nameType) {
        if (nameType == null) {
            throw new NullPointerException("nameType may not be null");
        }
        this.nameType = nameType;
    }

    /**
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
