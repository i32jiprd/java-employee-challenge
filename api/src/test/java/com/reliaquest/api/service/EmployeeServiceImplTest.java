package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.reliaquest.api.dto.EmployeeCreationRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.model.EmployeesResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

/**
 * This test suite exercises the methods from EmployeeServiceImpl
 * There are no abstract class or default implementations on the interface EmployeeService
 * Then we only need this test case file.
 */
class EmployeeServiceImplTest {

    private static final String NAME_1 = "John";
    private static final String NAME_2 = "David";
    private static final String NAME_3 = "Mary";
    private static final String NAME_4 = "Jane";
    private static final int SALARY_999 = 999;
    private static final int SALARY_123 = 123;
    private static final int SALARY_111 = 111;
    private static final int SALARY_333 = 333;
    private static final String USER_ID_1 = "8434ca78-fe18-4718-ab6f-b62ed53a11bd";
    private static final String USER_ID_2 = "4525ca31-xv12-3412-gj9w-4r56ui56nj7f";
    private static final String USER_ID_3 = "61ea8535-e727-4cb8-8338-f21fec11de7d";
    private static final String USER_ID_4 = "ba62125a-e646-4652-8433-8c1d6a36ea6e";
    private static final String HIGH_EARNER_1 = "HighEarner_1";
    private static final String HIGH_EARNER_2 = "HighEarner_2";
    private static final String HIGH_EARNER_3 = "HighEarner_3";
    private static final String HIGH_EARNER_4 = "HighEarner_4";
    private static final String RESPONSE_STATUS = "All good";

    private static final Employee EMPLOYEE_1 =
            new Employee(USER_ID_1, NAME_1, SALARY_999, 30, "Engineer", "john@example.com");
    private static final Employee EMPLOYEE_2 =
            new Employee(USER_ID_2, NAME_2, SALARY_123, 18, "Artist", "david@example.com");

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        // We need to keep track if this in order to properly close the resource if needed.
        mocks = MockitoAnnotations.openMocks(this);

