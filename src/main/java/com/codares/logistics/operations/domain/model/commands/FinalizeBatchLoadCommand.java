package com.codares.logistics.operations.domain.model.commands;

import java.util.List;
import java.util.UUID;

import com.codares.logistics.operations.domain.model.valueobjects.RowErrorDetail;

public record FinalizeBatchLoadCommand(
    UUID batchLoadId,
    int totalProcessed,
    int successCount,
    List<RowErrorDetail> errors
) {
    
}
