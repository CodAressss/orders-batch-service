package com.codares.logistics.operations.domain.model.valueobjects;

import java.time.LocalDate;

/**
 * Value Object que representa los datos necesarios para crear un pedido.
 * <p>
 * Usado como DTO inmutable para transferir datos validados desde el
 * Domain Service hacia el puerto de salida (ExternalOrdersService).
 * </p>
 *
 * @param orderNumber número único del pedido
 * @param customerId ID del cliente
 * @param deliveryDate fecha de entrega
 * @param status estado del pedido (PENDIENTE, CONFIRMADO, ENTREGADO)
 * @param zoneId ID de la zona de entrega
 * @param requiresRefrigeration si requiere cadena de frío
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record OrderData(
    String orderNumber,
    String customerId,
    LocalDate deliveryDate,
    String status,
    String zoneId,
    boolean requiresRefrigeration
) {}
