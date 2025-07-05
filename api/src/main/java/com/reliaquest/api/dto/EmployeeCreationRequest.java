package com.reliaquest.api.dto;

/**
 * This record provides an immutable dto that will be used to create new employees
 * The record provides built in validations.
 *
 * Note that a per specification we are not checking null values on the provided fields
 * But it could be a good idea to do so.
 * Ex: if (name == null || name.isBlank()) {
 *
 * Also note that even if email is a field present on the returned Employee entity,
 * it is not needed on this DTO as it is populated by the server.
 *
 * In case of an attribute validation exception the GlobalExceptionHandler will take care of it
 * and will display the IllegalArgumentException provided message.
 *
 */
public record EmployeeCreationRequest(String name, Integer salary, Integer age, String title) {

    public EmployeeCreationRequest {
        if (name.isBlank()) {
            throw new IllegalArgumentException("name can not be empty");
        }

        if (salary <= 0) {
            throw new IllegalArgumentException("salary must be greater than zero");
        }

        if (age < 16 || age > 75) {
            throw new IllegalArgumentException("age should be between 16 and 75");
        }

        if (title.isBlank()) {
            throw new IllegalArgumentException("title can not be empty");
        }
    }
}
