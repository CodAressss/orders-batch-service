package com.codares.logistics.iam.domain.services;

import com.codares.logistics.iam.domain.model.aggregates.User;
import com.codares.logistics.iam.domain.model.queries.GetAllUsersQuery;
import com.codares.logistics.iam.domain.model.queries.GetUserByIdQuery;
import com.codares.logistics.iam.domain.model.queries.GetUserByUsernameQuery;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de Dominio para Consultas de Usuarios.
 * <p>
 * Esta interfaz define el contrato para manejar consultas (queries) relacionadas con la búsqueda
 * y recuperación de información sobre usuarios en el contexto acotado de Gestión de Identidades y Acceso (IAM).
 * Implementa el patrón CQRS para separación de responsabilidades de lectura.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Recuperar todos los usuarios del sistema</li>
 *   <li>Buscar un usuario específico por su ID único</li>
 *   <li>Buscar un usuario específico por su nombre de usuario</li>
 *   <li>No modifica estado (operaciones de solo lectura)</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see User
 * @see GetAllUsersQuery
 * @see GetUserByIdQuery
 * @see GetUserByUsernameQuery
 */
public interface UserQueryService {
    /**
     * Maneja la consulta para obtener todos los usuarios del sistema.
     * <p>
     * Recupera la lista completa de todos los usuarios registrados sin filtros.
     * Puede retornar una lista grande, usar con cuidado en producción.
     * </p>
     *
     * @param query la consulta {@link GetAllUsersQuery}
     * @return una lista de todas las entidades {@link User}. Retorna lista vacía si no hay usuarios
     */
    List<User> handle(GetAllUsersQuery query);

    /**
     * Maneja la consulta para obtener un usuario por su identificador único.
     * <p>
     * Busca un único usuario que coincida con el UUID especificado.
     * </p>
     *
     * @param query la consulta {@link GetUserByIdQuery} que contiene el UUID del usuario
     * @return {@link Optional} con la entidad {@link User} si existe, {@link Optional#empty()} en caso contrario
     */
    Optional<User> handle(GetUserByIdQuery query);

    /**
     * Maneja la consulta para obtener un usuario por su nombre de usuario.
     * <p>
     * Busca un único usuario que coincida con el nombre de usuario especificado.
     * El nombre de usuario es único en el sistema, por lo que retorna máximo un resultado.
     * </p>
     *
     * @param query la consulta {@link GetUserByUsernameQuery} que contiene el nombre de usuario
     * @return {@link Optional} con la entidad {@link User} si existe, {@link Optional#empty()} en caso contrario
     */
    Optional<User> handle(GetUserByUsernameQuery query);

}
