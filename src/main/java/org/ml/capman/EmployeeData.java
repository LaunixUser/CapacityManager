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
