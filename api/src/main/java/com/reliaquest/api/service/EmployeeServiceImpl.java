package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeCreationRequest;
import com.reliaquest.api.dto.EmployeeDeleteRequest;
import com.reliaquest.api.exception.CustomResponseErrorHandler;
import com.reliaquest.api.exception.TooManyRequestsException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.model.EmployeesResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// NOTE: we limit to 5 attempts each 90 seconds to be in range. this is the worst case scenario.
// We could relax these conditions, but we could get the exception.
// This is applied to all public methods in this class as most of them make use of getAllEmployees() method
@Retryable(
        maxAttempts = 5,
        value = {TooManyRequestsException.class},
        backoff = @Backoff(delay = 90000))
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final RestTemplate restTemplate;

    @Value("${app.employeeAPI.url}") // Property initialized via application.yml to avoid harcoded values
    private String employeeAPIURL;

    public EmployeeServiceImpl() {
        this.restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
    }

    // NOTE: Constructor for testing. This is a work around due to the limited access to resTemplate Instance
    public EmployeeServiceImpl(RestTemplate restTemplate, String employeeAPIURL) {
        this.restTemplate = restTemplate;
        this.employeeAPIURL = employeeAPIURL;
    }

    /**
     * This is one of the most important service methods most other methods use it.
     *
     * This is a basic implementation, but it could be improved using some kind of cache.
     * Either @Cacheable or by keeping the last retrieved data and provide it when the server is busy.
     * So, we could attenuate the issues when the server is saturated
     * and reduce the number of calls to the server API
     *
     * @return List<Employee list of Employee from the server
     */
    public List<Employee> getAllEmployees() {
        final EmployeesResponse response = restTemplate.getForObject(employeeAPIURL, EmployeesResponse.class);
        return response != null ? response.data() : new ArrayList<>();
    }

    /**
     * Filters the employees list and returns all Employees exactly matching the provided searchString.
     *
     * @param searchString - String
     * @return - List<Employee> containing the matching Employees
     */
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        return getAllEmployees().stream()
                .filter(employee -> employee.employee_name().equals(searchString))
                .toList();
    }

    /**
     * This method return the Employee matching the provided id if it exists or null otherwise.
     *
     * WARNING: The server only supports ids with UUID format.
     * Otherwise, an exception occurs on its side.
     * That exception is correctly handled and the call will proceed as if not Employee was found.
     *
     * @param id - Employee id as an String
     * @return - Employee matching the id or null otherwise.
     */
    public Employee getEmployeeById(String id) {
        final EmployeeResponse response =
                restTemplate.getForObject(employeeAPIURL + "/{id}", EmployeeResponse.class, id);
        return response == null ? null : response.data();
    }

    /**
     * This method filters the list of employees searching for the highest salaried one
     *
     * @return - The highest found salary or an empty optional integer if there are no Employees
     */
    public Optional<Integer> getHighestSalaryOfEmployees() {
        return getAllEmployees().stream().map(Employee::employee_salary).max(Integer::compareTo);
    }

    /**
     *  This method filters the list of employees searching for the top 10 highest salaried ones.
     *
     *  NOTE: that the list could be empty or contain less than ten values if the server has lees than 10 employees.
     *  Note also that the returned list of names is provided in order from the highest earner to the lowest one.
     *
     *  WARNING: This implementation could return unexpected results in the following scenario:
     *
     *  Imagine that we have more than one Employee with the exact same salary,
     *  and they end up on positions 10 and 11 after being compared by salary.
     *  On that case, the filter will arbitrarily choose one of then.
     *
     *  A solution will be to provide and additional sorting base on other field.
     *  But this will not prevent the issue unless the field value is unique.
     *
     * @return - A List containing the names of the highest salaried Employees
     */
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return getAllEmployees().stream()
                .sorted(Comparator.comparingInt(Employee::employee_salary).reversed())
                .limit(10)
                .map(Employee::employee_name)
                .toList();
    }

    /**
     * This method always creates the employee unless the EmployeeAPI is down.
     *  So, no need to check for null returned data as any uncontroller exception will be handled by
     *  GlobalExceptionHandler class.
     *
     * @param employeeInput - EmployeeCreationRequest
     * @return - new Employee instance
     */
    public Employee createEmployee(EmployeeCreationRequest employeeInput) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<EmployeeCreationRequest> request = new HttpEntity<>(employeeInput, headers);

        return restTemplate
                .postForEntity(employeeAPIURL, request, EmployeeResponse.class)
                .getBody()
                .data();
    }

    /**
     * This method call the server API that deletes an Employee.
     * But first, it verified is the user exists.
     *
     * Note that due to Server specification we need to create and send a specific DTO
     * EmployeeDeleteRequest, instead of simply pass the Employee name.
     *
     * Due to this we need to use exchange method instead of the usual delete one.
     * As we are providing a body for the request and delete implementation does not support this.
     *
     * WARNING: There is a unwanted lateral effect/bug due to the server implementation.
     * If there are more than one Employee with different ids and the same name.
     * The server will delete the first one it finds, that could not necessarily be the correct one.
     *
     * In order to fix this we should include the Employee id on the EmployeeDeleteRequest and modify
     * the server implementation to take this into account it.
     *
     * Alternatively we could create different implementations on the server and return appropriate responses.
     *
     * @param id - employee id
     * @return - The employee name as a string if it has been deleted, null otherwise
     */
    public String deleteEmployeeById(String id) {
        final Employee result = getEmployeeById(id);
        if (result == null) {
            return null;
        } else {

            final String name = result.employee_name();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            final HttpEntity<EmployeeDeleteRequest> requestEntity =
                    new HttpEntity<>(new EmployeeDeleteRequest(name), headers);

            // NOTE: we use exchange with DELETE method as delete with a body is not supported by RestTemplate
            restTemplate.exchange(employeeAPIURL, HttpMethod.DELETE, requestEntity, Void.class);
            return name;
        }
    }
}
