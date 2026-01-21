package com.codares.logistics.catalog.domain.model.valueobjects;

import java.util.regex.Pattern;

import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;
import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa el identificador único de una zona de entrega.
 * <p>
 * Valida que el formato siga el patrón: ZONA[0-9]+
 * Ejemplo: ZONA1, ZONA99
 * </p>
 *
 * @param value el ID de la zona
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public record ZoneId(String value) {

    private static final Pattern PATTERN = Pattern.compile("^ZONA[0-9]+$");

    /**
     * Constructor que valida el formato del ID de la zona.
     *
     * @param value el ID a validar
     * @throws InvalidArgumentException si el formato es inválido
     */
    public ZoneId {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new InvalidArgumentException(
                "El ID de la zona debe tener formato ZONA[0-9]+, recibido: " + value,
                GlobalError.FORMATO_INVALIDO
            );
        }
    }
}
