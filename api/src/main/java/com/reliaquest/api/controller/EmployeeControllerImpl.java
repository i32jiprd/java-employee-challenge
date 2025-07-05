package com.reliaquest.api.controller;

import com.reliaquest.api.dto.EmployeeCreationRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Implementation using template. So, it can be replaced using other implementations.
 */
@RestController
public class EmployeeControllerImpl implements IEmployeeController<Employee, EmployeeCreationRequest> {

    private final EmployeeService employeeService;

    public EmployeeControllerImpl(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * this should return all employees
     *
     * @return ResponseEntity<List<Employee>>
     */
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * this should return all employees whose name contains or matches the string input provided
     *
     * @param searchString - employee name
     * @return List of employees matching this name or not found response otherwise
     */
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        final List<Employee> list = employeeService.getEmployeesByNameSearch(searchString);
        return list.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(list);
    }

    /**
     * this should return a single employee
     *
     * @param id - employee id
     * @return - Employee if a match is found or not found response otherwise
     */
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        final Employee result = employeeService.getEmployeeById(id);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }

    /**
     * this should return a single integer indicating the highest salary of amongst all employees
     *
     * @return - The highest salary value or not found response otherwise
     */
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        final Optional<Integer> result = employeeService.getHighestSalaryOfEmployees();
        return result.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * this should return a list of the top 10 employees based off of their salaries
     *
     * @return - Name list of the 10 highest earners
     */
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(employeeService.getTopTenHighestEarningEmployeeNames());
    }

    /**
     * this should return a single employee, if created, otherwise error
     * This method takes a EmployeeCreationRequest and validate it following the app specifications before attempting to create it.
     *
     * @param employeeInput - EmployeeCreationRequest DTO containing the required data to create a new Employee
     * @return - An instance of the newly created employee
     */
    public ResponseEntity<Employee> createEmployee(@RequestBody @Validated EmployeeCreationRequest employeeInput) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(employeeInput));
    }

    /**
     * this should delete the employee with specified id given, otherwise error
     *
     * @param id - Employee id
     * @return - If the user exist, it will return the user name or not found response otherwise
     */
    public ResponseEntity<String> deleteEmployeeById(String id) {
        final String result = employeeService.deleteEmployeeById(id);
        return result == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.status(HttpStatus.GONE).body(result);
    }
}
