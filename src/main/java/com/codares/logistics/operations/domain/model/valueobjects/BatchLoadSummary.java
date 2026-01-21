package com.codares.logistics.operations.domain.model.valueobjects;

import java.util.UUID;

/**
 * Value Object que contiene el resumen estadístico del procesamiento de batch.
 * <p>
 * Representa el resultado del orquestador con estadísticas del procesamiento.
 * NO es Result<T>. Result<T> es un Value Object monádico para acumular errores.
 * BatchLoadSummary es un VO que SIEMPRE contiene estadísticas del procesamiento.
 * </p>
 * <p>
 * Es agnóstico a HTTP: puede usarse en cualquier contexto (API, eventos, etc.).
 * </p>
 *
 * @param batchLoadId ID del lote procesado
 * @param totalRows cantidad total de filas procesadas
 * @param successCount cantidad de filas exitosas
 * @param failureCount cantidad de filas con error
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record BatchLoadSummary(
    UUID batchLoadId,
    int totalRows,
    int successCount,
    int failureCount
) {
    /**
     * Calcula la cantidad de errores.
     * @return totalRows - successCount
     */
    public int errorCount() {
        return totalRows - successCount;
    }
}
