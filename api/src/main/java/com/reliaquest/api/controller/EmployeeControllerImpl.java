package com.reliaquest.api.controller;

import com.reliaquest.api.dto.EmployeeCreationRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Implementation using the template.
 * So, it can be replaced using other implementations for Entity and Input.
 */
@RestController
@Tag(name = "Employee Client API")
public class EmployeeControllerImpl implements IEmployeeController<Employee, EmployeeCreationRequest> {

    private final EmployeeService employeeService;

    public EmployeeControllerImpl(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     *
     *
     * @return ResponseEntity<List<Employee>>
     */
    @Operation(summary = "Get all employees from server", description = "this should return all employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * @param searchString - employee name
     * @return List of employees matching this name or not found response otherwise
     */
    @Operation(
            summary = "Get all employees matching the provided search string or Not Found Otherwise",
            description = "this should return all employees whose name contains or matches the string input provided")
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        final List<Employee> list = employeeService.getEmployeesByNameSearch(searchString);
        return list.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(list);
    }

    /**
     * @param id - employee id
     * @return - Employee if a match is found or not found response otherwise
     */
    @Operation(
            summary = "Get the employee matching the provided Id or Not Found response otherwise",
            description = "this should return a single employee")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        final Employee result = employeeService.getEmployeeById(id);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }

    /**
     * @return - The highest salary value or not found response otherwise
     */
    @Operation(
            summary = "Get the highlights salary from the server or Not Found response otherwise",
            description = "this should return a single integer indicating the highest salary of amongst all employees")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        final Optional<Integer> result = employeeService.getHighestSalaryOfEmployees();
        return result.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * @return - Name list of the 10 highest earners
     */
    @Operation(
            summary =
                    "Get a name list containing, up to 10, highest earners or and empty list if there are no employees",
            description = "this should return a list of the top 10 employees based off of their salaries")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(employeeService.getTopTenHighestEarningEmployeeNames());
    }

    /**
     * NOTE: This method takes a EmployeeCreationRequest and validate it following the app specifications before attempting to create it.
     *
     * @param employeeInput - EmployeeCreationRequest DTO containing the required data to create a new Employee
     * @return - An instance of the newly created employee
     */
    @Operation(
            summary = "Create a new employee and return it",
            description = "this should return a single employee, if created, otherwise error")
    public ResponseEntity<Employee> createEmployee(@RequestBody @Validated EmployeeCreationRequest employeeInput) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(employeeInput));
    }

    /**
     * @param id - Employee id
     * @return - If the user exist, it will return the user name or not found response otherwise
     */
    @Operation(
            summary =
                    "Delete an employee using the provided id. On sucess it will return the employee name. Otherwise will return a Not Found response",
            description = "this should delete the employee with specified id given, otherwise error")
    public ResponseEntity<String> deleteEmployeeById(String id) {
        final String result = employeeService.deleteEmployeeById(id);
        return result == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.status(HttpStatus.GONE).body(result);
    }
}
