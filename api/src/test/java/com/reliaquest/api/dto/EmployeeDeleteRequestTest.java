package com.reliaquest.api.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test suite for EmployeeDeleteRequest
 *
 * This is a very simple one, but taken into account the odd implementation
 * I prefer to create it as a placeholder for future changes on the request class.
 */
class EmployeeDeleteRequestTest {

    private static final String NAME_VALUE = "name1";

    @Test
    void employeeDeleteRequest_RecordCreation() {
        // given
        // when
        final EmployeeDeleteRequest request = new EmployeeDeleteRequest(NAME_VALUE);
        // then
        assertEquals(NAME_VALUE, request.name());
    }
}
