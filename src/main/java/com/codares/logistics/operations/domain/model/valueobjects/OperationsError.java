package com.codares.logistics.operations.domain.model.valueobjects;

import com.codares.logistics.shared.domain.model.valueobjects.BusinessError;

/**
 * Códigos de error específicos del Bounded Context Operations.
 * <p>
 * Implementa {@link BusinessError} para integración con el
 * manejo de excepciones tipificadas del Shared Kernel.
 * </p>
 * <p>
 * Agrupa errores por categoría:
 * <ul>
 *   <li>Validación de entidades externas (cliente, zona)</li>
 *   <li>Validación de reglas de negocio (fecha, estado, refrigeración)</li>
 *   <li>Validación de unicidad (duplicados)</li>
 * </ul>
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public enum OperationsError implements BusinessError {

    // ─────────────────────────────────────────────────────────────
    // Errores de entidades externas
    // ─────────────────────────────────────────────────────────────

    /**
     * El cliente especificado no existe en el catálogo.
     */
    CLIENTE_NO_ENCONTRADO,

    /**
     * El cliente existe pero está inactivo.
     */
    CLIENTE_INACTIVO,

    /**
     * La zona de entrega especificada no existe.
     */
    ZONA_NO_ENCONTRADA,

    // ─────────────────────────────────────────────────────────────
    // Errores de reglas de negocio
    // ─────────────────────────────────────────────────────────────

    /**
     * La fecha de entrega es anterior a hoy (considerando timezone).
     */
    FECHA_ENTREGA_PASADA,

    /**
     * El estado del pedido no es válido.
     */
    ESTADO_INVALIDO,

    /**
     * El pedido requiere refrigeración pero la zona no la soporta.
     */
    CADENA_FRIO_NO_SOPORTADA,

    /**
     * El formato del número de pedido es inválido.
     */
    NUMERO_PEDIDO_INVALIDO,

    // ─────────────────────────────────────────────────────────────
    // Errores de unicidad
    // ─────────────────────────────────────────────────────────────

    /**
     * Ya existe un pedido con el mismo número.
     */
    PEDIDO_DUPLICADO,

    /**
     * El archivo ya fue procesado previamente (idempotencia).
     */
    ARCHIVO_YA_PROCESADO
}
