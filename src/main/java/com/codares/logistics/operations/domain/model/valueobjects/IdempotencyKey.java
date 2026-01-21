package com.codares.logistics.operations.domain.model.valueobjects;

import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;
import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;
import jakarta.persistence.Embeddable;

/**
 * Value Object que representa la clave de idempotencia de una carga.
 * <p>
 * Valida que no sea nula ni vacía.
 * </p>
 *
 * @param value la clave de idempotencia
 */
@Embeddable
public record IdempotencyKey(String value) {

    public IdempotencyKey {
        if (value == null || value.isBlank()) {
            throw new InvalidArgumentException(
                "Idempotency-Key no puede estar vacío",
                GlobalError.CAMPO_OBLIGATORIO
            );
        }
    }
}
