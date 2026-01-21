package com.codares.logistics.operations.domain.model.aggregates;

import com.codares.logistics.operations.domain.model.entities.LoadError;
import com.codares.logistics.operations.domain.model.valueobjects.BatchLoadStatus;
import com.codares.logistics.operations.domain.model.valueobjects.FileHash;
import com.codares.logistics.operations.domain.model.valueobjects.IdempotencyKey;
import com.codares.logistics.operations.domain.model.valueobjects.RowErrorDetail;
import com.codares.logistics.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Agregado que representa una carga procesada con su resultado.
 * <p>
 * Encapsula el resultado del procesamiento de un archivo CSV:
 * errores acumulados por fila, contadores y estado.
 * </p>
 * <p>
 * ID y auditoría se heredan de AuditableAbstractAggregateRoot.
 * LoadError es una Entity secundaria que depende de este Aggregate.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "cargas_idempotencias", uniqueConstraints = {
    @UniqueConstraint(name = "uk_batch_idempotency", columnNames = {"idempotency_key", "file_hash"})
})
@Getter
public class BatchLoad extends AuditableAbstractAggregateRoot<BatchLoad> {

    /**
     * Identificador único universal (UUID) del agregado.
     * Mapeado al tipo nativo 'uuid' de PostgreSQL para máxima eficiencia.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "idempotency_key", nullable = false, length = 50))
    private IdempotencyKey idempotencyKey;

    @Embedded
    @AttributeOverride(name = "sha256", column = @Column(name = "file_hash", nullable = false, length = 64))
    private FileHash fileHash;

    @Column(nullable = false, length = 20)
    private BatchLoadStatus status;

    @Column(nullable = false)
    private int totalProcessed;

    @Column(nullable = false)
    private int successCount;

    @Column(nullable = false)
    private int errorCount;
    
    @OneToMany(mappedBy = "batchLoad", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoadError> errors = new ArrayList<>();

    protected BatchLoad() {}

    public BatchLoad(IdempotencyKey idempotencyKey, FileHash fileHash) {
        this.idempotencyKey = idempotencyKey;
        this.fileHash = fileHash;
        this.status = BatchLoadStatus.PROCESSING;
        this.totalProcessed = 0;
        this.successCount = 0;
        this.errorCount = 0;
    }

    /**
     * Finaliza la carga registrando resultados y errores de una sola vez.
     * Este método reemplaza las múltiples llamadas a BD.
     */
    public void finishProcessing(int totalProcessed, int successCount, List<RowErrorDetail> errorDetails) {
        this.totalProcessed = totalProcessed;
        this.successCount = successCount;
        
        // Limpiamos errores previos si fuera un re-intento (aunque por idempotencia no debería ocurrir)
        this.errors.clear();

        // Convertimos DTOs a Entidades y las asociamos a este Agregado
        if (errorDetails != null && !errorDetails.isEmpty()) {
            for (var detail : errorDetails) {
                LoadError errorEntity = new LoadError(
                    this, // Pasamos 'this' para mantener la relación bidireccional
                    detail.rowNumber(), 
                    detail.errorCode(), 
                    detail.errorMessage()
                );
                this.errors.add(errorEntity);
            }
            this.errorCount = errorDetails.size();
        } else {
            this.errorCount = 0;
        }

        // Regla de negocio: Si se procesó (aunque todo sean errores), se marca COMPLETED.
        // FAILED se reserva para errores técnicos (caída de sistema, error de parsing).
        this.status = BatchLoadStatus.COMPLETED;
    }

    public void failProcessing() {
        this.status = BatchLoadStatus.FAILED;
    }
}
