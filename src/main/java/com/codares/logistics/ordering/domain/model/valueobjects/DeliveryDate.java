package com.codares.logistics.ordering.domain.model.valueobjects;

import java.time.LocalDate;
import java.time.ZoneId;

import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa una fecha de entrega inmutable.
 * <p>
 * Garantiza que la fecha de entrega no sea nula y no sea anterior
 * a la fecha actual en la zona horaria America/Lima.
 * </p>
 *
 * @param value La fecha de entrega como LocalDate.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Embeddable
public record DeliveryDate(LocalDate value) {

    /**
     * Constructor compacto con validaciones de negocio.
     * <p>
     * Valida que la fecha no sea nula y no sea anterior a la fecha actual en Lima.
     * </p>
     *
     * @param value La fecha de entrega a validar.
     * @throws InvalidArgumentException si la fecha es nula o anterior a hoy en Lima.
     */
    public DeliveryDate {
        if (value == null) {
            throw new InvalidArgumentException("La fecha de entrega no puede ser nula", OrderError.FECHA_ENTREGA_INVALIDA);
        }
        // Validación de negocio: No puede ser pasada en Lima
        LocalDate limaNow = LocalDate.now(ZoneId.of("America/Lima"));
        if (value.isBefore(limaNow)) {
            throw new InvalidArgumentException("La fecha de entrega no puede ser anterior a la fecha actual en Lima", OrderError.FECHA_ENTREGA_INVALIDA);
        }
    }

}
