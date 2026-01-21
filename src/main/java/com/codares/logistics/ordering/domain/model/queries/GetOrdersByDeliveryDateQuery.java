package com.codares.logistics.ordering.domain.model.queries;

import java.time.LocalDate;

/**
 * Query para obtener pedidos por fecha de entrega.
 * <p>
 * Este es un DTO de lectura que representa la intención de consultar
 * todos los pedidos programados para una fecha de entrega específica.
 * </p>
 *
 * @param deliveryDate Fecha de entrega a consultar.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record GetOrdersByDeliveryDateQuery(LocalDate deliveryDate) {
}
