package com.codares.logistics.ordering.domain.model.queries;

/**
 * Query para obtener pedidos por cliente.
 * <p>
 * Este es un DTO de lectura que representa la intención de consultar
 * todos los pedidos realizados por un cliente específico.
 * </p>
 *
 * @param customerId Identificador del cliente a consultar.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record GetOrdersByCustomerQuery(String customerId) {
}
