package com.codares.logistics.operations.domain.model.valueobjects;

import java.time.ZoneId;

/**
 * Constantes del Bounded Context Operations.
 * <p>
 * Centraliza valores constantes usados en las reglas de negocio
 * para evitar duplicación y facilitar mantenimiento.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public final class OperationsConstants {

    private OperationsConstants() {
        // Utility class - no instantiation
    }

    /**
     * Timezone de negocio para validaciones de fecha.
     * <p>
     * Perú (America/Lima) es UTC-5, sin horario de verano.
     * Usado para determinar "hoy" en validaciones de fecha de entrega.
     * </p>
     */
    public static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("America/Lima");
}
