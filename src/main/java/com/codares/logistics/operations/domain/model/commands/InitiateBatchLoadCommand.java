package com.codares.logistics.operations.domain.model.commands;

public record InitiateBatchLoadCommand(
    String idempotencyKey, 
    String fileHash
) {}
