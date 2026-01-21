package com.codares.logistics.ordering.domain.model.queries;

/**
 * Query para obtener un pedido por su número de pedido.
 * <p>
 * Este es un DTO de lectura que representa la intención de consultar
 * un pedido específico por su número único (ej. P001).
 * </p>
 *
 * @param orderNumber Número del pedido a consultar.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record GetOrderByOrderNumberQuery(String orderNumber) {
}
