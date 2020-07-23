package org.ml.capman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <EMP>
 * @author mlaux
 */
public class EmployeeData<EMP extends Employee> {

    private final List<EMP> employees = new ArrayList<>();
    private final Map<String, EMP> employeesByID = new HashMap<>();
    private EMP rootEmployee = null;
    private ConstraintHandler constraintHandler = null;

    /**
     * @return
     */
    public List<? extends EMP> getEmployees() {
        return employees;
    }

    /**
     * @return
     */
    public ConstraintHandler getConstraintHandler() {
        if (constraintHandler == null) {
            constraintHandler = new ConstraintHandler();
        }
        return constraintHandler;
    }

    /**
     * Add constraints from another handler
     *
     * @param constraintHandler
     */
    public void addConstraintHandler(ConstraintHandler constraintHandler) {
        if (constraintHandler == null) {
            throw new IllegalArgumentException("constraintHandler may not be null");
        }
        this.constraintHandler = new ConstraintHandler(constraintHandler);
    }

    /**
     * @param ID
     * @return
     */
    public EMP getEmployee(String ID) {
        if (ID == null) {
            throw new IllegalArgumentException("ID may not be null");
        }
        return employeesByID.get(ID);
    }

    /**
     * @param employee
     */
    public void addEmployee(EMP employee) {
        if (employee == null) {
            throw new IllegalArgumentException("employee may not be null");
        }
        if (employeesByID.containsKey(employee.getID())) {
            throw new IllegalArgumentException("UUID violation: ID " + employee.getID() + " is already used");
        }
        employees.add(employee);
        employeesByID.put(employee.getID(), employee);
    }

    /**
     * @return the rootEmployee
     */
    public EMP getRootEmployee() {
        return rootEmployee;
    }

    /**
     * @param rootEmployee the rootEmployee to set
     */
    public void setRootEmployee(EMP rootEmployee) {
        if (rootEmployee == null) {
            throw new IllegalArgumentException("rootEmployee may not be null");
        }
        this.rootEmployee = rootEmployee;
    }
}
