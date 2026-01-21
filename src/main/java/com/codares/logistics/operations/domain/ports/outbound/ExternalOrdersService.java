package com.codares.logistics.operations.domain.ports.outbound;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.codares.logistics.operations.domain.model.valueobjects.OrderData;

/**
 * Puerto de salida para acceder al Bounded Context de Ordering.
 * <p>
 * Define el contrato que el dominio requiere para validar y crear pedidos.
 * La implementación concreta (ACL) reside en la capa de aplicación.
 * </p>
 * <p>
 * Diseñado para soportar operaciones batch con métodos de pre-carga
 * y persistencia en bloque.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface ExternalOrdersService {

    /**
     * Obtiene todos los números de pedido existentes en el sistema.
     * <p>
     * Usada para pre-carga de contexto antes de procesar batch (evita N+1).
     * Permite validar duplicados con O(1) lookup durante el procesamiento.
     * </p>
     *
     * @return Set con los números de pedido existentes
     */
    Set<String> getAllOrderNumbers();

    /**
     * Verifica si existe un pedido con el número dado.
     *
     * @param orderNumber número del pedido
     * @return true si existe, false en caso contrario
     */
    boolean orderExists(String orderNumber);

    /**
     * Crea un nuevo pedido en el Ordering BC.
     *
     * @param orderNumber número del pedido
     * @param customerId ID del cliente
     * @param deliveryDate fecha de entrega
     * @param status estado inicial (PENDIENTE, CONFIRMADO, ENTREGADO)
     * @param zoneId ID de la zona de entrega
     * @param requiresRefrigeration si requiere cadena de frío
     * @return UUID del pedido creado
     */
    UUID createOrder(
            String orderNumber,
            String customerId,
            LocalDate deliveryDate,
            String status,
            String zoneId,
            boolean requiresRefrigeration);

    /**
     * Crea múltiples pedidos en bloque (batch insert).
     * <p>
     * Optimizado para persistencia masiva en una sola transacción.
     * </p>
     *
     * @param orders lista de datos de pedidos a crear
     * @return lista de UUIDs de los pedidos creados
     */
    List<UUID> createOrdersBatch(List<OrderData> orders);
}
