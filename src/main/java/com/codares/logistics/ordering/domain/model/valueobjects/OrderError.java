package com.codares.logistics.ordering.domain.model.valueobjects;

import com.codares.logistics.shared.domain.model.valueobjects.BusinessError;

/**
 * Enum que define los códigos de error específicos del contexto de pedidos (Orders).
 * <p>
 * Implementa {@link BusinessError} para permitir el manejo tipificado de errores
 * en validaciones de dominio, excepciones y procesamiento batch.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public enum OrderError implements BusinessError {
    CLIENTE_NO_ENCONTRADO,
    CLIENTE_ID_INVALIDO,
    ZONA_INVALIDA,
    FECHA_ENTREGA_INVALIDA,
    ESTADO_INVALIDO,
    DUPLICADO,
    CADENA_FRIO_NO_SOPORTADA,
    NUMERO_PEDIDO_INVALIDO,
    IDEMPOTENCY_KEY_DUPLICADO
}