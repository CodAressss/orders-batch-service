package com.codares.logistics.ordering.domain.model.valueobjects;

import java.util.regex.Pattern;

import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa el número de pedido inmutable.
 * <p>
 * Garantiza que el número de pedido siga el formato específico: una letra mayúscula
 * seguida de exactamente tres dígitos (ej. P001). Este formato asegura unicidad
 * y consistencia en el sistema de pedidos.
 * </p>
 *
 * @param value El número de pedido como String.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public record OrderNumber(String value) {

    /**
     * Patrón regex precompilado para validar el formato del número de pedido.
     * <p>
     * Formato: Una letra mayúscula seguida de exactamente tres dígitos (ej. P001).
     * Este patrón asegura que solo se acepten valores en el formato esperado.
     * </p>
     */
    private static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("^[A-Z][0-9]{3}$");

    /**
     * Constructor compacto con validaciones de negocio.
     * <p>
     * Valida que el número de pedido no sea nulo, vacío o en blanco, y que siga
     * el formato requerido (una letra mayúscula seguida de tres dígitos).
     * </p>
     *
     * @param value El número de pedido a validar.
     * @throws InvalidArgumentException si el valor es nulo/vacío o no cumple el formato.
     */
    public OrderNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidArgumentException("El número de pedido no puede ser nulo o vacío");
        }
        if (!ORDER_NUMBER_PATTERN.matcher(value).matches()) {
            throw new InvalidArgumentException("El número de pedido debe tener el formato: una letra mayúscula seguida de tres dígitos (ej. P001)", OrderError.NUMERO_PEDIDO_INVALIDO);
        }
    }

    /**
     * Método factory para crear una instancia de OrderNumber.
     * <p>
     * Proporciona una forma alternativa de creación, delegando al constructor
     * principal para mantener las validaciones.
     * </p>
     *
     * @param value El número de pedido como String.
     * @return Una nueva instancia de OrderNumber.
     * @throws InvalidArgumentException si el valor no es válido.
     */
    public static OrderNumber of(String value) {
        return new OrderNumber(value);
    }

}