        // Inject mock RestTemplate and absolute URL via constructor
        employeeService = new EmployeeServiceImpl(restTemplate, "http://dummy-api/employees");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void getAllEmployees_whenThereIsNoEmployees_thenReturnEmptyList() {
        // given
        final List<Employee> list = List.of();
        final EmployeesResponse response = new EmployeesResponse(RESPONSE_STATUS, list);
        when(restTemplate.getForObject(anyString(), any())).thenReturn(response);

        // when
        final List<Employee> result = employeeService.getAllEmployees();
        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllEmployees_whenThereAreEmployees_thenReturnPopulatedList() {
        // given
        final EmployeesResponse mockResponse = new EmployeesResponse(RESPONSE_STATUS, List.of(EMPLOYEE_1, EMPLOYEE_2));

        // when
        when(restTemplate.getForObject(anyString(), eq(EmployeesResponse.class)))
                .thenReturn(mockResponse);

        final List<Employee> result = employeeService.getAllEmployees();

        // then
        assertEquals(2, result.size());
        assertSame(EMPLOYEE_1, result.get(0));
        assertSame(EMPLOYEE_2, result.get(1));
    }

    @Test
    void getEmployeesByNameSearch_whenThereAreNoMatchingNames_thenReturnEmptyList() {
        final List<Employee> list = List.of();

        // when
        final EmployeesResponse response = new EmployeesResponse(RESPONSE_STATUS, list);
        when(restTemplate.getForObject(anyString(), any())).thenReturn(response);
        // when
        final List<Employee> result = employeeService.getEmployeesByNameSearch(NAME_1);
        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeesByNameSearch_whenThereAreMatchingNames_thenReturnPopulatedList() {
        // given
        final List<Employee> list = List.of(EMPLOYEE_1);
        final EmployeesResponse response = new EmployeesResponse(RESPONSE_STATUS, list);

        // when
        when(restTemplate.getForObject(anyString(), any())).thenReturn(response);

        final List<Employee> result = employeeService.getEmployeesByNameSearch(NAME_1);

        // then
        assertEquals(1, result.size());
        assertSame(EMPLOYEE_1, result.get(0));
    }

    @Test
    void getEmployeeById_whenThereIsNoMatchingEmployee_thenReturnNull() {
        // given
        // when
        when(restTemplate.getForObject(anyString(), any(), anyString())).thenReturn(null);
        final Employee result = employeeService.getEmployeeById(USER_ID_1);

        // then
        assertNull(result);
    }

    @Test
    void getEmployeeById_whenThereIsMatchingEmployee_thenReturnEmployee() {
        // given
        final EmployeeResponse response = new EmployeeResponse(RESPONSE_STATUS, EMPLOYEE_1);

        // when
        when(restTemplate.getForObject(anyString(), eq(EmployeeResponse.class), anyString()))
                .thenReturn(response);
        final Employee result = employeeService.getEmployeeById(USER_ID_1);

        // then
        assertSame(EMPLOYEE_1, result);
    }

    @Test
    void getHighestSalaryOfEmployees_whenThereIsNoEmployees_thenReturnEmptyList() {
        // given
        final List<Employee> list = List.of();

        // when
        final EmployeesResponse mockResponse = new EmployeesResponse(RESPONSE_STATUS, list);
        when(restTemplate.getForObject(anyString(), eq(EmployeesResponse.class)))
                .thenReturn(mockResponse);

        final Optional<Integer> result = employeeService.getHighestSalaryOfEmployees();

        // then
        assertFalse(result.isPresent());
    }

    @Test
    void getHighestSalaryOfEmployees_whenThereAreEmployees_thenReturnExpectedSalaryValue() {
        // given
        final List<Employee> list = List.of(EMPLOYEE_1, EMPLOYEE_2);

        // when
        final EmployeesResponse mockResponse = new EmployeesResponse(RESPONSE_STATUS, list);
        when(restTemplate.getForObject(anyString(), eq(EmployeesResponse.class)))
                .thenReturn(mockResponse);

        final Optional<Integer> result = employeeService.getHighestSalaryOfEmployees();

        // then
        assertTrue(result.isPresent());
        assertEquals(SALARY_999, result.get());
        assertEquals(SALARY_999, EMPLOYEE_1.employee_salary());
        assertTrue(EMPLOYEE_1.employee_salary() > EMPLOYEE_2.employee_salary());
    }

    @Test
    void getHighestSalaryOfEmployees_whenThereAreNoEmployees_thenReturnEmptyList() {
        // given
        final List<Employee> highEarnerList = List.of();

        // when
        final EmployeesResponse mockResponse = new EmployeesResponse(RESPONSE_STATUS, highEarnerList);
        when(restTemplate.getForObject(anyString(), eq(EmployeesResponse.class)))
                .thenReturn(mockResponse);
        final List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_whenThereAreEmployees_thenReturnNAmeListInExpectedOrder() {
        // given
        final Employee employee1 =
                new Employee(USER_ID_1, HIGH_EARNER_1, SALARY_999, 29, "Lead1", "HighEarner1@example.com");
        final Employee employee2 =
                new Employee(USER_ID_2, HIGH_EARNER_2, SALARY_123, 30, "Lead2", "HighEarner2@example.com");
        final Employee employee3 =
                new Employee(USER_ID_3, HIGH_EARNER_3, SALARY_111, 31, "Lead3", "HighEarner3@example.com");
        final Employee employee4 =
                new Employee(USER_ID_4, HIGH_EARNER_4, SALARY_333, 31, "Lead3", "HighEarner3@example.com");

        final List<Employee> highEarnerList = List.of(employee1, employee2, employee3, employee4);

        // when
        final EmployeesResponse mockResponse = new EmployeesResponse(RESPONSE_STATUS, highEarnerList);
        when(restTemplate.getForObject(anyString(), eq(EmployeesResponse.class)))
                .thenReturn(mockResponse);

        final List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        // then
        final List<String> reorderedBySalaryList = List.of(HIGH_EARNER_1, HIGH_EARNER_4, HIGH_EARNER_2, HIGH_EARNER_3);
        assertEquals(reorderedBySalaryList, result);
        assertTrue(employee1.employee_salary() > employee4.employee_salary());
        assertTrue(employee4.employee_salary() > employee2.employee_salary());
        assertTrue(employee2.employee_salary() > employee3.employee_salary());
    }

    @Test
    void testCreateEmployee_success() {
        // given
        final EmployeeCreationRequest request = new EmployeeCreationRequest(NAME_4, 7000, 29, "Lead");
        final Employee newEmployee = new Employee(USER_ID_3, NAME_4, SALARY_333, 29, "Lead", "jane@example.com");

        final EmployeeResponse response = new EmployeeResponse(RESPONSE_STATUS, newEmployee);
        final ResponseEntity<EmployeeResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        // when
        when(restTemplate.postForEntity(anyString(), any(), eq(EmployeeResponse.class)))
                .thenReturn(responseEntity);

        final Employee result = employeeService.createEmployee(request);

        // then
        assertNotNull(result);
        assertEquals(newEmployee.id(), result.id());
        assertEquals(newEmployee.employee_name(), result.employee_name());
        assertEquals(newEmployee.employee_email(), result.employee_email());
        assertEquals(newEmployee.employee_title(), result.employee_title());
        assertEquals(newEmployee.employee_salary(), result.employee_salary());
        assertEquals(newEmployee.employee_age(), result.employee_age());
    }

    @Test
    void deleteEmployeeById_whenFound_thenDeleteAndReturnEmployeeName() {
        // given
        final Employee newEmployee = new Employee(USER_ID_3, NAME_3, SALARY_111, 33, "Sr Dev", "Mary@example.com");
        final EmployeeResponse response = new EmployeeResponse(RESPONSE_STATUS, newEmployee);

        // when
        when(restTemplate.getForObject(anyString(), eq(EmployeeResponse.class), anyString()))
                .thenReturn(response);

        // then
        final String result = employeeService.deleteEmployeeById(USER_ID_3);
        assertEquals(NAME_3, result);
    }

    @Test
    void deleteEmployeeById_whenNotFound_thenDoNothingAndReturnNull() {
        // given
        final List<Employee> list = List.of(EMPLOYEE_1, EMPLOYEE_2);
        final EmployeesResponse mockResponse = new EmployeesResponse(RESPONSE_STATUS, list);

        // when
        when(restTemplate.getForObject(anyString(), eq(EmployeesResponse.class)))
                .thenReturn(mockResponse);

        // then
        final String deletedName = employeeService.deleteEmployeeById(USER_ID_3);

        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Void.class));

        assertNull(deletedName);
    }
}
