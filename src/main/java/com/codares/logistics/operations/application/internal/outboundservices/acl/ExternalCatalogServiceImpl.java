package com.codares.logistics.operations.application.internal.outboundservices.acl;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.codares.logistics.catalog.interfaces.acl.CatalogContextFacade;
import com.codares.logistics.operations.domain.ports.outbound.ExternalCatalogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del puerto {@link ExternalCatalogService}.
 * <p>
 * Actúa como Anti-Corruption Layer, adaptando el Bounded Context de Catalog
 * para que Operations BC acceda a validaciones de referencia (clientes, zonas)
 * sin acoplarse directamente a la implementación del Catalog.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalCatalogServiceImpl implements ExternalCatalogService {

    private final CatalogContextFacade catalogContextFacade;

    /**
     * Obtiene todos los IDs de clientes activos disponibles en el catálogo.
     * <p>
     * Usada para pre-carga de contexto antes de procesar batch (evita N+1).
     * </p>
     *
     * @return Set con los IDs de clientes activos
     */
    @Override
    public Set<String> getAllActiveClientIds() {
        log.debug("Obteniendo IDs de clientes activos del Catalog BC");
        return catalogContextFacade.getAllActiveClientIds();
    }

    /**
     * Obtiene información de zonas con soporte de refrigeración.
     * <p>
     * Retorna un mapa donde la clave es el ID de zona y el valor indica
     * si la zona soporta refrigeración.
     * </p>
     * <p>
     * Usada para pre-carga de contexto antes de procesar batch (evita N+1).
     * </p>
     *
     * @return Map {@code <zonaId, soportaRefrigeracion>}
     */
    @Override
    public Map<String, Boolean> getZonesWithRefrigerationSupport() {
        log.debug("Obteniendo zonas con capacidad de refrigeración desde Catalog BC");
        return catalogContextFacade.getAllZonesWithRefrigerationSupport();
    }

    /**
     * Verifica si un cliente existe y está activo.
     * <p>
     * Método simple para validaciones unitarias sin pre-carga de contexto.
     * </p>
     *
     * @param clientId ID del cliente
     * @return true si existe y está activo, false en caso contrario
     */
    @Override
    public boolean isClientActiveAndExists(String clientId) {
        log.debug("Verificando si cliente {} está activo", clientId);
        return catalogContextFacade.clientExistsAndIsActive(clientId);
    }

    /**
     * Verifica si una zona existe en el catálogo.
     * <p>
     * Método simple para validaciones unitarias sin pre-carga de contexto.
     * </p>
     *
     * @param zoneId ID de la zona
     * @return true si existe, false en caso contrario
     */
    @Override
    public boolean zoneExists(String zoneId) {
        log.debug("Verificando si zona {} existe", zoneId);
        return catalogContextFacade.zoneExists(zoneId);
    }

    /**
     * Verifica si una zona soporta refrigeración.
     *
     * @param zoneId ID de la zona
     * @return true si soporta refrigeración, false en caso contrario
     */
    @Override
    public boolean zoneSupportsRefrigeration(String zoneId) {
        log.debug("Verificando si zona {} soporta refrigeración", zoneId);
        return catalogContextFacade.zoneSupportsRefrigeration(zoneId);
    }
}
