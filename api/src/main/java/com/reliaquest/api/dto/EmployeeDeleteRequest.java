package com.reliaquest.api.dto;

/**
 * This record provides an immutable dto that will be used to delete an existent Employee,
 *
 * Note that we do not need to validate the name value for null as it is populated from an API call
 */
public record EmployeeDeleteRequest(String name) {}
