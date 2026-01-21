package com.codares.logistics.catalog.domain.model.valueobjects;

import java.util.regex.Pattern;

import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;
import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa el identificador único de un cliente.
 * <p>
 * Valida que el formato siga el patrón: CLI-[0-9]+
 * Ejemplo: CLI-123, CLI-999
 * </p>
 *
 * @param value el ID del cliente
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public record ClientId(String value) {

    private static final Pattern PATTERN = Pattern.compile("^CLI-[0-9]+$");

    /**
     * Constructor que valida el formato del ID del cliente.
     *
     * @param value el ID a validar
     * @throws InvalidArgumentException si el formato es inválido
     */
    public ClientId {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new InvalidArgumentException(
                "El ID del cliente debe tener formato CLI-[0-9]+, recibido: " + value,
                GlobalError.FORMATO_INVALIDO
            );
        }
    }
}
