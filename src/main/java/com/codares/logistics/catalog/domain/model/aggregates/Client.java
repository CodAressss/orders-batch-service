package com.codares.logistics.catalog.domain.model.aggregates;

import com.codares.logistics.catalog.domain.model.valueobjects.ClientId;
import com.codares.logistics.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;

/**
 * Agregado raíz que representa un Cliente en el catálogo.
 * <p>
 * Encapsula la información básica de un cliente que actúa como referencia
 * para validaciones en otros Bounded Contexts (ej. Orders). Es una entidad
 * de solo lectura en el dominio de Catalog, creada y mantenida por sistemas
 * externos.
 * </p>
 * <p>
 * Usa su propio identificador de negocio (ClientId) en lugar de UUID,
 * ya que Catalog es un contexto con datos referenciales externos.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Getter
@Entity
public class Client extends AuditableAbstractAggregateRoot<Client> {

    /**
     * Identificador único del cliente (ej. CLI-123).
     * Encapsulado en Value Object para validación de formato.
     */
    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id", length = 20))
    private ClientId id;

    /**
     * Nombre o razón social del cliente.
     * Información descriptiva del cliente en el sistema.
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Indica si el cliente está activo en el sistema.
     * Solo los clientes activos pueden crear pedidos.
     */
    @Column(nullable = false)
    private boolean activo;

    

    /**
     * Constructor protegido sin parámetros requerido por JPA.
     */
    protected Client() {
    }

    /**
     * Constructor para crear un cliente con datos crudos.
     *
     * @param id identificador único del cliente (será validado en ClientId VO)
     * @param name nombre o razón social del cliente
     * @param activo si el cliente está activo
     */
    public Client(ClientId id, String name, boolean activo) {
        this.id = id;
        this.name = name;
        this.activo = activo;
    }

     
    public Client(String id, boolean activo) {
        this.id = new ClientId(id);
        this.activo = activo;
    }
}
