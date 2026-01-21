package com.codares.logistics.operations.interfaces.rest.transform;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.codares.logistics.operations.domain.model.aggregates.BatchLoad;
import com.codares.logistics.operations.domain.model.entities.LoadError;
import com.codares.logistics.operations.domain.model.valueobjects.BatchLoadSummary;
import com.codares.logistics.operations.interfaces.rest.resources.BatchLoadResponseResource;
import com.codares.logistics.operations.interfaces.rest.resources.ErrorDetailResource;

/**
 * Assembler para transformar entidades de dominio a resources de respuesta REST.
 * <p>
 * Transforma {@link BatchLoad} y {@link BatchLoadSummary} a {@link BatchLoadResponseResource}
 * incluyendo agrupación de errores por tipo.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public class BatchLoadResponseResourceFromEntityAssembler {

    private BatchLoadResponseResourceFromEntityAssembler() {}

    /**
     * Convierte un BatchLoad completo (con errores cargados) a BatchLoadResponseResource.
     * <p>
     * Agrupa los errores por tipo (errorCode) para el resumen estadístico.
     * </p>
     *
     * @param batchLoad agregado con errores ya cargados (eager/fetch)
     * @return resource con estadísticas y detalle de errores
     */
    public static BatchLoadResponseResource toResourceFromEntity(BatchLoad batchLoad) {
        List<LoadError> errors = batchLoad.getErrors();

        // Agrupar errores por tipo para el resumen
        Map<String, Long> erroresPorTipo = errors.stream()
            .collect(Collectors.groupingBy(
                LoadError::getErrorCode,
                Collectors.counting()
            ));

        // Convertir errores a DTOs de detalle
        List<ErrorDetailResource> detalleErrores = errors.stream()
            .map(e -> new ErrorDetailResource(
                e.getRowNumber(),
                e.getErrorCode(),
                e.getErrorMessage()
            ))
            .toList();

        return new BatchLoadResponseResource(
            batchLoad.getId(),
            batchLoad.getTotalProcessed(),
            batchLoad.getSuccessCount(),
            batchLoad.getErrorCount(),
            erroresPorTipo,
            detalleErrores
        );
    }

    /**
     * Convierte un BatchLoadSummary y BatchLoad a BatchLoadResponseResource.
     * <p>
     * Usado cuando se tiene el summary del orquestador y el BatchLoad para errores.
     * </p>
     *
     * @param summary resumen del orquestador
     * @param batchLoad agregado con errores ya cargados
     * @return resource con estadísticas y detalle de errores
     */
    public static BatchLoadResponseResource toResourceFromSummary(
            BatchLoadSummary summary, 
            BatchLoad batchLoad) {
        
        List<LoadError> errors = batchLoad.getErrors();

        Map<String, Long> erroresPorTipo = errors.stream()
            .collect(Collectors.groupingBy(
                LoadError::getErrorCode,
                Collectors.counting()
            ));

        List<ErrorDetailResource> detalleErrores = errors.stream()
            .map(e -> new ErrorDetailResource(
                e.getRowNumber(),
                e.getErrorCode(),
                e.getErrorMessage()
            ))
            .toList();

        return new BatchLoadResponseResource(
            summary.batchLoadId(),
            summary.totalRows(),
            summary.successCount(),
            summary.failureCount(),
            erroresPorTipo,
            detalleErrores
        );
    }
}
