package com.codares.logistics.ordering.domain.model.queries;

/**
 * Query para verificar si existe un pedido con un número específico.
 * <p>
 * Uso: Validación de unicidad antes de crear un pedido.
 * </p>
 *
 * @param orderNumber número del pedido a verificar
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record ExistsByOrderNumberQuery(String orderNumber) {}
