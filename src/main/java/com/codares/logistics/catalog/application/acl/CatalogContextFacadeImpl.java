package com.codares.logistics.catalog.application.acl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.codares.logistics.catalog.domain.model.aggregates.Zone;
import com.codares.logistics.catalog.domain.model.queries.GetAllActiveClientsQuery;
import com.codares.logistics.catalog.domain.model.queries.GetAllZonesQuery;
import com.codares.logistics.catalog.domain.model.queries.GetClientByIdQuery;
import com.codares.logistics.catalog.domain.model.queries.GetZoneByIdQuery;
import com.codares.logistics.catalog.domain.services.CatalogQueryService;
import com.codares.logistics.catalog.interfaces.acl.CatalogContextFacade;

import lombok.RequiredArgsConstructor;

/**
 * Implementación de la fachada del Bounded Context de Catalog.
 * <p>
 * Orquesta las consultas al catálogo de datos referenciales,
 * proporcionando una interfaz simplificada para validaciones desde otros contextos.
 * Traduce parámetros String a Value Objects de forma segura.
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
public class CatalogContextFacadeImpl implements CatalogContextFacade {

    private final CatalogQueryService catalogQueryService;

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS UNITARIOS
    // ═══════════════════════════════════════════════════════════════

    @Override
    public boolean clientExistsAndIsActive(String clientId) {
        try {
            var query = GetClientByIdQuery.from(clientId);
            var client = catalogQueryService.handle(query);
            return client.map(c -> c.isActivo()).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean zoneExists(String zoneId) {
        try {
            var query = GetZoneByIdQuery.from(zoneId);
            return catalogQueryService.handle(query).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean zoneSupportsRefrigeration(String zoneId) {
        try {
            var query = GetZoneByIdQuery.from(zoneId);
            var zone = catalogQueryService.handle(query);
            return zone.map(z -> z.isSoporteRefrigeracion()).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS BATCH
    // ═══════════════════════════════════════════════════════════════

    @Override
    public Set<String> getAllActiveClientIds() {
        return catalogQueryService.handle(new GetAllActiveClientsQuery())
            .stream()
            .map(client -> client.getId().value())
            .collect(Collectors.toSet());
    }

    @Override
    public Map<String, Boolean> getAllZonesWithRefrigerationSupport() {
        return catalogQueryService.handle(new GetAllZonesQuery())
            .stream()
            .collect(Collectors.toMap(
                zone -> zone.getId().value(),
                Zone::isSoporteRefrigeracion
            ));
    }
}
