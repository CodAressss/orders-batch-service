package com.codares.logistics.iam.application.internal.queryservices;

import com.codares.logistics.iam.domain.model.aggregates.User;
import com.codares.logistics.iam.domain.model.queries.GetAllUsersQuery;
import com.codares.logistics.iam.domain.model.queries.GetUserByIdQuery;
import com.codares.logistics.iam.domain.model.queries.GetUserByUsernameQuery;
import com.codares.logistics.iam.domain.services.UserQueryService;
import com.codares.logistics.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de consultas para usuarios.
 * <p>
 * Esta clase implementa la interfaz {@link UserQueryService} y proporciona operaciones
 * de lectura para recuperar información de usuarios del sistema. Facilita la búsqueda
 * de usuarios por diferentes criterios como ID o nombre de usuario.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see UserQueryService
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param userRepository el repositorio de usuarios para acceso a datos
     */
    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Maneja la consulta para obtener todos los usuarios.
     * <p>
     * Recupera la lista completa de todos los usuarios registrados en el sistema.
     * </p>
     *
     * @param query la consulta {@link GetAllUsersQuery}
     * @return una lista de todos los usuarios del sistema
     * @see GetAllUsersQuery
     */
    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }

    /**
     * Maneja la consulta para obtener un usuario por su ID.
     * <p>
     * Busca un usuario específico en el sistema por su identificador único (UUID).
     * </p>
     *
     * @param query la consulta {@link GetUserByIdQuery} que contiene el ID del usuario
     * @return {@link Optional} con el usuario si existe, {@link Optional#empty()} en caso contrario
     * @see GetUserByIdQuery
     */
    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    /**
     * Maneja la consulta para obtener un usuario por su nombre de usuario.
     * <p>
     * Busca un usuario específico en el sistema por su nombre de usuario único,
     * útil para autenticación y búsqueda de usuarios.
     * </p>
     *
     * @param query la consulta {@link GetUserByUsernameQuery} que contiene el nombre de usuario
     * @return {@link Optional} con el usuario si existe, {@link Optional#empty()} en caso contrario
     * @see GetUserByUsernameQuery
     */
    @Override
    public Optional<User> handle(GetUserByUsernameQuery query) {
        return userRepository.findByUsername(query.username());
    }
}
