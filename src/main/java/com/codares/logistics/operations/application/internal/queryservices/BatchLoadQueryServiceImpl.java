package com.codares.logistics.operations.application.internal.queryservices;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codares.logistics.operations.domain.model.aggregates.BatchLoad;
import com.codares.logistics.operations.domain.model.queries.GetBatchLoadByIdQuery;
import com.codares.logistics.operations.domain.services.BatchLoadQueryService;
import com.codares.logistics.operations.infrastructure.persistence.jpa.repositories.BatchLoadRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de consultas para el agregado {@link BatchLoad}.
 * <p>
 * Este servicio proporciona acceso de solo lectura a los registros de cargas batch,
 * incluyendo su estado actual, estadísticas de procesamiento y errores asociados.
 * </p>
 * <p>
 * Casos de uso típicos:
 * <ul>
 *   <li>Consultar el estado de una carga en progreso</li>
 *   <li>Obtener el resumen final de una carga completada</li>
 *   <li>Listar los errores de validación registrados (vía {@link LoadError})</li>
 * </ul>
 * </p>
 * <p>
 * Todas las consultas utilizan transacciones de solo lectura para optimizar
 * el rendimiento y evitar bloqueos innecesarios.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 * @see BatchLoad
 * @see LoadError
 * @see BatchLoadQueryService
 */
@Service
@RequiredArgsConstructor
public class BatchLoadQueryServiceImpl implements BatchLoadQueryService {

    /**
     * Repositorio JPA para consultas de cargas batch.
     */
    private final BatchLoadRepository batchLoadRepository;

    /**
     * Obtiene un registro de carga batch por su identificador único.
     * <p>
     * Retorna el {@link BatchLoad} completo incluyendo sus errores asociados
     * (eager loading configurado en la entidad). Si no existe, retorna
     * {@link Optional#empty()}.
     * </p>
     *
     * @param query Query con el UUID del BatchLoad a consultar.
     * @return {@link Optional} con el BatchLoad si existe, vacío en caso contrario.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<BatchLoad> handle(GetBatchLoadByIdQuery query) {
        return batchLoadRepository.findById(query.batchLoadId());
    }
}
