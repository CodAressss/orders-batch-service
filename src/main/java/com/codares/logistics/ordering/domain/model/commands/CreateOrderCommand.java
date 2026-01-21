package com.codares.logistics.ordering.domain.model.commands;

import java.time.LocalDate;

/**
 * Command que representa la intención de crear un nuevo pedido.
 * <p>
 * Este es un Data Transfer Object (DTO) que captura todos los datos necesarios
 * para crear un Order en el dominio. Es utilizado por los servicios de aplicación
 * para orquestar la creación de pedidos desde múltiples fuentes (API REST, CSV, etc.).
 * </p>
 * <p>
 * Los datos en este command son strings/tipos primitivos, y la validación y
 * conversión a Value Objects ocurre en el servicio de aplicación.
 * </p>
 *
 * @param orderNumber Número del pedido (ej. P001).
 * @param customerId ID del cliente (ej. CLI-123).
 * @param deliveryDate Fecha de entrega.
 * @param status Estado inicial del pedido (PENDIENTE, CONFIRMADO, ENTREGADO).
 * @param zoneId ID de la zona de entrega (ej. ZONA1).
 * @param requiresRefrigeration true si requiere refrigeración.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record CreateOrderCommand(
    String orderNumber,
    String customerId,
    LocalDate deliveryDate,
    String status,
    String zoneId,
    boolean requiresRefrigeration
) {
}