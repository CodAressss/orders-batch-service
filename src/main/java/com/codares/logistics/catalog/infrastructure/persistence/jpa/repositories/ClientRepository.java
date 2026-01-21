package com.codares.logistics.catalog.infrastructure.persistence.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codares.logistics.catalog.domain.model.aggregates.Client;
import com.codares.logistics.catalog.domain.model.valueobjects.ClientId;

/**
 * Repositorio JPA para el agregado Client.
 * <p>
 * Proporciona acceso a datos de clientes desde la base de datos.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, ClientId> {

    /**
     * Encuentra todos los clientes activos.
     * <p>
     * Uso: Pre-carga de catálogo para validación batch.
     * </p>
     *
     * @return lista de clientes con activo = true
     */
    List<Client> findByActivoTrue();
}
