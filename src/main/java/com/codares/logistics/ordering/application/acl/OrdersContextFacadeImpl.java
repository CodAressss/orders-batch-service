package com.codares.logistics.ordering.application.acl;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.codares.logistics.ordering.domain.model.commands.CreateOrderCommand;
import com.codares.logistics.ordering.domain.model.queries.ExistsByOrderNumberQuery;
import com.codares.logistics.ordering.domain.model.queries.GetAllOrderNumbersQuery;
import com.codares.logistics.ordering.domain.model.queries.GetOrderByOrderNumberQuery;
import com.codares.logistics.ordering.domain.services.OrderCommandService;
import com.codares.logistics.ordering.domain.services.OrderQueryService;
import com.codares.logistics.ordering.interfaces.acl.OrdersContextFacade;

import lombok.RequiredArgsConstructor;

/**
 * Implementación de la fachada del Bounded Context de Ordering.
 * <p>
 * Orquesta las operaciones del dominio de órdenes, ocultando la complejidad
 * de Commands y Queries. Actúa como Anti-Corruption Layer traduciendo parámetros
 * externos (strings, primitivos) a Value Objects del dominio de forma segura.
 * </p>
 * <p>
 * Incluye métodos batch para validación en procesamiento masivo.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class OrdersContextFacadeImpl implements OrdersContextFacade {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS UNITARIOS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Crea un nuevo pedido en el sistema.
     * <p>
     * Flujo:
     * 1. Construir CreateOrderCommand con parámetros crudos
     * 2. Delegar al CommandService (que valida y persiste)
     * 3. Retornar UUID del pedido creado
     * </p>
     *
     * Los parámetros de entrada se traducen automáticamente a Value Objects
     * dentro del CommandService, que valida formato y reglas de negocio.
     */
    @Override
    public UUID createOrder(
            String orderNumber,
            String customerId,
            LocalDate deliveryDate,
            String status,
            String zoneId,
            boolean requiresRefrigeration) {

        var command = new CreateOrderCommand(
                orderNumber,
                customerId,
                deliveryDate,
                status,
                zoneId,
                requiresRefrigeration);

        var order = orderCommandService.handle(command);
        return order.getId();
    }

    /**
     * Obtiene el UUID de un pedido por su número.
     * <p>
     * Flujo:
     * 1. Construir GetOrderByOrderNumberQuery
     * 2. Delegar al QueryService
     * 3. Retornar UUID si existe, null en caso contrario
     * </p>
     */
    @Override
    public UUID getOrderIdByOrderNumber(String orderNumber) {
        var query = new GetOrderByOrderNumberQuery(orderNumber);
        var order = orderQueryService.handle(query);
        return order.map(o -> o.getId()).orElse(null);
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS BATCH
    // ═══════════════════════════════════════════════════════════════

    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        return orderQueryService.handle(new ExistsByOrderNumberQuery(orderNumber));
    }

    @Override
    public Set<String> getAllOrderNumbers() {
        return orderQueryService.handle(new GetAllOrderNumbersQuery());
    }
}
