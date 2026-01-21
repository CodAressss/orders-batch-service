package com.codares.logistics.operations.domain.services;

import java.util.Optional;

import com.codares.logistics.operations.domain.model.aggregates.BatchLoad;
import com.codares.logistics.operations.domain.model.queries.GetBatchLoadByIdQuery;

/**
 * Servicio de consultas para el agregado BatchLoad.
 * <p>
 * Proporciona acceso de solo lectura a las cargas procesadas.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface BatchLoadQueryService {

    /**
     * Obtiene un BatchLoad por su ID.
     *
     * @param query consulta con ID
     * @return Optional con el BatchLoad si existe
     */
    Optional<BatchLoad> handle(GetBatchLoadByIdQuery query);
}
