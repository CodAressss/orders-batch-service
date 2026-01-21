package com.codares.logistics.ordering.domain.model.valueobjects;

import java.util.regex.Pattern;

import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa el ID del cliente inmutable.
 * <p>
 * Garantiza que el ID del cliente siga el formato específico: "CLI-" seguido de uno o más dígitos (ej. CLI-123).
 * </p>
 *
 * @param value El ID del cliente como String.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public record CustomerId(String value) {

    /**
     * Patrón regex precompilado para validar el formato del ID del cliente.
     * <p>
     * Formato: "CLI-" seguido de uno o más dígitos (ej. CLI-123).
     * </p>
     */
    private static final Pattern CUSTOMER_ID_PATTERN = Pattern.compile("^CLI-[0-9]+$");

    /**
     * Constructor compacto con validaciones de formato.
     * <p>
     * Valida que el ID del cliente no sea nulo, vacío y siga el formato requerido.
     * </p>
     *
     * @param value El ID del cliente a validar.
     * @throws InvalidArgumentException si el valor es nulo, vacío o no cumple el formato.
     */
    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new InvalidArgumentException("El ID del cliente no puede ser nulo o vacío", OrderError.CLIENTE_ID_INVALIDO);
        }
        if (!CUSTOMER_ID_PATTERN.matcher(value).matches()) {
            throw new InvalidArgumentException("El ID del cliente debe tener el formato: CLI- seguido de dígitos (ej. CLI-123)", OrderError.CLIENTE_ID_INVALIDO);
        }
    }

}
