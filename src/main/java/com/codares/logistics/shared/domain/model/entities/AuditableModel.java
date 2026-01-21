package com.codares.logistics.shared.domain.model.entities;

import java.time.LocalDateTime;

import com.codares.logistics.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Clase base abstracta para entidades auditables que no son raíces de agregado.
 * <p>
 * Proporciona únicamente identificación y auditoría básica a entidades secundarias
 * que requieren un identificador único pero no necesitan capacidades de publicación
 * de eventos de dominio. Esta clase es más ligera que {@link AuditableAbstractAggregateRoot}
 * y se utiliza para entidades que dependen de un agregado padre.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class AuditableModel {

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
}
