package com.codares.logistics.operations.infrastructure.persistence.jpa.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codares.logistics.operations.domain.model.aggregates.BatchLoad;
import com.codares.logistics.operations.domain.model.valueobjects.FileHash;
import com.codares.logistics.operations.domain.model.valueobjects.IdempotencyKey;

/**
 * Repositorio JPA para el agregado BatchLoad.
 * <p>
 * Proporciona acceso a registros de cargas procesadas
 * para auditoría, consulta de resultados y verificación de idempotencia.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface BatchLoadRepository extends JpaRepository<BatchLoad, UUID> {

    /**
     * Busca un BatchLoad por su clave de idempotencia y hash de archivo.
     * <p>
     * Usado para verificar si un archivo ya fue procesado previamente.
     * La combinación de ambos valores forma la clave única de idempotencia.
     * </p>
     *
     * @param idempotencyKey clave de idempotencia del header
     * @param fileHash hash SHA-256 del archivo
     * @return Optional con el BatchLoad si existe
     */
    Optional<BatchLoad> findByIdempotencyKeyAndFileHash(IdempotencyKey idempotencyKey, FileHash fileHash);
}
