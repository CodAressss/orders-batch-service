package com.codares.logistics.catalog.application.internal.queryservices;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codares.logistics.catalog.domain.model.aggregates.Client;
import com.codares.logistics.catalog.domain.model.aggregates.Zone;
import com.codares.logistics.catalog.domain.model.queries.GetAllActiveClientsQuery;
import com.codares.logistics.catalog.domain.model.queries.GetAllZonesQuery;
import com.codares.logistics.catalog.domain.model.queries.GetClientByIdQuery;
import com.codares.logistics.catalog.domain.model.queries.GetZoneByIdQuery;
import com.codares.logistics.catalog.domain.services.CatalogQueryService;
import com.codares.logistics.catalog.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.codares.logistics.catalog.infrastructure.persistence.jpa.repositories.ZoneRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de consultas para el catálogo.
 * <p>
 * Proporciona acceso optimizado de solo lectura a los datos referenciales
 * (clientes y zonas) que otros Bounded Contexts necesitan validar.
 * </p>
 * <p>
 * Incluye métodos batch para pre-carga de catálogos en procesamiento masivo.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class CatalogQueryServiceImpl implements CatalogQueryService {

    private final ClientRepository clientRepository;
    private final ZoneRepository zoneRepository;

    // ═══════════════════════════════════════════════════════════════
    // QUERIES UNITARIAS
    // ═══════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> handle(GetClientByIdQuery query) {
        return clientRepository.findById(query.clientId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Zone> handle(GetZoneByIdQuery query) {
        return zoneRepository.findById(query.zoneId());
    }

    // ═══════════════════════════════════════════════════════════════
    // QUERIES BATCH
    // ═══════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<Client> handle(GetAllActiveClientsQuery query) {
        return clientRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Zone> handle(GetAllZonesQuery query) {
        return zoneRepository.findAll();
    }
}
