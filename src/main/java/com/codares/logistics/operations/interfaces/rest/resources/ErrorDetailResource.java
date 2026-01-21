package com.codares.logistics.operations.interfaces.rest.resources;

/**
 * DTO que representa el detalle de un error en una fila específica del CSV.
 * <p>
 * Proporciona información suficiente para que el cliente identifique
 * y corrija el error en el archivo original.
 * </p>
 *
 * @param numeroLinea número de fila en el archivo CSV (1-indexed, sin contar header)
 * @param codigoError código de error tipificado (ej: CLIENTE_NO_ENCONTRADO)
 * @param mensaje     descripción legible del error
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record ErrorDetailResource(
    int numeroLinea,
    String codigoError,
    String mensaje
) {}
