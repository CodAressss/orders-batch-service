package com.codares.logistics.ordering.domain.model.queries;

/**
 * Query para obtener pedidos por zona de entrega.
 * <p>
 * Este es un DTO de lectura que representa la intención de consultar
 * todos los pedidos asignados a una zona de entrega específica.
 * </p>
 *
 * @param zoneId Identificador de la zona de entrega a consultar.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record GetOrdersByZoneQuery(String zoneId) {
}
