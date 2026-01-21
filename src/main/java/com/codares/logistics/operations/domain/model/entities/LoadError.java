package com.codares.logistics.operations.domain.model.entities;

import java.util.UUID;

import com.codares.logistics.operations.domain.model.aggregates.BatchLoad;
import com.codares.logistics.shared.domain.model.entities.AuditableModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

/**
 * Entidad secundaria que representa un error en una fila del archivo CSV procesado.
 * <p>
 * Depende del Aggregate BatchLoad y es eliminada cuando el agregado se elimina.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Entity
@Getter
public class LoadError extends AuditableModel {

    /**
     * Identificador único universal (UUID) del agregado.
     * Mapeado al tipo nativo 'uuid' de PostgreSQL para máxima eficiencia.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_id", nullable = false)
    private BatchLoad batchLoad;

    @Column(nullable = false)
    private int rowNumber;

    @Column(nullable = false)
    private String errorCode;

    @Column(nullable = false)
    private String errorMessage;

    protected LoadError() {}

    public LoadError(BatchLoad batchLoad, int rowNumber, String errorCode, String errorMessage) {
        this.batchLoad = batchLoad;
        this.rowNumber = rowNumber;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
