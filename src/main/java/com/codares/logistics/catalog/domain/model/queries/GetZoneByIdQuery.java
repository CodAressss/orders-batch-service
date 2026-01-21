package com.codares.logistics.catalog.domain.model.queries;

import com.codares.logistics.catalog.domain.model.valueobjects.ZoneId;

/**
 * Query para obtener una zona por su identificador.
 *
 * @param zoneId ID de la zona a buscar (será validado en ZoneId VO)
 */
public record GetZoneByIdQuery(ZoneId zoneId) {

    /**
     * Constructor que acepta string y crea el Value Object.
     *
     * @param zoneIdString ID de la zona como string
     * @return nueva instancia de GetZoneByIdQuery
     */
    public static GetZoneByIdQuery from(String zoneIdString) {
        return new GetZoneByIdQuery(new ZoneId(zoneIdString));
    }
}
