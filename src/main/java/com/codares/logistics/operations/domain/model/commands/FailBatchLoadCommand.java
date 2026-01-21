package com.codares.logistics.operations.domain.model.commands;

import java.util.UUID;

/**
 * Command para marcar una carga batch como fallida.
 *
 * @param batchLoadId ID del BatchLoad a marcar como fallido
 */
public record FailBatchLoadCommand(
    UUID batchLoadId
) {}
