package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeCreationRequest;
import com.reliaquest.api.dto.EmployeeDeleteRequest;
import com.reliaquest.api.exception.TooManyRequestsException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.model.EmployeesResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final String RESPONSE_STATUS_MESSAGE = "Response status: : {}";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WebClient webClient;

    public EmployeeServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * This is one of the most important service methods most other methods use it.
     *
     * <p>This is a basic implementation, but it could be improved using some kind of cache.
     * Either @Cacheable or by keeping the last retrieved data and provide it when the server is busy.
     * So, we could attenuate the issues when the server is saturated
     * and reduce the number of calls to the server API
     *
     * @return List<Employee list of Employee from the server
     */
    public List<Employee> getAllEmployees() {
        final EmployeesResponse response = webClient
                .get()
                .uri("")
                .retrieve()
                .onStatus(this::isTooManyRequests, this::handleTooManyRequests)
                .bodyToMono(EmployeesResponse.class)
                .retryWhen(getRetrySpec())
                .block();

        final List<Employee> result;
        String responseStatus = null;
        if (response != null) {
            result = response.data();
            responseStatus = response.status();
        } else {
            result = new ArrayList<>();
        }
        logger.info(RESPONSE_STATUS_MESSAGE, responseStatus);

        return result;
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
     * <p>WARNING: The server only supports ids with UUID format.
     * Otherwise, an exception occurs on its side.
     * That exception is correctly handled and the call will proceed as if not Employee was found.
     *
     * @param id - Employee id as a String
     * @return - Employee matching the id or null otherwise.
     */
    public Employee getEmployeeById(String id) {
        final EmployeeResponse response = webClient
                .get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(this::isTooManyRequests, this::handleTooManyRequests)
                .onStatus(this::isNotFound, clientResponse -> Mono.empty())
                .bodyToMono(EmployeeResponse.class)
                .retryWhen(getRetrySpec())
                .block();

        final Employee result;
        String responseStatus = null;
        if (response != null) {
            result = response.data();
            responseStatus = response.status();
        } else {
            result = null;
        }
        logger.info(RESPONSE_STATUS_MESSAGE, responseStatus);

        return result;
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
     *  <p>NOTE: that the list could be empty or contain less than ten values if the server has lees than 10 employees.
     *  Note also that the returned list of names is provided in order from the highest earner to the lowest one.
     *
     *  <p>WARNING: This implementation could return unexpected results in the following scenario:
     *
     *  <p>Imagine that we have more than one Employee with the exact same salary,
     *  and they end up on positions 10 and 11 after being compared by salary.
     *  On that case, the filter will arbitrarily choose one of then.
     *
     *  <p>A solution will be to provide and additional sorting base on other field.
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
     *  So, no need to check for null returned data as any uncontrolled exception will be handled by
     *  GlobalExceptionHandler class.
     *
     * @param employeeInput - EmployeeCreationRequest
     * @return - new Employee instance
     */
    public Employee createEmployee(EmployeeCreationRequest employeeInput) {
        return webClient
                .post()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(employeeInput)
                .retrieve()
                .bodyToMono(EmployeeResponse.class)
                .map(EmployeeResponse::data)
                .retryWhen(getRetrySpec())
                .block();
    }

    /**
     * This method call the server API that deletes an Employee.
     * But first, it verified is the user exists.
     *
     * <p>Note that due to Server specification we need to create and send a specific DTO
     * EmployeeDeleteRequest, instead of simply pass the Employee name.
     *
     * <p>Due to this we need to use exchange method instead of the usual delete one.
     * As we are providing a body for the request and delete implementation does not support this.
     *
     * <p>WARNING: There is an unwanted lateral effect/bug due to the server implementation.
     * If there are more than one Employee with different ids and the same name.
     * The server will delete the first one it finds, that could not necessarily be the correct one.
     *
     * <p>In order to fix this we should include the Employee id on the EmployeeDeleteRequest and modify
     * the server implementation to take this into account it.
     *
     * <p>Alternatively we could create different implementations on the server and return appropriate responses.
     *
     * @param id - employee id
     * @return - The employee name as a string if it has been deleted, null otherwise
     */
    public String deleteEmployeeById(String id) {
        final Employee result = getEmployeeById(id);
        if (result == null) {
            logger.info("Employee with id {} not found", id);
            return null;
        } else {

            final String name = result.employee_name();

            logger.info("Employee with id {} and name {} was found", id, name);

            webClient
                    .method(HttpMethod.DELETE)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(new EmployeeDeleteRequest(name))
                    .retrieve()
                    .toBodilessEntity()
                    .retryWhen(getRetrySpec())
                    .block(); // Blocks for completion (synchronous like original)

            return name;
        }
    }

    /**
     * Support method used to define the retry strategy when a tooMany retries status response is found.
     *
     * <p>we limit to 5 attempts each 90 seconds to be in range.
     * This is the worst case scenario.</p>
     *
     * @return RetryBackoffSpec
     */
    private static RetryBackoffSpec getRetrySpec() {
        int maxRetries = 10;
        Duration initialBackoff = Duration.ofSeconds(30);
        Duration maxBackoff = Duration.ofSeconds(90);

        return Retry.backoff(maxRetries, initialBackoff)
                .maxBackoff(maxBackoff)
                .jitter(0.5) // Adds 50% random jitter to avoid retry storms
                .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests)
                .doBeforeRetry(retrySignal -> System.out.printf(
                        "Retrying (%d/%d)... Reason: %s%n",
                        retrySignal.totalRetries() + 1, maxRetries, retrySignal.failure().getMessage()))
                .onRetryExhaustedThrow((spec, signal) ->
                        new TooManyRequestsException("Retries exhausted after " + maxRetries + " attempts"));
    }

    private boolean isTooManyRequests(HttpStatusCode httpStatusCode) {
        return httpStatusCode.value() == 429;
    }

    private boolean isNotFound(HttpStatusCode httpStatusCode) {
        return httpStatusCode.value() == 404;
    }

    private Mono<? extends Throwable> handleTooManyRequests(ClientResponse response) {
        System.out.println("Received 429 Too Many Requests. Will attempt retry.");
        return response.createException();
    }
}
