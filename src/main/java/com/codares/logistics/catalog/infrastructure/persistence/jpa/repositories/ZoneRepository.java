package com.codares.logistics.catalog.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codares.logistics.catalog.domain.model.aggregates.Zone;
import com.codares.logistics.catalog.domain.model.valueobjects.ZoneId;

/**
 * Repositorio JPA para el agregado Zone.
 * <p>
 * Proporciona acceso a datos de zonas de entrega desde la base de datos.
 * </p>
 */
@Repository
public interface ZoneRepository extends JpaRepository<Zone, ZoneId> {
}
