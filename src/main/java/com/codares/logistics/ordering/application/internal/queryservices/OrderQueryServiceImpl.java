package com.codares.logistics.ordering.application.internal.queryservices;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codares.logistics.ordering.domain.model.aggregates.Order;
import com.codares.logistics.ordering.domain.model.queries.ExistsByOrderNumberQuery;
import com.codares.logistics.ordering.domain.model.queries.GetAllOrderNumbersQuery;
import com.codares.logistics.ordering.domain.model.queries.GetAllOrdersQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrderByIdQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrderByOrderNumberQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrdersByCustomerQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrdersByDeliveryDateQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrdersByStatusQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrdersByZoneQuery;
import com.codares.logistics.ordering.domain.services.OrderQueryService;
import com.codares.logistics.ordering.infrastructure.persistence.jpa.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de consultas para el agregado Order.
 * <p>
 * Proporciona acceso de solo lectura a los pedidos desde múltiples perspectivas,
 * optimizando el rendimiento con transacciones readonly.
 * </p>
 * <p>
 * Incluye métodos batch para pre-carga de datos en procesamiento masivo.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;

    // ═══════════════════════════════════════════════════════════════
    // QUERIES UNITARIAS
    // ═══════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> handle(GetOrderByIdQuery query) {
        return orderRepository.findById(query.orderId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> handle(GetOrderByOrderNumberQuery query) {
        return orderRepository.findByOrderNumber(query.orderNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> handle(GetAllOrdersQuery query) {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> handle(GetOrdersByStatusQuery query) {
        return orderRepository.findByStatus(query.status());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> handle(GetOrdersByDeliveryDateQuery query) {
        return orderRepository.findByDeliveryDate(query.deliveryDate());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> handle(GetOrdersByZoneQuery query) {
        return orderRepository.findByZonaId(query.zoneId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> handle(GetOrdersByCustomerQuery query) {
        return orderRepository.findByCustomerId(query.customerId());
    }

    // ═══════════════════════════════════════════════════════════════
    // QUERIES BATCH
    // ═══════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public boolean handle(ExistsByOrderNumberQuery query) {
        return orderRepository.existsByOrderNumber(query.orderNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> handle(GetAllOrderNumbersQuery query) {
        return orderRepository.findAllOrderNumbers();
    }
}
