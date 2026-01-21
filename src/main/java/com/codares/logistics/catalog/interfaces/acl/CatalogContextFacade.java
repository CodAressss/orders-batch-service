package com.codares.logistics.catalog.interfaces.acl;

import java.util.Map;
import java.util.Set;

/**
 * Fachada del Bounded Context de Catalog.
 * <p>
 * Proporciona acceso simplificado a los datos referenciales (clientes y zonas)
 * para que otros Bounded Contexts puedan validar sus operaciones.
 * Actúa como Anti-Corruption Layer entre contextos.
 * </p>
 * <p>
 * Incluye métodos batch para pre-carga de catálogos en operaciones masivas,
 * evitando el problema N+1 en procesamiento de archivos CSV.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface CatalogContextFacade {

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS UNITARIOS (para validaciones individuales)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Verifica si un cliente existe y está activo.
     *
     * @param clientId ID del cliente
     * @return true si el cliente existe y está activo, false en caso contrario
     */
    boolean clientExistsAndIsActive(String clientId);

    /**
     * Verifica si una zona existe.
     *
     * @param zoneId ID de la zona
     * @return true si la zona existe, false en caso contrario
     */
    boolean zoneExists(String zoneId);

    /**
     * Obtiene información de soporte de refrigeración de una zona.
     *
     * @param zoneId ID de la zona
     * @return true si la zona soporta refrigeración, false en caso contrario o si no existe
     */
    boolean zoneSupportsRefrigeration(String zoneId);

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS BATCH (para pre-carga en procesamiento masivo)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Obtiene todos los IDs de clientes activos.
     * <p>
     * Uso recomendado: Pre-cargar antes de procesar CSV para evitar N queries.
     * </p>
     *
     * @return Set con los IDs de clientes activos
     */
    Set<String> getAllActiveClientIds();

    /**
     * Obtiene todas las zonas con su información de refrigeración.
     * <p>
     * Uso recomendado: Pre-cargar antes de procesar CSV para evitar N queries.
     * El valor Boolean indica si la zona soporta refrigeración.
     * </p>
     *
     * @return Map con ID de zona como clave y soporte de refrigeración como valor
     */
    Map<String, Boolean> getAllZonesWithRefrigerationSupport();
}
