package com.codares.logistics.shared.domain.model.aggregates;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Clase base abstracta para las Raíces de Agregado (Aggregate Roots) que requieren
 * capacidades de auditoría automática y publicación de eventos de dominio.
 * <p>
 * Esta clase extiende {@link AbstractAggregateRoot} para permitir el registro y
 * publicación de eventos de dominio como parte del ciclo de vida de la transacción.
 * Además, incluye campos de auditoría gestionados automáticamente por Spring Data JPA.
 * </p>
 *
 * @param <T> El tipo de la clase hija que extiende esta clase (patrón CRTP).
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class AuditableAbstractAggregateRoot<T extends AbstractAggregateRoot<T>> extends AbstractAggregateRoot<T> {

    /**
     * Fecha y hora de creación del registro.
     * Este campo es inmutable una vez creado y se gestiona automáticamente.
     * Usa LocalDateTime para compatibilidad con Java 8+ Time API.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última modificación del registro.
     * Se actualiza automáticamente cada vez que la entidad sufre cambios.
     * Usa LocalDateTime para compatibilidad con Java 8+ Time API.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Registra un evento de dominio para ser publicado.
     * <p>
     * Este método expone la funcionalidad protegida {@code registerEvent} de
     * {@link AbstractAggregateRoot} para permitir que la lógica de dominio
     * registre eventos que serán despachados al finalizar la transacción.
     * </p>
     *
     * @param event El evento de dominio a registrar.
     */
    public void addDomainEvent(Object event) {
        registerEvent(event);
    }    
}
