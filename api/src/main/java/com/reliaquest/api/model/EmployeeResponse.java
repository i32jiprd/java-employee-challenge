package com.reliaquest.api.model;

/**
 * This record models the response provided by the server API
 *
 * @param status - Status result
 * @param data - Employee DTO
 */
public record EmployeeResponse(String status, Employee data) {}
