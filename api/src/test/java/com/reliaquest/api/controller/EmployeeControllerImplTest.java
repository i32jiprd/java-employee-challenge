package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.dto.EmployeeCreationRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerImplTest {

    private static final String NAME_1 = "John";
    private static final String NAME_2 = "David";
    private static final String NAME_3 = "Jane";
    private static final int SALARY_999 = 999;
    private static final int SALARY_123 = 123;
    private static final int SALARY_111 = 111;
    private static final String USER_ID_1 = "8434ca78-fe18-4718-ab6f-b62ed53a11bd";
    private static final String USER_ID_2 = "4525ca31-xv12-3412-gj9w-4r56ui56nj7f";

    private static final Employee EMPLOYEE_1 =
            new Employee(USER_ID_1, NAME_1, SALARY_999, 30, "Engineer", "john@example.com");
    private static final Employee EMPLOYEE_2 =
            new Employee(USER_ID_2, NAME_2, SALARY_123, 18, "Artist", "david@example.com");

    private EmployeeService employeeService;
    private EmployeeControllerImpl controller;

    @BeforeEach
    void setup() {
        employeeService = mock(EmployeeService.class);
        controller = new EmployeeControllerImpl(employeeService);
    }

    @Test
    void getAllEmployees_whenThereAreNoEmployees_shouldReturnAndEmptyList() {
        // given
        final List<Employee> mockEmployees = List.of();

        // when
        when(employeeService.getAllEmployees()).thenReturn(mockEmployees);
        final ResponseEntity<List<Employee>> response = controller.getAllEmployees();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getAllEmployees_whenThereAreEmployees_shouldReturnPopulatedList() {
        // given
        final List<Employee> mockEmployees = List.of(EMPLOYEE_1, EMPLOYEE_2);

        // when
        when(employeeService.getAllEmployees()).thenReturn(mockEmployees);
        final ResponseEntity<List<Employee>> response = controller.getAllEmployees();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockEmployees, response.getBody());
    }

    @Test
    void getEmployeesByNameSearch_whenThereAreMatchingEmployees_shouldReturnPopulatedList() {
        // given
        final List<Employee> mockList = List.of(EMPLOYEE_1);

        // when
        when(employeeService.getEmployeesByNameSearch(NAME_1)).thenReturn(mockList);
        final ResponseEntity<List<Employee>> response = controller.getEmployeesByNameSearch(NAME_1);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockList, response.getBody());
    }

    @Test
    void getEmployeesByNameSearch_whenThereAreNoMatchingEmployees_shouldReturnNotFoundWhenEmpty() {
        // given
        // when
        when(employeeService.getEmployeesByNameSearch(NAME_3)).thenReturn(Collections.emptyList());
        final ResponseEntity<List<Employee>> response = controller.getEmployeesByNameSearch(NAME_3);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getEmployeeById_whenThereAreMatchingEmployees_shouldReturnEmployeeIfExists() {
        // given
        // when
        when(employeeService.getEmployeeById(USER_ID_1)).thenReturn(EMPLOYEE_1);
        final ResponseEntity<Employee> response = controller.getEmployeeById(USER_ID_1);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(EMPLOYEE_1, response.getBody());
    }

    @Test
    void getEmployeeById_shouldReturnNotFoundIfNull() {
        // given
        // when
        when(employeeService.getEmployeeById(USER_ID_1)).thenReturn(null);
        final ResponseEntity<Employee> response = controller.getEmployeeById(USER_ID_1);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getHighestSalaryOfEmployees_whenIsFound_shouldReturnSalary() {
        // given
        // when
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(Optional.of(SALARY_999));
        final ResponseEntity<Integer> response = controller.getHighestSalaryOfEmployees();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(SALARY_999, response.getBody());
    }

    @Test
    void getHighestSalaryOfEmployees_whenIsNotFound_shouldReturnNotFoundWhenEmpty() {
        // given
        // when
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(Optional.empty());
        final ResponseEntity<Integer> response = controller.getHighestSalaryOfEmployees();

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_whenThereAreEmployees_shouldReturnPopulatedList() {
        // given
        final List<String> mockNames = List.of("Alice", "Bob");

        // when
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(mockNames);
        final ResponseEntity<List<String>> response = controller.getTopTenHighestEarningEmployeeNames();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockNames, response.getBody());
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_whenThereAreNoEmployees_shouldReturnEmptyList() {
        // given
        final List<String> mockNames = List.of();

        // when
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(mockNames);
        final ResponseEntity<List<String>> response = controller.getTopTenHighestEarningEmployeeNames();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockNames, response.getBody());
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() {
        // given
        final EmployeeCreationRequest request = new EmployeeCreationRequest(NAME_1, SALARY_111, 34, "Junior Developer");
        final Employee mockEmployee = EMPLOYEE_1;

        // when
        when(employeeService.createEmployee(request)).thenReturn(mockEmployee);
        final ResponseEntity<Employee> response = controller.createEmployee(request);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockEmployee, response.getBody());
    }

    @Test
    void deleteEmployeeById_whenEmployeeExists_shouldReturnGoneWhenDeleted() {
        // given
        // when
        when(employeeService.deleteEmployeeById(USER_ID_1)).thenReturn(NAME_1);
        final ResponseEntity<String> response = controller.deleteEmployeeById(USER_ID_1);

        // then
        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals(NAME_1, response.getBody());
    }

    @Test
    void deleteEmployeeById_whenEmployeedoNotExists_shouldReturnNotFoundIfNull() {
        // given
        // when
        when(employeeService.deleteEmployeeById(USER_ID_1)).thenReturn(null);
        final ResponseEntity<String> response = controller.deleteEmployeeById(USER_ID_1);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
