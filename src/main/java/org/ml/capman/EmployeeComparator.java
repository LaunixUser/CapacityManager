package org.ml.capman;

import java.util.Comparator;

/**
 * @author osboxes
 */
public class EmployeeComparator implements Comparator<Employee> {

    /**
     * #
     *
     * @param employee1
     * @param employee2
     * @return
     */
    @Override
    public int compare(Employee employee1, Employee employee2) {
        if (employee1 == null) {
            throw new NullPointerException("employee1 may not be null");
        }
        if (employee2 == null) {
            throw new NullPointerException("employee2 may not be null");
        }
        return employee1.getSortSignature().compareTo(employee2.getSortSignature());
    }

}
