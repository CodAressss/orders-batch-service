package com.codares.logistics.ordering.domain.services;

import com.codares.logistics.ordering.domain.model.aggregates.Order;
import com.codares.logistics.ordering.domain.model.commands.CreateOrderCommand;
import com.codares.logistics.ordering.domain.model.commands.ConfirmOrderCommand;
import com.codares.logistics.ordering.domain.model.commands.DeliverOrderCommand;

/**
 * Servicio de comandos para el agregado Order.
 * <p>
 * Define las operaciones de escritura (comandos) que pueden realizarse sobre el agregado Order,
 * siguiendo los principios de CQRS. Cada comando representa una intención de cambio de estado
 * en el dominio de órdenes de pedidos.
 * </p>
 * <p>
 * Los métodos de esta interfaz garantizan la consistencia del agregado y lanzan excepciones
 * de dominio cuando se violan las invariantes de negocio.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface OrderCommandService {

    /**
     * Crea un nuevo pedido en el sistema.
     * <p>
     * Inicializa el agregado Order con los datos proporcionados y persiste en la base de datos.
     * Valida que el cliente exista, la zona exista, el número de pedido sea único,
     * y que la fecha de entrega no sea pasada.
     * </p>
     *
     * @param command el comando que contiene los datos del pedido a crear
     * @return el Order recién creado
     * @throws ResourceNotFoundException si el cliente o zona no existen
     * @throws ResourceAlreadyExistsException si el número de pedido ya existe
     * @throws InvalidArgumentException si la validación falla
     */
    Order handle(CreateOrderCommand command);

    /**
     * Confirma un pedido pendiente.
     * <p>
     * Transiciona el estado del pedido de PENDIENTE a CONFIRMADO.
     * Solo es posible si el pedido existe y está en estado PENDIENTE.
     * </p>
     *
     * @param command el comando que contiene el ID del pedido a confirmar
     * @return el Order actualizado con estado CONFIRMADO
     * @throws ResourceNotFoundException si el pedido no existe
     * @throws InvalidArgumentException si el pedido no está en estado PENDIENTE
     */
    Order handle(ConfirmOrderCommand command);

    /**
     * Entrega un pedido confirmado.
     * <p>
     * Transiciona el estado del pedido de CONFIRMADO a ENTREGADO.
     * Solo es posible si el pedido existe y está en estado CONFIRMADO.
     * </p>
     *
     * @param command el comando que contiene el ID del pedido a entregar
     * @return el Order actualizado con estado ENTREGADO
     * @throws ResourceNotFoundException si el pedido no existe
     * @throws InvalidArgumentException si el pedido no está en estado CONFIRMADO
     */
    Order handle(DeliverOrderCommand command);
}
