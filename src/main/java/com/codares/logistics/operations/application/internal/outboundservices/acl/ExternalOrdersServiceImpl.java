package com.codares.logistics.operations.application.internal.outboundservices.acl;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.codares.logistics.operations.domain.model.valueobjects.OrderData;
import com.codares.logistics.operations.domain.ports.outbound.ExternalOrdersService;
import com.codares.logistics.ordering.interfaces.acl.OrdersContextFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del puerto {@link ExternalOrdersService}.
 * <p>
 * Actúa como Anti-Corruption Layer, adaptando el Bounded Context de Ordering
 * para que Operations BC consuma servicios de creación de pedidos sin acoplarse
 * directamente a la implementación del Ordering.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalOrdersServiceImpl implements ExternalOrdersService {

    private final OrdersContextFacade ordersContextFacade;

    /**
     * Obtiene todos los números de pedido existentes en el sistema.
     * <p>
     * Usada para pre-carga de contexto antes de procesar batch (evita N+1).
     * Permite validar duplicados con O(1) lookup durante el procesamiento.
     * </p>
     *
     * @return Set con los números de pedido existentes
     */
    @Override
    public Set<String> getAllOrderNumbers() {
        log.debug("Obteniendo todos los números de pedido del Ordering BC");
        return ordersContextFacade.getAllOrderNumbers();
    }

    /**
     * Verifica si existe un pedido con el número dado.
     * <p>
     * Método simple para validaciones unitarias sin pre-carga de contexto.
     * </p>
     *
     * @param orderNumber número del pedido
     * @return true si existe, false en caso contrario
     */
    @Override
    public boolean orderExists(String orderNumber) {
        log.debug("Verificando si pedido {} existe", orderNumber);
        return ordersContextFacade.existsByOrderNumber(orderNumber);
    }

    /**
     * Obtiene el UUID de un pedido por su número.
     * <p>
     * Retorna el UUID único del pedido si existe, null en caso contrario.
     * </p>
     *
     * @param orderNumber número del pedido
     * @return UUID del pedido si existe, null en caso contrario
     */
    public UUID getOrderIdByOrderNumber(String orderNumber) {
        log.debug("Obteniendo ID del pedido {}", orderNumber);
        return ordersContextFacade.getOrderIdByOrderNumber(orderNumber);
    }

    /**
     * Crea un nuevo pedido en el Ordering BC.
     * <p>
     * Orquesta la creación traduciendo parámetros primitivos a Value Objects
     * del dominio del Ordering.
     * </p>
     *
     * @param orderNumber número del pedido
     * @param customerId ID del cliente
     * @param deliveryDate fecha de entrega
     * @param status estado inicial (PENDIENTE, CONFIRMADO, ENTREGADO)
     * @param zoneId ID de la zona de entrega
     * @param requiresRefrigeration si requiere cadena de frío
     * @return UUID del pedido creado
     * @throws Exception si hay error en la creación (cliente no existe, duplicado, etc.)
     */
    @Override
    public UUID createOrder(
            String orderNumber,
            String customerId,
            LocalDate deliveryDate,
            String status,
            String zoneId,
            boolean requiresRefrigeration) {

        log.debug("Creando pedido {} para cliente {} via Ordering BC", orderNumber, customerId);

        try {
            UUID orderId = ordersContextFacade.createOrder(
                orderNumber,
                customerId,
                deliveryDate,
                status,
                zoneId,
                requiresRefrigeration
            );

            log.debug("Pedido {} creado exitosamente: {}", orderNumber, orderId);
            return orderId;

        } catch (Exception e) {
            log.warn("Error creando pedido {}: {}", orderNumber, e.getMessage());
            throw e;
        }
    }

    /**
     * Crea múltiples pedidos en bloque (batch insert).
     * <p>
     * Optimizado para persistencia masiva. Itera sobre los datos y delega
     * al facade de Ordering para cada pedido.
     * </p>
     *
     * @param orders lista de datos de pedidos a crear
     * @return lista de UUIDs de los pedidos creados
     */
    @Override
    public List<UUID> createOrdersBatch(List<OrderData> orders) {
        log.debug("Creando {} pedidos en batch via Ordering BC", orders.size());
        
        return orders.stream()
            .map(order -> ordersContextFacade.createOrder(
                order.orderNumber(),
                order.customerId(),
                order.deliveryDate(),
                order.status(),
                order.zoneId(),
                order.requiresRefrigeration()
            ))
            .collect(Collectors.toList());
    }
}
