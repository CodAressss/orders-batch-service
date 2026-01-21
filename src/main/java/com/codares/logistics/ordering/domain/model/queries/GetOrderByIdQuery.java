package com.codares.logistics.ordering.domain.model.queries;

import java.util.UUID;

/**
 * Query para obtener un pedido por su identificador único.
 * <p>
 * Este es un DTO de lectura que representa la intención de consultar
 * un pedido específico por su UUID. Es utilizado por el QueryService
 * correspondiente.
 * </p>
 *
 * @param orderId Identificador único del pedido a consultar.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record GetOrderByIdQuery(UUID orderId) {
}
