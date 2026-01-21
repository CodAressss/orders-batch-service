package com.codares.logistics.ordering.domain.model.commands;

import java.util.UUID;

/**
 * Command que representa la intención de entregar un pedido.
 * <p>
 * Esta es la intención de cambiar el estado de un pedido de CONFIRMADO a ENTREGADO.
 * Solo se puede ejecutar sobre un Order que existe en la base de datos y está en estado CONFIRMADO.
 * </p>
 *
 * @param orderId Identificador único del pedido a entregar.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record DeliverOrderCommand(UUID orderId) {


}