package com.codares.logistics.ordering.domain.model.queries;

/**
 * Query para obtener pedidos por estado.
 * <p>
 * Este es un DTO de lectura que representa la intención de consultar
 * todos los pedidos que se encuentran en un estado específico
 * (PENDIENTE, CONFIRMADO, ENTREGADO).
 * </p>
 *
 * @param status Estado de los pedidos a consultar.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record GetOrdersByStatusQuery(String status) {
}
