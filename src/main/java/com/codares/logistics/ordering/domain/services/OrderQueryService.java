package com.codares.logistics.ordering.domain.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.codares.logistics.ordering.domain.model.aggregates.Order;
import com.codares.logistics.ordering.domain.model.queries.ExistsByOrderNumberQuery;
import com.codares.logistics.ordering.domain.model.queries.GetAllOrderNumbersQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrderByIdQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrderByOrderNumberQuery;
import com.codares.logistics.ordering.domain.model.queries.GetAllOrdersQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrdersByStatusQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrdersByDeliveryDateQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrdersByZoneQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrdersByCustomerQuery;

/**
 * Servicio de consultas para el agregado Order.
 * <p>
 * Define las operaciones de lectura (queries) que pueden realizarse sobre el agregado Order,
 * siguiendo los principios de CQRS. Cada query representa una intención de consulta de datos
 * sin efectos secundarios en el dominio.
 * </p>
 * <p>
 * Todos los métodos son de solo lectura y usan transacciones en modo readonly para optimización.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface OrderQueryService {

    /**
     * Obtiene un pedido por su identificador único.
     *
     * @param query la consulta que contiene el UUID del pedido
     * @return un Optional con el Order si existe, vacío en caso contrario
     */
    Optional<Order> handle(GetOrderByIdQuery query);

    /**
     * Obtiene un pedido por su número de pedido.
     *
     * @param query la consulta que contiene el número del pedido (ej. P001)
     * @return un Optional con el Order si existe, vacío en caso contrario
     */
    Optional<Order> handle(GetOrderByOrderNumberQuery query);

    /**
     * Obtiene todos los pedidos del sistema.
     *
     * @param query la consulta para obtener todos los pedidos
     * @return una lista con todos los Orders, vacía si no hay pedidos
     */
    List<Order> handle(GetAllOrdersQuery query);

    /**
     * Obtiene todos los pedidos con un estado específico.
     *
     * @param query la consulta que contiene el estado a filtrar (PENDIENTE, CONFIRMADO, ENTREGADO)
     * @return una lista con los Orders que coinciden con el estado
     */
    List<Order> handle(GetOrdersByStatusQuery query);

    /**
     * Obtiene todos los pedidos con fecha de entrega específica.
     *
     * @param query la consulta que contiene la fecha de entrega a filtrar
     * @return una lista con los Orders programados para esa fecha
     */
    List<Order> handle(GetOrdersByDeliveryDateQuery query);

    /**
     * Obtiene todos los pedidos de una zona de entrega específica.
     *
     * @param query la consulta que contiene el ID de la zona
     * @return una lista con los Orders asignados a esa zona
     */
    List<Order> handle(GetOrdersByZoneQuery query);

    /**
     * Obtiene todos los pedidos de un cliente específico.
     *
     * @param query la consulta que contiene el ID del cliente
     * @return una lista con los Orders realizados por ese cliente
     */
    List<Order> handle(GetOrdersByCustomerQuery query);

    // ═══════════════════════════════════════════════════════════════
    // QUERIES BATCH (para validación en procesamiento masivo)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Verifica si existe un pedido con el número dado.
     *
     * @param query la consulta con el número del pedido
     * @return true si existe, false en caso contrario
     */
    boolean handle(ExistsByOrderNumberQuery query);

    /**
     * Obtiene todos los números de pedido existentes.
     * <p>
     * Uso recomendado: Pre-cargar antes de procesar CSV para evitar N queries
     * al verificar duplicados. Retorna solo strings (no entidades completas).
     * </p>
     *
     * @param query la consulta para obtener números de pedido
     * @return Set con los números de pedido existentes
     */
    Set<String> handle(GetAllOrderNumbersQuery query);
}
