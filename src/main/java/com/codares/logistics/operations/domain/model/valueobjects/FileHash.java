package com.codares.logistics.operations.domain.model.valueobjects;

import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;
import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;
import jakarta.persistence.Embeddable;

/**
 * Value Object que representa el hash SHA-256 de un archivo.
 * <p>
 * Valida que sea un hash válido (64 caracteres hexadecimales).
 * </p>
 *
 * @param sha256 el hash del archivo
 */
@Embeddable
public record FileHash(String sha256) {

    public FileHash {
        if (sha256 == null || !sha256.matches("^[a-fA-F0-9]{64}$")) {
            throw new InvalidArgumentException(
                "El hash debe ser SHA-256 válido (64 caracteres hexadecimales), recibido: " + sha256,
                GlobalError.FORMATO_INVALIDO
            );
        }
    }
}
