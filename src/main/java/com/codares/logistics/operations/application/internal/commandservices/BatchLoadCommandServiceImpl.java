package com.codares.logistics.operations.application.internal.commandservices;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codares.logistics.operations.domain.model.aggregates.BatchLoad;
import com.codares.logistics.operations.domain.model.commands.FailBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.FinalizeBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.InitiateBatchLoadCommand;
import com.codares.logistics.operations.domain.model.valueobjects.FileHash;
import com.codares.logistics.operations.domain.model.valueobjects.IdempotencyKey;
import com.codares.logistics.operations.domain.services.BatchLoadCommandService;
import com.codares.logistics.operations.infrastructure.persistence.jpa.repositories.BatchLoadRepository;
import com.codares.logistics.shared.domain.exceptions.ResourceAlreadyExistsException;
import com.codares.logistics.shared.domain.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de comandos para el agregado {@link BatchLoad}.
 * <p>
 * Este servicio gestiona el ciclo de vida completo del procesamiento batch de archivos CSV,
 * incluyendo la creación del registro de carga, el registro de errores por fila,
 * y la finalización del proceso (completado o fallido).
 * </p>
 * <p>
 * El ciclo de vida típico de un {@link BatchLoad} es:
 * <ol>
 *   <li><strong>Creación:</strong> Se inicia con estado PROCESSING al recibir el archivo</li>
 *   <li><strong>Registro de errores:</strong> Durante el procesamiento, se agregan {@link LoadError}
 *       para cada fila que falla validación</li>
 *   <li><strong>Finalización:</strong> Se marca como COMPLETED (con conteo de éxitos) o FAILED</li>
 * </ol>
 * </p>
 * <p>
 * Todas las operaciones son transaccionales para garantizar consistencia.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 * @see BatchLoad
 * @see LoadError
 * @see BatchLoadCommandService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchLoadCommandServiceImpl implements BatchLoadCommandService {

    /**
     * Repositorio JPA para persistencia de cargas batch.
     */
    private final BatchLoadRepository batchLoadRepository;

    
    /**
     * Paso 1: Inicia la carga y reserva la idempotencia.
     * <p>
     * Crea el registro en estado PROCESSING. Si ya existe un registro
     * con la misma clave+hash, lanza ResourceAlreadyExistsException.
     * </p>
     *
     * @param command comando con idempotencyKey y fileHash
     * @return BatchLoad creado en estado PROCESSING
     * @throws ResourceAlreadyExistsException si ya existe
     */
    @Override
    @Transactional
    public BatchLoad handle(InitiateBatchLoadCommand command) {
        log.debug("Iniciando BatchLoad: key={}, hash={}", 
            command.idempotencyKey(), command.fileHash());

        var idempotencyKey = new IdempotencyKey(command.idempotencyKey());
        var fileHash = new FileHash(command.fileHash());

        var batchLoad = new BatchLoad(idempotencyKey, fileHash);

        try {
            batchLoad = batchLoadRepository.saveAndFlush(batchLoad);
            log.info("BatchLoad creado: id={}", batchLoad.getId());
            return batchLoad;
        } catch (DataIntegrityViolationException e) {
            // Constraint UK violation - otro proceso ya creó el registro
            log.warn("BatchLoad ya existe para key={}, hash={}", 
                command.idempotencyKey(), command.fileHash());
            throw new ResourceAlreadyExistsException(
                "El archivo ya fue procesado previamente"
            );
        }
    }

    /**
     * Paso 2: Finaliza el procesamiento y persiste resultados en bloque.
     * <p>
     * Actualiza contadores y guarda todos los errores acumulados en una sola transacción.
     * </p>
     *
     * @param command comando con batchLoadId, contadores y errores
     * @return BatchLoad actualizado en estado COMPLETED
     * @throws ResourceNotFoundException si el BatchLoad no existe
     */
    @Override
    @Transactional
    public BatchLoad handle(FinalizeBatchLoadCommand command) {
        log.debug("Finalizando BatchLoad: id={}, success={}, errors={}", 
            command.batchLoadId(), command.successCount(), 
            command.errors() != null ? command.errors().size() : 0);

        var batchLoad = batchLoadRepository.findById(command.batchLoadId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "BatchLoad no encontrado: " + command.batchLoadId()
            ));

        batchLoad.finishProcessing(
            command.totalProcessed(),
            command.successCount(),
            command.errors()
        );

        batchLoad = batchLoadRepository.save(batchLoad);
        log.info("BatchLoad finalizado: id={}, status={}", 
            batchLoad.getId(), batchLoad.getStatus());

        return batchLoad;
    }

    /**
     * Paso Alternativo: Marca la carga como fallida por error de sistema.
     * <p>
     * Usado cuando ocurre una excepción no controlada durante el procesamiento.
     * </p>
     *
     * @param command comando con batchLoadId
     * @return BatchLoad actualizado en estado FAILED
     * @throws ResourceNotFoundException si el BatchLoad no existe
     */
    @Override
    @Transactional
    public BatchLoad handle(FailBatchLoadCommand command) {
        log.warn("Marcando BatchLoad como FAILED: id={}", command.batchLoadId());

        var batchLoad = batchLoadRepository.findById(command.batchLoadId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "BatchLoad no encontrado: " + command.batchLoadId()
            ));

        batchLoad.failProcessing();
        batchLoad = batchLoadRepository.save(batchLoad);

        log.info("BatchLoad marcado como FAILED: id={}", batchLoad.getId());
        return batchLoad;
    }
}
