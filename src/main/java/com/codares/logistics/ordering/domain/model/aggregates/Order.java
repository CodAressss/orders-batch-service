package com.codares.logistics.ordering.domain.model.aggregates;

import java.util.UUID;

import com.codares.logistics.ordering.domain.model.valueobjects.CustomerId;
import com.codares.logistics.ordering.domain.model.valueobjects.DeliveryDate;
import com.codares.logistics.ordering.domain.model.valueobjects.OrderError;
import com.codares.logistics.ordering.domain.model.valueobjects.OrderNumber;
import com.codares.logistics.ordering.domain.model.valueobjects.OrderStatus;
import com.codares.logistics.ordering.domain.model.valueobjects.RequiresRefrigeration;
import com.codares.logistics.ordering.domain.model.valueobjects.ZonaId;
import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;
import com.codares.logistics.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

/**
 * Agregado raíz Order que representa un pedido en el contexto de pedidos (Orders).
 * <p>
 * Esta clase es la raíz del agregado Order y encapsula toda la lógica de negocio
 * relacionada con la gestión de pedidos. Extiende {@link AuditableAbstractAggregateRoot}
 * para obtener capacidades de auditoría automática y publicación de eventos de dominio.
 * </p>
 * <p>
 * Un pedido contiene información sobre el número de pedido, cliente, zona de entrega,
 * fecha de entrega, estado y requerimientos de refrigeración.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Entity
@Getter
public class Order extends AuditableAbstractAggregateRoot<Order> {

    /**
     * Identificador único universal (UUID) del agregado.
     * Mapeado al tipo nativo 'uuid' de PostgreSQL para máxima eficiencia.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Número único del pedido.
     * Valor Object inmutable que garantiza el formato alfanumérico específico (ej. P001).
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "order_number", unique = true, nullable = false, length = 10))
    private OrderNumber orderNumber;

    /**
     * Identificador del cliente que realizó el pedido.
     * Value Object que garantiza el formato específico (ej. CLI-123).
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "customer_id", nullable = false, length = 20))
    private CustomerId customerId;

    /**
     * Identificador de la zona de entrega.
     * Value Object que garantiza el formato específico (ej. ZONA1).
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "zona_id", nullable = false, length = 20))
    private ZonaId zonaId;

    /**
     * Fecha de entrega del pedido.
     * Value Object que valida que no sea anterior a hoy en la zona horaria de Lima.
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "delivery_date", nullable = false))
    private DeliveryDate deliveryDate;

    /**
     * Estado actual del pedido (PENDIENTE, CONFIRMADO, ENTREGADO).
     * Value Object que garantiza solo valores válidos del dominio.
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "status", nullable = false, length = 20))
    private OrderStatus status;

    /**
     * Indicador de si el pedido requiere refrigeración.
     * Value Object inmutable que encapsula la lógica de cadena de frío.
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "requires_refrigeration", nullable = false))
    private RequiresRefrigeration requiresRefrigeration;


    /**
     * Constructor sin parámetros reservado para JPA.
     * <p>
     * Este constructor es de uso exclusivo del framework JPA para
     * instanciar la entidad desde la base de datos. No debe ser utilizado
     * directamente desde la lógica de negocio.
     * </p>
     */
    protected Order() {
    }

    /**
     * Constructor con parámetros para crear una nueva instancia de Order.
     * <p>
     * Inicializa todos los atributos del agregado con los valores proporcionados.
     * </p>
     *
     * @param orderNumber Número del pedido (ej. P001).
     * @param customerId ID del cliente (ej. CLI-123).
     * @param deliveryDate Fecha de entrega.
     * @param status Estado inicial del pedido.
     * @param zoneId ID de la zona de entrega.
     * @param requiresRefrigeration Si requiere refrigeración.
     */
    public Order(
            OrderNumber orderNumber,
            CustomerId customerId,
            DeliveryDate deliveryDate,
            OrderStatus status,
            ZonaId zoneId,
            RequiresRefrigeration requiresRefrigeration) {
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.zonaId = zoneId;
        this.requiresRefrigeration = requiresRefrigeration;
    }

    /**
     * Valida las restricciones de refrigeración del pedido.
     * <p>
     * Si el pedido requiere refrigeración, la zona de entrega debe soportarla.
     * Este método recibe como parámetro un booleano que indica si la zona soporta
     * refrigeración. La verificación de existencia de la zona ocurre en el servicio
     * de aplicación.
     * </p>
     *
     * @param zoneSupportsRefrigeration true si la zona soporta refrigeración.
     * @throws InvalidArgumentException si requiere refrigeración pero la zona no la soporta.
     */
    public void validateRefrigerationRequirements(boolean zoneSupportsRefrigeration) {
        if (this.requiresRefrigeration.isRequired() && !zoneSupportsRefrigeration) {
            throw new InvalidArgumentException(
                "La zona " + this.zonaId.value() + " no soporta refrigeración pero el pedido la requiere",
                OrderError.CADENA_FRIO_NO_SOPORTADA
            );
        }
    }

    /**
     * Transiciona el pedido a estado CONFIRMADO.
     * <p>
     * Solo es posible desde estado PENDIENTE.
     * </p>
     *
     * @throws InvalidArgumentException si no se puede confirmar desde el estado actual.
     */
    public void confirm() {
        if (!this.status.value().equals(OrderStatus.Status.PENDIENTE.name())) {
            throw new InvalidArgumentException(
                "No se puede confirmar un pedido que no está en estado PENDIENTE",
                OrderError.ESTADO_INVALIDO
            );
        }
        this.status = OrderStatus.of(OrderStatus.Status.CONFIRMADO);
    }

    /**
     * Transiciona el pedido a estado ENTREGADO.
     * <p>
     * Solo es posible desde estado CONFIRMADO.
     * </p>
     *
     * @throws InvalidArgumentException si no se puede entregar desde el estado actual.
     */
    public void deliver() {
        if (!this.status.value().equals(OrderStatus.Status.CONFIRMADO.name())) {
            throw new InvalidArgumentException(
                "No se puede entregar un pedido que no está en estado CONFIRMADO",
                OrderError.ESTADO_INVALIDO
            );
        }
        this.status = OrderStatus.of(OrderStatus.Status.ENTREGADO);
    }
}
