package com.codares.logistics.iam.domain.services;


import com.codares.logistics.iam.domain.model.entities.Role;
import com.codares.logistics.iam.domain.model.queries.GetAllRolesQuery;
import com.codares.logistics.iam.domain.model.queries.GetRoleByNameQuery;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de Dominio para Consultas de Roles.
 * <p>
 * Esta interfaz define el contrato para manejar consultas (queries) relacionadas con la búsqueda
 * y recuperación de información sobre roles en el contexto acotado de Gestión de Identidades y Acceso (IAM).
 * Implementa el patrón CQRS para separación de responsabilidades de lectura.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Recuperar todos los roles disponibles en el sistema</li>
 *   <li>Buscar un rol específico por su nombre</li>
 *   <li>No modifica estado (operaciones de solo lectura)</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see Role
 * @see GetAllRolesQuery
 * @see GetRoleByNameQuery
 */
public interface RoleQueryService {
    /**
     * Maneja la consulta para obtener todos los roles del sistema.
     * <p>
     * Recupera la lista completa de todos los roles registrados sin filtros.
     * </p>
     *
     * @param query la consulta {@link GetAllRolesQuery}
     * @return una lista de todas las entidades {@link Role} disponibles. Retorna lista vacía si no hay roles
     */
    List<Role> handle(GetAllRolesQuery query);

    /**
     * Maneja la consulta para obtener un rol específico por su nombre.
     * <p>
     * Busca un único rol que coincida con el nombre especificado en la consulta.
     * </p>
     *
     * @param query la consulta {@link GetRoleByNameQuery} que contiene el nombre del rol
     * @return {@link Optional} con la entidad {@link Role} si existe, {@link Optional#empty()} en caso contrario
     */
    Optional<Role> handle(GetRoleByNameQuery query);
}
