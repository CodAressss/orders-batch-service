package com.codares.logistics.operations.domain.services;

import com.codares.logistics.operations.domain.model.aggregates.BatchLoad;
import com.codares.logistics.operations.domain.model.commands.FailBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.FinalizeBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.InitiateBatchLoadCommand;

/**
 * Servicio de comandos para el agregado BatchLoad.
 * <p>
 * Gestiona el ciclo de vida del procesamiento batch:
 * creación, registro de errores, completar y fallar.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface BatchLoadCommandService {

   /**
     * Paso 1: Inicia la carga y reserva la idempotencia.
     * Crea el registro en estado PROCESSING.
     */
    BatchLoad handle(InitiateBatchLoadCommand command);

    /**
     * Paso 2: Finaliza el procesamiento y persiste resultados en bloque.
     * Guarda contadores y todos los errores acumulados en una sola transacción.
     */
    BatchLoad handle(FinalizeBatchLoadCommand command);

    /**
     * Paso Alternativo: Marca la carga como fallida por error de sistema.
     */
    BatchLoad handle(FailBatchLoadCommand command);
}
