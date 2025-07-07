package com.reliaquest.api.controller;

import com.reliaquest.api.dto.EmployeeCreationRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponse(
            responseCode = "200",
            description = "List of employees returned",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Employee.class))))
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
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Employee list returned",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        array = @ArraySchema(schema = @Schema(implementation = Employee.class)))),
                @ApiResponse(
                        responseCode = "404",
                        description = "No Employees found",
                        content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(
            @PathVariable @Parameter(name = "searchString", description = "Employee name", example = "Joe Denver")
                    String searchString) {
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
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Employee found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Employee.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Employee not found",
                        content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<Employee> getEmployeeById(
            @PathVariable
                    @Parameter(
                            name = "id",
                            description = "Employee id. It should be provided in UUID format",
                            example = "8434ca78-fe18-4718-ab6f-b62ed53a11bd")
                    String id) {
        final Employee result = employeeService.getEmployeeById(id);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }

    /**
     * @return - The highest salary value or not found response otherwise
     */
    @Operation(
            summary = "Get the highlights salary from the server or Not Found response otherwise",
            description = "this should return a single integer indicating the highest salary of amongst all employees")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Highest Salary Of Employees found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Integer.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Highest Salary Of Employees not found",
                        content = @Content(mediaType = "application/json"))
            })
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
    @ApiResponse(
            responseCode = "200",
            description = "List of Top Ten Highest Earning Employee Names returned",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = String.class))))
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
    @ApiResponse(
            responseCode = "201",
            description = "New employee with populated email returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Employee.class)))
    public ResponseEntity<Employee> createEmployee(
            @RequestBody
                    @Validated
                    @Parameter(
                            name = "employeeInput",
                            description =
                                    "Record containing all fields needed to create a new Employee. The values will be validated before creation",
                            example =
                                    "{\n  \"name\": \"Joe Denver\",\n  \"salary\":  483648,\n  \"age\":  66,\n  \"title\": \"Software Engineer\"\n}")
                    EmployeeCreationRequest employeeInput) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(employeeInput));
    }

    /**
     * @param id - Employee id
     * @return - If the employee exist, it will return the employee or not found response otherwise
     */
    @Operation(
            summary =
                    "Delete an employee using the provided id. On success it will return the employee name. Otherwise will return a Not Found response",
            description = "this should delete the employee with specified id given, otherwise error")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "410",
                        description = "Employee has been deleted",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = String.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Employee with provided id not found",
                        content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<String> deleteEmployeeById(
            @Parameter(name = "id", description = "Employee id", example = "8434ca78-fe18-4718-ab6f-b62ed53a11bd")
                    String id) {
        final String result = employeeService.deleteEmployeeById(id);
        return result == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.status(HttpStatus.GONE).body(result);
    }
}
