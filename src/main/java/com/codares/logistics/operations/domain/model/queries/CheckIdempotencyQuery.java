package com.codares.logistics.operations.domain.model.queries;

/**
 * Query para verificar si existe un registro de idempotencia.
 * <p>
 * Se usa antes de procesar un archivo para evitar reprocesos.
 * </p>
 *
 * @param idempotencyKey clave de idempotencia del header
 * @param fileHash hash SHA-256 del archivo
 */
public record CheckIdempotencyQuery(
    String idempotencyKey,
    String fileHash
) {}
