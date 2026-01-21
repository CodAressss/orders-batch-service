package com.codares.logistics.operations.application.internal.commandservices;

import java.util.Optional;

import com.codares.logistics.operations.domain.model.valueobjects.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codares.logistics.operations.domain.model.aggregates.BatchLoad;
import com.codares.logistics.operations.domain.model.commands.FailBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.FinalizeBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.InitiateBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.ProcessBatchCommand;
import com.codares.logistics.operations.domain.ports.outbound.ExternalCatalogService;
import com.codares.logistics.operations.domain.ports.outbound.ExternalOrdersService;
import com.codares.logistics.operations.domain.services.BatchLoadCommandService;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService.ProcessingResult;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService.ValidationContext;
import com.codares.logistics.operations.infrastructure.persistence.jpa.repositories.BatchLoadRepository;
import com.codares.logistics.shared.domain.exceptions.ResourceAlreadyExistsException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orquestador principal del procesamiento batch de pedidos.
 * <p>
 * Coordina el flujo completo de carga masiva:
 * <ol>
 *   <li>Verificar idempotencia (evitar reproceso)</li>
 *   <li>Reservar procesamiento (crear BatchLoad en PROCESSING)</li>
 *   <li>Pre-cargar catálogos (clientes, zonas, pedidos existentes)</li>
 *   <li>Validar filas (Domain Service)</li>
 *   <li>Persistir pedidos válidos en bloque</li>
 *   <li>Finalizar BatchLoad con resultados</li>
 * </ol>
 * </p>
 * <p>
 * Este servicio es un Application Service (orquestador) que coordina
 * la interacción entre Domain Services, puertos de salida y repositorios.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchLoadProcessingService {

    private final BatchLoadRepository batchLoadRepository; 
    private final BatchLoadCommandService batchLoadCommandService; // Interfaz
    private final OrderProcessingDomainService orderProcessingDomainService;
    private final ExternalCatalogService externalCatalogService;
    private final ExternalOrdersService externalOrdersService;

    /**
     * Ejecuta el procesamiento completo del batch.
     * <p>
     * Flujo:
     * <ol>
     *   <li>Verificar si ya fue procesado (idempotencia)</li>
     *   <li>Reservar el procesamiento (INSERT atómico)</li>
     *   <li>Pre-cargar catálogos para validación</li>
     *   <li>Validar todas las filas</li>
     *   <li>Persistir pedidos válidos</li>
     *   <li>Finalizar BatchLoad con resultados</li>
     * </ol>
     * </p>
     *
     * @param command comando con datos del batch a procesar
     * @return resumen del procesamiento
     * @throws ResourceAlreadyExistsException si el archivo ya fue procesado (idempotencia)
     */
    @Transactional
    public BatchLoadSummary execute(ProcessBatchCommand command) {
        log.info("Iniciando procesamiento batch: key={}, filas={}",
                command.idempotencyKey(), command.csvRows().size());

        // FASE 1: Verificar idempotencia usando REPOSITORIO
        var idempotencyKey = new IdempotencyKey(command.idempotencyKey());
        var fileHash = new FileHash(command.fileHash());

        Optional<BatchLoad> existingBatch = batchLoadRepository.findByIdempotencyKeyAndFileHash(
                idempotencyKey, fileHash
        );

        if (existingBatch.isPresent()) {
            var batch = existingBatch.get();
            log.info("Batch ya procesado: id={}, status={}", batch.getId(), batch.getStatus());

            if (batch.getStatus() == BatchLoadStatus.COMPLETED) {
                return new BatchLoadSummary(
                        batch.getId(),
                        batch.getTotalProcessed(),
                        batch.getSuccessCount(),
                        batch.getErrorCount()
                );
            }

            throw new ResourceAlreadyExistsException(
                    "El archivo está siendo procesado o ya existe con estado: " + batch.getStatus(),
                    OperationsError.ARCHIVO_YA_PROCESADO
            );
        }

        // FASE 2: Reservar procesamiento
        var batchLoad = batchLoadCommandService.handle(
                new InitiateBatchLoadCommand(command.idempotencyKey(), command.fileHash())
        );

        try {
            // FASE 3: Pre-cargar catálogos
            log.debug("Pre-cargando catálogos para validación");
            var validationContext = new ValidationContext(
                    externalCatalogService.getAllActiveClientIds(),
                    externalCatalogService.getZonesWithRefrigerationSupport(),
                    externalOrdersService.getAllOrderNumbers()
            );

            // FASE 4: Validar filas
            log.debug("Validando {} filas", command.csvRows().size());
            ProcessingResult result = orderProcessingDomainService.processRows(
                    command.csvRows(),
                    validationContext
            );

            // FASE 5: Persistir pedidos válidos
            if (!result.validOrders().isEmpty()) {
                log.debug("Persistiendo {} pedidos válidos", result.validOrders().size());
                externalOrdersService.createOrdersBatch(result.validOrders());
            }

            // FASE 6: Finalizar BatchLoad
            batchLoad = batchLoadCommandService.handle(new FinalizeBatchLoadCommand(
                    batchLoad.getId(),
                    command.csvRows().size(),
                    result.validOrders().size(),
                    result.errors()
            ));

            log.info("Procesamiento completado: total={}, éxitos={}, errores={}",
                    command.csvRows().size(),
                    result.validOrders().size(),
                    result.errors().size());

            return new BatchLoadSummary(
                    batchLoad.getId(),
                    command.csvRows().size(),
                    result.validOrders().size(),
                    result.errors().size()
            );

        } catch (Exception e) {
            log.error("Error procesando batch: {}", e.getMessage(), e);
            batchLoadCommandService.handle(new FailBatchLoadCommand(batchLoad.getId()));
            throw e;
        }
    }
}
