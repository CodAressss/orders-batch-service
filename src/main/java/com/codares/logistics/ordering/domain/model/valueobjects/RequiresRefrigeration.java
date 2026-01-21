package com.codares.logistics.ordering.domain.model.valueobjects;

import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa si un pedido requiere refrigeración.
 * <p>
 * Encapsula la lógica de negocio de la cadena de frío.
 * Es inmutable y garantiza que el valor sea válido.
 * </p>
 *
 * @param value true si requiere refrigeración, false en caso contrario.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public record RequiresRefrigeration(Boolean value) {

    /**
     * Constructor compacto con validación de nulidad.
     * <p>
     * Garantiza que el valor no sea nulo, previniendo estados inconsistentes.
     * </p>
     *
     * @param value El booleano a validar.
     * @throws InvalidArgumentException si el valor es nulo.
     */
    public RequiresRefrigeration {
        if (value == null) {
            throw new InvalidArgumentException("El requerimiento de refrigeración no puede ser nulo", OrderError.ESTADO_INVALIDO);
        }
    }

    /**
     * Método de conveniencia para consultar si requiere refrigeración.
     *
     * @return true si el pedido requiere refrigeración.
     */
    public boolean isRequired() {
        return value;
    }

    /**
     * Factory method para crear un RequiresRefrigeration.
     *
     * @param required true si requiere, false en caso contrario.
     * @return Un nuevo RequiresRefrigeration.
     */
    public static RequiresRefrigeration of(boolean required) {
        return new RequiresRefrigeration(required);
    }
}