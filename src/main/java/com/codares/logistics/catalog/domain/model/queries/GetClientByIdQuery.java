package com.codares.logistics.catalog.domain.model.queries;

import com.codares.logistics.catalog.domain.model.valueobjects.ClientId;

/**
 * Query para obtener un cliente por su identificador.
 *
 * @param clientId ID del cliente a buscar (será validado en ClientId VO)
 */
public record GetClientByIdQuery(ClientId clientId) {

    /**
     * Constructor que acepta string y crea el Value Object.
     *
     * @param clientIdString ID del cliente como string
     * @return nueva instancia de GetClientByIdQuery
     */
    public static GetClientByIdQuery from(String clientIdString) {
        return new GetClientByIdQuery(new ClientId(clientIdString));
    }
}
