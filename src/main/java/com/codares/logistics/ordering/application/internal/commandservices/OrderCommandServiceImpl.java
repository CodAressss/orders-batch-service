package com.codares.logistics.ordering.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codares.logistics.ordering.domain.model.aggregates.Order;
import com.codares.logistics.ordering.domain.model.commands.CreateOrderCommand;
import com.codares.logistics.ordering.domain.model.commands.ConfirmOrderCommand;
import com.codares.logistics.ordering.domain.model.commands.DeliverOrderCommand;
import com.codares.logistics.ordering.domain.model.valueobjects.CustomerId;
import com.codares.logistics.ordering.domain.model.valueobjects.DeliveryDate;
import com.codares.logistics.ordering.domain.model.valueobjects.OrderError;
import com.codares.logistics.ordering.domain.model.valueobjects.OrderNumber;
import com.codares.logistics.ordering.domain.model.valueobjects.OrderStatus;
import com.codares.logistics.ordering.domain.model.valueobjects.RequiresRefrigeration;
import com.codares.logistics.ordering.domain.model.valueobjects.ZonaId;
import com.codares.logistics.ordering.domain.services.OrderCommandService;
import com.codares.logistics.shared.domain.exceptions.ResourceAlreadyExistsException;
import com.codares.logistics.shared.domain.exceptions.ResourceNotFoundException;
import com.codares.logistics.ordering.application.internal.outboundservices.acl.ExternalCatalogService;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de comandos para el agregado Order.
 * <p>
 * Orquesta la creación y transición de estados de pedidos, validando reglas de negocio
 * y persistiendo los cambios en la base de datos.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    private final com.codares.logistics.ordering.infrastructure.persistence.jpa.repositories.OrderRepository orderRepository;
    private final ExternalCatalogService externalCatalogService;

    /**
     * Crea un nuevo pedido con validaciones de negocio.
     * <p>
     * Flujo:
     * 1. Crear Value Objects con validaciones de formato
     * 2. Validar existencia de cliente y zona en BD
     * 3. Validar unicidad del número de pedido
     * 4. Crear agregado Order
     * 5. Validar restricciones de refrigeración
     * 6. Persistir
     * </p>
     */
    @Override
    @Transactional
    public Order handle(CreateOrderCommand command) {
        // 1 Crear Value Objects (validan formato automáticamente)
        OrderNumber orderNumber = new OrderNumber(command.orderNumber());
        CustomerId customerId = new CustomerId(command.customerId());
        DeliveryDate deliveryDate = new DeliveryDate(command.deliveryDate());
        OrderStatus status = new OrderStatus(command.status());
        ZonaId zoneId = new ZonaId(command.zoneId());
        RequiresRefrigeration requiresRefrigeration = new RequiresRefrigeration(command.requiresRefrigeration());

        // 2 Validar que cliente existe y está activo (ACL del Catalog BC)
        if (!externalCatalogService.isClientActiveAndExists(customerId.value())) {
            throw new ResourceNotFoundException(
                "El cliente " + customerId.value() + " no existe o no está activo en el sistema",
                OrderError.CLIENTE_NO_ENCONTRADO
            );
        }

        // 3 Validar que zona existe (ACL del Catalog BC)
        if (!externalCatalogService.isZoneValid(zoneId.value())) {
            throw new ResourceNotFoundException(
                "La zona " + zoneId.value() + " no existe en el sistema",
                OrderError.ZONA_INVALIDA
            );
        }

        // 4 Validar unicidad del número de pedido
        if (orderRepository.existsByOrderNumber(orderNumber.value())) {
            throw new ResourceAlreadyExistsException(
                "El número de pedido " + orderNumber.value() + " ya existe",
                OrderError.DUPLICADO
            );
        }

        // 5 Validar restricción de refrigeración (ACL del Catalog BC)
        if (requiresRefrigeration.isRequired() && !externalCatalogService.canZoneRefrigerate(zoneId.value())) {
            throw new ResourceNotFoundException(
                "La zona " + zoneId.value() + " no soporta refrigeración",
                OrderError.CADENA_FRIO_NO_SOPORTADA
            );
        }

        // 6 Crear agregado con Value Objects validados
        Order order = new Order(orderNumber, customerId, deliveryDate, status, zoneId, requiresRefrigeration);

        // 7 Persistir
        return orderRepository.save(order);
    }

    /**
     * Confirma un pedido existente.
     * <p>
     * Flujo:
     * 1. Obtener pedido por ID
     * 2. Invocar método de transición de estado
     * 3. Persistir
     * </p>
     */
    @Override
    @Transactional
    public Order handle(ConfirmOrderCommand command) {
        // 1 Obtener orden existente
        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "El pedido con ID " + command.orderId() + " no existe",
                OrderError.NUMERO_PEDIDO_INVALIDO
            ));

        // 2 Ejecutar transición de estado (valida invariantes)
        order.confirm();

        // 3 Persistir cambios
        return orderRepository.save(order);
    }

    /**
     * Entrega un pedido existente.
     * <p>
     * Flujo:
     * 1. Obtener pedido por ID
     * 2. Invocar método de transición de estado
     * 3. Persistir
     * </p>
     */
    @Override
    @Transactional
    public Order handle(DeliverOrderCommand command) {
        // 1 Obtener orden existente
        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "El pedido con ID " + command.orderId() + " no existe",
                OrderError.NUMERO_PEDIDO_INVALIDO
            ));

        // 2 Ejecutar transición de estado (valida invariantes)
        order.deliver();

        // 3 Persistir cambios
        return orderRepository.save(order);
    }
}
