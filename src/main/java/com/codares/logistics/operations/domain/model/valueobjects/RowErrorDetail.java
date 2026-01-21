package com.codares.logistics.operations.domain.model.valueobjects;

public record RowErrorDetail(
    int rowNumber, 
    String errorCode, 
    String errorMessage
) {}


