package com.codares.logistics.catalog.domain.model.aggregates;

import com.codares.logistics.catalog.domain.model.valueobjects.ZoneId;
import com.codares.logistics.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;

/**
 * Agregado raíz que representa una Zona de Entrega en el catálogo.
 * <p>
 * Encapsula la información de una zona de entrega y sus capacidades logísticas.
 * Actúa como referencia para validaciones en otros Bounded Contexts (ej. Orders).
 * Es una entidad de solo lectura en el dominio de Catalog.
 * </p>
 * <p>
 * Usa su propio identificador de negocio (ZoneId) en lugar de UUID,
 * ya que Catalog es un contexto con datos referenciales externos.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Getter
@Entity
public class Zone extends AuditableAbstractAggregateRoot<Zone> {

    /**
     * Identificador único de la zona (ej. ZONA1, ZONA2).
     * Encapsulado en Value Object para validación de formato.
     */
    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id", length = 20))
    private ZoneId id;

    /**
     * Indica si esta zona cuenta con soporte para productos que requieren refrigeración.
     * Se valida al crear pedidos que requieran cadena de frío.
     */
    @Column(nullable = false)
    private boolean soporteRefrigeracion;

    /**
     * Constructor protegido sin parámetros requerido por JPA.
     */
    protected Zone() {
    }

    /**
     * Constructor para crear una zona con datos crudos.
     *
     * @param id identificador único de la zona (será validado en ZoneId VO)
     * @param soporteRefrigeracion si la zona soporta refrigeración
     */
    public Zone(String id, boolean soporteRefrigeracion) {
        this.id = new ZoneId(id);
        this.soporteRefrigeracion = soporteRefrigeracion;
    }
}
