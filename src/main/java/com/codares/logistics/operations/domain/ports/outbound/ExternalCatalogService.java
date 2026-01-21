package com.codares.logistics.operations.domain.ports.outbound;

import java.util.Map;
import java.util.Set;

/**
 * Puerto de salida para acceder al catálogo de clientes y zonas.
 * <p>
 * Define el contrato que el dominio requiere para validar datos de referencia.
 * La implementación concreta (ACL) reside en la capa de aplicación.
 * </p>
 * <p>
 * Diseñado para soportar pre-carga de datos en operaciones batch,
 * evitando el problema N+1 mediante métodos que retornan colecciones completas.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface ExternalCatalogService {

    /**
     * Obtiene todos los IDs de clientes activos disponibles en el catálogo.
     * <p>
     * Usada para pre-carga de contexto antes de procesar batch (evita N+1).
     * </p>
     *
     * @return Set con los IDs de clientes activos
     */
    Set<String> getAllActiveClientIds();

    /**
     * Obtiene información de zonas con soporte de refrigeración.
     * <p>
     * Retorna un mapa donde la clave es el ID de zona y el valor indica
     * si la zona soporta refrigeración.
     * </p>
     *
     * @return Map {@code <zonaId, soportaRefrigeracion>}
     */
    Map<String, Boolean> getZonesWithRefrigerationSupport();

    /**
     * Verifica si un cliente existe y está activo.
     *
     * @param clientId ID del cliente
     * @return true si existe y está activo, false en caso contrario
     */
    boolean isClientActiveAndExists(String clientId);

    /**
     * Verifica si una zona existe en el catálogo.
     *
     * @param zoneId ID de la zona
     * @return true si existe, false en caso contrario
     */
    boolean zoneExists(String zoneId);

    /**
     * Verifica si una zona soporta refrigeración.
     *
     * @param zoneId ID de la zona
     * @return true si soporta refrigeración, false en caso contrario
     */
    boolean zoneSupportsRefrigeration(String zoneId);
}
