package com.codares.logistics.ordering.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;

import com.codares.logistics.catalog.interfaces.acl.CatalogContextFacade;

/**
 * Servicio externo que adapta el ACL del Bounded Context de Catalog.
 * <p>
 * Actúa como Anti-Corruption Layer, permitiendo que el Ordering BC
 * acceda a validaciones de referencia (clientes, zonas) sin acoplarse
 * directamente a la implementación del Catalog.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Service
public class ExternalCatalogService {

    private final CatalogContextFacade catalogContextFacade;

    /**
     * Constructor que inyecta la fachada del Catalog BC.
     *
     * @param catalogContextFacade Fachada del contexto Catalog
     */
    public ExternalCatalogService(CatalogContextFacade catalogContextFacade) {
        this.catalogContextFacade = catalogContextFacade;
    }

    /**
     * Verifica si un cliente existe y está activo.
     *
     * @param clientId ID del cliente a validar
     * @return true si el cliente existe y está activo, false en caso contrario
     */
    public boolean isClientActiveAndExists(String clientId) {
        return catalogContextFacade.clientExistsAndIsActive(clientId);
    }

    /**
     * Verifica si una zona existe.
     *
     * @param zoneId ID de la zona a validar
     * @return true si la zona existe, false en caso contrario
     */
    public boolean isZoneValid(String zoneId) {
        return catalogContextFacade.zoneExists(zoneId);
    }

    /**
     * Verifica si una zona soporta refrigeración.
     *
     * @param zoneId ID de la zona a validar
     * @return true si la zona soporta refrigeración, false en caso contrario
     */
    public boolean canZoneRefrigerate(String zoneId) {
        return catalogContextFacade.zoneSupportsRefrigeration(zoneId);
    }
}
