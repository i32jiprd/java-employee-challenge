package com.reliaquest.api.model;

import java.util.List;

/**
 * This record models the response provided by the server API when more than one Employee is returned
 *
 * @param status - Status result
 * @param data - Employee DTO List
 */
public record EmployeesResponse(String status, List<Employee> data) {}
