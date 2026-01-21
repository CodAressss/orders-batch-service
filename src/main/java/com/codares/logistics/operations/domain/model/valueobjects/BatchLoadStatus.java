package com.codares.logistics.operations.domain.model.valueobjects;

/**
 * Enumeración que representa los estados posibles de una carga batch.
 * <p>
 * Define el ciclo de vida del procesamiento:
 * <ul>
 *   <li>{@code PROCESSING}: La carga se está procesando</li>
 *   <li>{@code COMPLETED}: La carga completó exitosamente</li>
 *   <li>{@code FAILED}: La carga falló durante el procesamiento</li>
 * </ul>
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public enum BatchLoadStatus {
    /**
     * La carga se encuentra en proceso de validación y persistencia.
     */
    PROCESSING,

    /**
     * La carga completó exitosamente (puede haber filas con error).
     */
    COMPLETED,

    /**
     * La carga falló durante el procesamiento.
     */
    FAILED
}
