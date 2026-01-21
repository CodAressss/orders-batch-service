package com.codares.logistics.operations.application.internal.commandservices;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codares.logistics.operations.domain.model.commands.FailBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.FinalizeBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.InitiateBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.ProcessBatchCommand;
import com.codares.logistics.operations.domain.model.valueobjects.BatchLoadStatus;
import com.codares.logistics.operations.domain.model.valueobjects.BatchLoadSummary;
import com.codares.logistics.operations.domain.ports.outbound.ExternalCatalogService;
import com.codares.logistics.operations.domain.ports.outbound.ExternalOrdersService;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService.ProcessingResult;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService.ValidationContext;
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

    private final BatchLoadCommandServiceImpl batchLoadCommandService;
    private final OrderProcessingDomainService orderProcessingDomainService;
    private final ExternalCatalogService catalogService;
    private final ExternalOrdersService ordersService;

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

        // FASE 1: Verificar idempotencia
        var existingBatch = batchLoadCommandService.findByIdempotencyKeyAndFileHash(
            command.idempotencyKey(), 
            command.fileHash()
        );

        if (existingBatch.isPresent()) {
            var batch = existingBatch.get();
            log.info("Batch ya procesado: id={}, status={}", batch.getId(), batch.getStatus());
            
            // Siempre lanzar excepción para idempotencia (409 CONFLICT)
            if (batch.getStatus() == BatchLoadStatus.COMPLETED) {
                throw new ResourceAlreadyExistsException(
                    "Este archivo ya fue procesado anteriormente"
                );
            }
            
            throw new ResourceAlreadyExistsException(
                "El archivo está siendo procesado actualmente"
            );
        }

        // FASE 2: Reservar procesamiento (INSERT atómico)
        var batchLoad = batchLoadCommandService.handle(
            new InitiateBatchLoadCommand(command.idempotencyKey(), command.fileHash())
        );

        try {
            // FASE 3: Pre-cargar catálogos (evita N+1)
            log.debug("Pre-cargando catálogos para validación");
            var validationContext = new ValidationContext(
                catalogService.getAllActiveClientIds(),
                catalogService.getZonesWithRefrigerationSupport(),
                ordersService.getAllOrderNumbers()
            );

            // FASE 4: Validar filas (Domain Service - lógica de negocio)
            log.debug("Validando {} filas", command.csvRows().size());
            ProcessingResult result = orderProcessingDomainService.processRows(
                command.csvRows(), 
                validationContext
            );

            // FASE 5: Persistir pedidos válidos en bloque
            if (!result.validOrders().isEmpty()) {
                log.debug("Persistiendo {} pedidos válidos", result.validOrders().size());
                List<?> createdOrderIds = ordersService.createOrdersBatch(result.validOrders());
                log.info("Pedidos creados: {}", createdOrderIds.size());
            }

            // FASE 6: Finalizar BatchLoad con resultados
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
            // Marcar como fallido en caso de error
            log.error("Error procesando batch: {}", e.getMessage(), e);
            batchLoadCommandService.handle(new FailBatchLoadCommand(batchLoad.getId()));
            throw e;
        }
    }
}
