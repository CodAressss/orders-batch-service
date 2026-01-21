package com.codares.logistics.ordering.domain.model.commands;

import java.util.UUID;

/**
 * Command que representa la intención de confirmar un pedido.
 * <p>
 * Esta es la intención de cambiar el estado de un pedido de PENDIENTE a CONFIRMADO.
 * Solo se puede ejecutar sobre un Order que existe en la base de datos.
 * </p>
 *
 * @param orderId Identificador único del pedido a confirmar.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record ConfirmOrderCommand(UUID orderId) {


}