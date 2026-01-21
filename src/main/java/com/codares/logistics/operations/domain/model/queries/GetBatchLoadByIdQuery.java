package com.codares.logistics.operations.domain.model.queries;

import java.util.UUID;

/**
 * Query para obtener un BatchLoad por su ID.
 *
 * @param batchLoadId ID del BatchLoad
 */
public record GetBatchLoadByIdQuery(
    UUID batchLoadId
) {}
