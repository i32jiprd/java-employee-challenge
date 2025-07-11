package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeCreationRequest;
import com.reliaquest.api.model.Employee;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = "webclientconfig.baseurl=http://localhost:8112/api/v1/employee")
class ApiApplicationTest {

    /**
     * This is a test integration suite for the application.
     *  It is probably best implemented using Spock or Selenium, but in this case I use directly Junit
     *
     *  <p>This test needs the server to be running prior to the test execution
     *  NOTE: Due to the server random retries policy I added a wait timer after each scenario</p>
     *
     *  <p>WARNING: In order to rerun these test we need to restart the server otherwise some assertions will fail.</p>
     */
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    void extendTimeout() {
        this.webTestClient = this.webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(90))
                .build();
    }

    /**
     * This tests exercises the following end points
     *
     *  @GetMapping()
     *  @GetMapping("/search/{searchString}")
     *  @GetMapping("/{id}")
     *  @GetMapping("/highestSalary")
     *  @GetMapping("/topTenHighestEarningEmployeeNames")
     *
     */
    @Test
    void endtoEnScenario1() throws Exception {

        // GET /
        String json = webTestClient
                .get()
                .uri("/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        List<Employee> employeesOriginalList = parseJson(json, new TypeReference<>() {});

        final Employee employee = employeesOriginalList.get(0);

        // GET /search/{searchString}
        json = webTestClient
                .get()
                .uri("/search/{searchString}", employee.employee_name())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        List<Employee> resultByName = parseJson(json, new TypeReference<>() {});
        assertEquals(1, resultByName.size());
        assertEquals(resultByName.get(0), employee);

        // GET /{id}
        json = webTestClient
                .get()
                .uri("/{id}", employee.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        Employee resultById = parseJson(json, new TypeReference<>() {});
        assertEquals(resultById, employee);

        // GET /highestSalary
        json = webTestClient
                .get()
                .uri("/highestSalary")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        Integer highestSalary = objectMapper.readValue(json, Integer.class);

        Integer originalHighestSalary = employeesOriginalList.stream()
                .map(Employee::employee_salary)
                .max(Integer::compareTo)
                .orElse(null);

        assertEquals(originalHighestSalary, highestSalary);

        // GET /topTenHighestEarningEmployeeNames
        json = webTestClient
                .get()
                .uri("/topTenHighestEarningEmployeeNames")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        List<String> highEarnersList = parseJson(json, new TypeReference<>() {});

        List<String> originalHighEarnersList = employeesOriginalList.stream()
                .sorted(Comparator.comparingInt(Employee::employee_salary).reversed())
                .limit(10)
                .map(Employee::employee_name)
                .toList();

        assertEquals(originalHighEarnersList, highEarnersList);
    }

    @Test
    void endtoEnScenario2() throws Exception {
        // GET /
        String json = webTestClient
                .get()
                .uri("/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        List<Employee> employeesOriginalList = parseJson(json, new TypeReference<>() {});
        int originalNumberOfEmployees = employeesOriginalList.size();

        final Employee employee = employeesOriginalList.get(0);

        // DELETE /{id}
        webTestClient
                .delete()
                .uri("/{id}", employee.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(410); // HTTP 410 Gone

        // GET / to verify size decreased
        webTestClient
                .get()
                .uri("/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Employee.class)
                .hasSize(originalNumberOfEmployees - 1);

        // DELETE again, expect 404 Not Found
        webTestClient
                .delete()
                .uri("/{id}", employee.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();

        // POST two new Employees with same name
        final String name = UUID.randomUUID().toString();

        final EmployeeCreationRequest request1 = new EmployeeCreationRequest(name, 3456, 44, "title1");
        webTestClient
                .post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request1)
                .exchange()
                .expectStatus()
                .isCreated();

        final EmployeeCreationRequest request2 = new EmployeeCreationRequest(name, 6789, 55, "title2");
        webTestClient
                .post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request2)
                .exchange()
                .expectStatus()
                .isCreated();

        // GET / to verify new size
        json = webTestClient
                .get()
                .uri("/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        List<Employee> employeesNewList = parseJson(json, new TypeReference<>() {});
        assertEquals(originalNumberOfEmployees + 1, employeesNewList.size());

        // GET /search/{searchString}
        json = webTestClient
                .get()
                .uri("/search/{searchString}", name)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        List<Employee> resultByName = parseJson(json, new TypeReference<>() {});
        assertEquals(2, resultByName.size());
    }

    private <T> T parseJson(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

}
