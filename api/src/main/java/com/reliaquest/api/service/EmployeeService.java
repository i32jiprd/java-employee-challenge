package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeCreationRequest;
import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.Optional;

/**
 * Interface definition for the service
 */
public interface EmployeeService {

    List<Employee> getAllEmployees();

    List<Employee> getEmployeesByNameSearch(String searchString);

    Employee getEmployeeById(String id);

    Optional<Integer> getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    Employee createEmployee(EmployeeCreationRequest employeeInput);

    String deleteEmployeeById(String id);
}
