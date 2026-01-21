package com.codares.logistics.ordering.domain.model.valueobjects;

import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa el estado de un pedido inmutable.
 * <p>
 * Encapsula los estados válidos de un pedido (PENDIENTE, CONFIRMADO, ENTREGADO)
 * y garantiza que solo se usen valores permitidos del dominio.
 * </p>
 *
 * @param value El estado del pedido como String.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public record OrderStatus(String value) {

    /**
     * Estados válidos permitidos en el dominio.
     */
    public enum Status {
        /**
         * Pedido registrado pero pendiente de confirmación.
         */
        PENDIENTE,
        
        /**
         * Pedido confirmado y listo para entrega.
         */
        CONFIRMADO,
        
        /**
         * Pedido ya entregado al cliente.
         */
        ENTREGADO
    }

    /**
     * Constructor compacto con validación de valor.
     * <p>
     * Valida que el estado sea uno de los permitidos en el dominio.
     * </p>
     *
     * @param value El estado a validar.
     * @throws InvalidArgumentException si el estado no es válido.
     */
    public OrderStatus {
        if (value == null || value.isBlank()) {
            throw new InvalidArgumentException("El estado del pedido no puede ser nulo o vacío", OrderError.ESTADO_INVALIDO);
        }
        try {
            Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException("El estado debe ser uno de: PENDIENTE, CONFIRMADO, ENTREGADO", OrderError.ESTADO_INVALIDO);
        }
    }

    /**
     * Factory method para crear un OrderStatus válido desde el enum.
     *
     * @param status El estado desde el enum Status.
     * @return Un nuevo OrderStatus.
     */
    public static OrderStatus of(Status status) {
        return new OrderStatus(status.name());
    }
}