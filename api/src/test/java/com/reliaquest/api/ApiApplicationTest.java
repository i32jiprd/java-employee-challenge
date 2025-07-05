package com.reliaquest.api;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeCreationRequest;
import com.reliaquest.api.model.Employee;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.employeeAPI.url=http://localhost:8112/api/v1/employee")
class ApiApplicationTest {

    /**
     * This is a test integration suite for the application.
     *  It is probably best implemented using Spock or Selenium, but in this case I use directly Junit
     *
     *  This test needs the server to be running prior to the test execution
     *  NOTE: Due to the server random retries policy I added a wait timer after each scenario
     *
     *  WARNING: In order to rerun these test we need to restart the server otherwise some assertions will fail.
     */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        try {
            System.out.println("Waiting to reset server request retries...");
            // I add this wait to control the server worst case scenario (5 requests in 90 seconds)
            Thread.sleep(1000);
            // Thread.sleep(90000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Continuing...");
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

        MvcResult result = mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<Employee> employeesOriginalList = objectMapper.readValue(json, new TypeReference<List<Employee>>() {});

        // We are going to exercise the API
        final Employee employee = employeesOriginalList.get(0);

        // We retrieve the fist employee and search it be name
        result = mockMvc.perform(
                        get("/search/{searchString}", employee.employee_name()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        json = result.getResponse().getContentAsString();
        List<Employee> resultByName = objectMapper.readValue(json, new TypeReference<List<Employee>>() {});

        assertEquals(1, resultByName.size());
        assertEquals(resultByName.get(0), employee);

        // We retrieve the fist employee and search it be id
        result = mockMvc.perform(get("/{id}", employee.id()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        json = result.getResponse().getContentAsString();
        Employee resultById = objectMapper.readValue(json, new TypeReference<Employee>() {});

        assertNotNull(resultById);
        assertEquals(resultById, employee);

        // We retrieve the highestSalary and compare with the original list of Employees
        result = mockMvc.perform(get("/highestSalary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        json = result.getResponse().getContentAsString();
        Integer highestSalary = objectMapper.readValue(json, new TypeReference<Integer>() {});
        Integer originalHighestSalary = employeesOriginalList.stream()
                .map(Employee::employee_salary)
                .max(Integer::compareTo)
                .orElse(null);

        Assertions.assertEquals(originalHighestSalary, highestSalary);

        // We retrieve the list of 10 high earners and compare with the original list of Employees
        result = mockMvc.perform(get("/topTenHighestEarningEmployeeNames").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        json = result.getResponse().getContentAsString();
        List<String> highEarnersList = objectMapper.readValue(json, new TypeReference<List<String>>() {});
        List<String> originalHighEarnersList = employeesOriginalList.stream()
                .sorted(Comparator.comparingInt(Employee::employee_salary).reversed())
                .limit(10)
                .map(Employee::employee_name)
                .toList();

        assertEquals(originalHighEarnersList, highEarnersList);
    }

    /**
     * This tests exercises the following end points
     *
     * @GetMapping()
     * @PostMapping()
     * @DeleteMapping("/{id}")
     * @GetMapping("/search/{searchString}")
     *
     */
    @Test
    void endtoEnScenario2() throws Exception {
        // we get the initial number of users
        MvcResult result = mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<Employee> employeesOriginalList = objectMapper.readValue(json, new TypeReference<List<Employee>>() {});

        final int originalNumberOfEmployees = employeesOriginalList.size();

        // We are going to exercise the API
        final Employee employee = employeesOriginalList.get(0);

        // We retrieve the fist employee and search it be name
        result = mockMvc.perform(delete("/{id}", employee.id()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone())
                .andReturn();

        // The number of employees has been reduced
        result = mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(originalNumberOfEmployees - 1)))
                .andReturn();

        // We try to remove it again and it is not found
        mockMvc.perform(delete("/{id}", employee.id()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // We add two new Employees with the same name
        // And we verified that they are created
        final String name = UUID.randomUUID().toString();
        final EmployeeCreationRequest request1 = new EmployeeCreationRequest(name, 3456, 44, "title1");
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        final EmployeeCreationRequest request2 = new EmployeeCreationRequest(name, 6789, 55, "title2");
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // We verify the number of employees has increased
        result = mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        json = result.getResponse().getContentAsString();
        List<Employee> employeesNewList = objectMapper.readValue(json, new TypeReference<List<Employee>>() {});

        assertEquals(originalNumberOfEmployees + 1, employeesNewList.size());

        result = mockMvc.perform(get("/search/{searchString}", name).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        json = result.getResponse().getContentAsString();
        List<Employee> resultByName = objectMapper.readValue(json, new TypeReference<List<Employee>>() {});

        assertEquals(2, resultByName.size());
    }
}
