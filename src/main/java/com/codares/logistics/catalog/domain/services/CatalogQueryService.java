package com.codares.logistics.catalog.domain.services;

import java.util.List;
import java.util.Optional;

import com.codares.logistics.catalog.domain.model.aggregates.Client;
import com.codares.logistics.catalog.domain.model.aggregates.Zone;
import com.codares.logistics.catalog.domain.model.queries.GetAllActiveClientsQuery;
import com.codares.logistics.catalog.domain.model.queries.GetAllZonesQuery;
import com.codares.logistics.catalog.domain.model.queries.GetClientByIdQuery;
import com.codares.logistics.catalog.domain.model.queries.GetZoneByIdQuery;

/**
 * Servicio de consultas para el catálogo (datos referenciales).
 * <p>
 * Proporciona acceso de solo lectura a clientes y zonas, que actúan como
 * fuente de verdad para validaciones en otros Bounded Contexts.
 * Sigue el patrón CQRS con solo operaciones de lectura.
 * </p>
 * <p>
 * Incluye métodos batch para pre-carga de catálogos en operaciones masivas.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface CatalogQueryService {

    // ═══════════════════════════════════════════════════════════════
    // QUERIES UNITARIAS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Obtiene un cliente por su ID.
     *
     * @param query la consulta que contiene el ID del cliente
     * @return un Optional con el Client si existe, vacío en caso contrario
     */
    Optional<Client> handle(GetClientByIdQuery query);

    /**
     * Obtiene una zona por su ID.
     *
     * @param query la consulta que contiene el ID de la zona
     * @return un Optional con la Zone si existe, vacío en caso contrario
     */
    Optional<Zone> handle(GetZoneByIdQuery query);

    // ═══════════════════════════════════════════════════════════════
    // QUERIES BATCH (para pre-carga en procesamiento masivo)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Obtiene todos los clientes activos.
     * <p>
     * Uso recomendado: Pre-cargar antes de procesar CSV para evitar N queries.
     * </p>
     *
     * @param query la consulta para obtener clientes activos
     * @return lista de clientes activos
     */
    List<Client> handle(GetAllActiveClientsQuery query);

    /**
     * Obtiene todas las zonas.
     * <p>
     * Uso recomendado: Pre-cargar antes de procesar CSV para evitar N queries.
     * </p>
     *
     * @param query la consulta para obtener todas las zonas
     * @return lista de todas las zonas
     */
    List<Zone> handle(GetAllZonesQuery query);
}
