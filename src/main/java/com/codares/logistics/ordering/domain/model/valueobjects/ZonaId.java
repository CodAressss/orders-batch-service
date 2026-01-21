package com.codares.logistics.ordering.domain.model.valueobjects;

import java.util.regex.Pattern;

import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;
import jakarta.persistence.Embeddable;

/**
 * Value Object que representa el ID de la zona inmutable.
 * <p>
 * Garantiza que el ID de la zona siga el formato específico: "ZONA" seguido de uno o más dígitos (ej. ZONA1).
 * </p>
 *
 * @param value El ID de la zona como String.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public record ZonaId(String value) {

    /**
     * Patrón regex precompilado para validar el formato del ID de la zona.
     * <p>
     * Formato: "ZONA" seguido de uno o más dígitos (ej. ZONA1).
     * </p>
     */
    private static final Pattern ZONA_ID_PATTERN = Pattern.compile("^ZONA[0-9]+$");

    /**
     * Constructor compacto con validaciones de formato.
     * <p>
     * Valida que el ID de la zona no sea nulo, vacío y siga el formato requerido.
     * </p>
     *
     * @param value El ID de la zona a validar.
     * @throws InvalidArgumentException si el valor es nulo, vacío o no cumple el formato.
     */
    public ZonaId {
        if (value == null || value.isBlank()) {
            throw new InvalidArgumentException("El ID de zona no puede ser nulo o vacío", OrderError.ZONA_INVALIDA);
        }
        if (!ZONA_ID_PATTERN.matcher(value).matches()) {
            throw new InvalidArgumentException("El ID de zona debe tener el formato: ZONA seguido de dígitos (ej. ZONA1)", OrderError.ZONA_INVALIDA);
        }
    }

}
