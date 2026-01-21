package com.codares.logistics.operations.domain.model.valueobjects;

import java.util.UUID;

/**
 * Value Object que representa el resultado del procesamiento de una fila CSV.
 * <p>
 * Encapsula el resultado exitoso (ID del pedido creado) o fallido
 * (información del error para reporte).
 * </p>
 * <p>
 * Este VO es agnóstico a HTTP y puede usarse en cualquier capa.
 * </p>
 *
 * @param rowNumber número de fila procesada
 * @param orderId ID del pedido creado (null si falló)
 * @param success indica si la fila se procesó exitosamente
 * @param errorCode código de error tipificado (null si éxito)
 * @param errorMessage mensaje descriptivo del error (null si éxito)
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record CsvRowResult(
    int rowNumber,
    UUID orderId,
    boolean success,
    String errorCode,
    String errorMessage
) {

    /**
     * Crea un resultado exitoso para una fila.
     *
     * @param rowNumber número de fila
     * @param orderId ID del pedido creado
     * @return resultado exitoso
     */
    public static CsvRowResult success(int rowNumber, UUID orderId) {
        return new CsvRowResult(rowNumber, orderId, true, null, null);
    }

    /**
     * Crea un resultado fallido para una fila.
     *
     * @param rowNumber número de fila
     * @param errorCode código de error tipificado
     * @param errorMessage mensaje descriptivo
     * @return resultado fallido
     */
    public static CsvRowResult failure(int rowNumber, String errorCode, String errorMessage) {
        return new CsvRowResult(rowNumber, null, false, errorCode, errorMessage);
    }
}
