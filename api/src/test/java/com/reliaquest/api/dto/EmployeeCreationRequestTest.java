package com.reliaquest.api.dto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EmployeeCreationRequestTest {

    private static final String NAME_VALUE = "name1";
    private static final int SALARY_100 = 100;
    private static final int AGE_25 = 25;
    private static final String TITLE_VALUE = "title1";

    private static Stream<Arguments> EmployeeCreationRequestCreationExceptionCombinations() {
        return Stream.of(
                Arguments.of("", SALARY_100, AGE_25, TITLE_VALUE, "name can not be empty"),
                Arguments.of(NAME_VALUE, -10, AGE_25, TITLE_VALUE, "salary must be greater than zero"),
                Arguments.of(NAME_VALUE, 0, AGE_25, TITLE_VALUE, "salary must be greater than zero"),
                Arguments.of(NAME_VALUE, SALARY_100, 15, TITLE_VALUE, "age should be between 16 and 75"),
                Arguments.of(NAME_VALUE, SALARY_100, 76, TITLE_VALUE, "age should be between 16 and 75"),
                Arguments.of(NAME_VALUE, SALARY_100, AGE_25, "", "title can not be empty"));
    }

    @ParameterizedTest(name = "{index} name: {0} salary: {1} age: {2} title: {3} Expected Exception: {4}")
    @MethodSource("EmployeeCreationRequestCreationExceptionCombinations")
    void employeeCreationRequest_CreationWithException(
            String name, Integer salary, Integer age, String title, String exceptionMessage) {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> new EmployeeCreationRequest(name, salary, age, title));
        Assertions.assertEquals(exceptionMessage, exception.getMessage());
    }

    private static Stream<Arguments> EmployeeCreationRequestCreationNoExceptionCombinations() {
        return Stream.of(
                Arguments.of(NAME_VALUE, SALARY_100, 25, TITLE_VALUE),
                Arguments.of(NAME_VALUE, 1, AGE_25, TITLE_VALUE),
                Arguments.of(NAME_VALUE, 1, 16, TITLE_VALUE),
                Arguments.of(NAME_VALUE, 1, 75, TITLE_VALUE),
                Arguments.of(NAME_VALUE, 1, 75, TITLE_VALUE, null) // Note: Email can be null
                );
    }

    @ParameterizedTest(name = "{index} name: {0} salary: {1} age: {2} title: {3}")
    @MethodSource("EmployeeCreationRequestCreationNoExceptionCombinations")
    void employeeCreationRequest_CreationNoException(String name, Integer salary, Integer age, String title) {
        assertDoesNotThrow(() -> new EmployeeCreationRequest(name, salary, age, title));
    }

    private static Stream<Arguments> EmployeeCreationRequestCreationNullParamExceptionCombinations() {
        return Stream.of(
                Arguments.of(
                        null,
                        SALARY_100,
                        AGE_25,
                        TITLE_VALUE,
                        "Cannot invoke \"String.isBlank()\" because \"name\" is null"),
                Arguments.of(
                        NAME_VALUE,
                        null,
                        25,
                        TITLE_VALUE,
                        "Cannot invoke \"java.lang.Integer.intValue()\" because \"salary\" is null"),
                Arguments.of(
                        NAME_VALUE,
                        SALARY_100,
                        null,
                        TITLE_VALUE,
                        "Cannot invoke \"java.lang.Integer.intValue()\" because \"age\" is null"),
                Arguments.of(
                        NAME_VALUE,
                        SALARY_100,
                        AGE_25,
                        null,
                        "Cannot invoke \"String.isBlank()\" because \"title\" is null"));
    }

    @ParameterizedTest(name = "{index} name: {0} salary: {1} age: {2} title: {3} Expected Exception: {4}")
    @MethodSource("EmployeeCreationRequestCreationNullParamExceptionCombinations")
    void employeeCreationRequest_CreationNullParamExceptionCombinations(
            String name, Integer salary, Integer age, String title, String exceptionMessage) {
        final NullPointerException exception =
                assertThrows(NullPointerException.class, () -> new EmployeeCreationRequest(name, salary, age, title));
        Assertions.assertEquals(exceptionMessage, exception.getMessage());
    }
}
