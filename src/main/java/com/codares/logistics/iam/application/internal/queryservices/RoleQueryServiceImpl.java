package com.codares.logistics.iam.application.internal.queryservices;

import com.codares.logistics.iam.domain.model.entities.Role;
import com.codares.logistics.iam.domain.model.queries.GetAllRolesQuery;
import com.codares.logistics.iam.domain.model.queries.GetRoleByNameQuery;
import com.codares.logistics.iam.domain.services.RoleQueryService;
import com.codares.logistics.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de consultas para roles.
 * <p>
 * Esta clase implementa la interfaz {@link RoleQueryService} y proporciona operaciones
 * de lectura para recuperar información de roles del sistema. Facilita la búsqueda
 * de roles por diferentes criterios.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see RoleQueryService
 */
@Service
public class RoleQueryServiceImpl implements RoleQueryService {
    private final RoleRepository roleRepository;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param roleRepository el repositorio de roles para acceso a datos
     */
    public RoleQueryServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Maneja la consulta para obtener todos los roles.
     * <p>
     * Recupera la lista completa de todos los roles disponibles en el sistema.
     * </p>
     *
     * @param query la consulta {@link GetAllRolesQuery}
     * @return una lista de todos los roles del sistema
     * @see GetAllRolesQuery
     */
    @Override
    public List<Role> handle(GetAllRolesQuery query) {
        return roleRepository.findAll();
    }

    /**
     * Maneja la consulta para obtener un rol por su nombre.
     * <p>
     * Busca un rol específico en el sistema por su nombre único.
     * </p>
     *
     * @param query la consulta {@link GetRoleByNameQuery} que contiene el nombre del rol
     * @return {@link Optional} con el rol si existe, {@link Optional#empty()} en caso contrario
     * @see GetRoleByNameQuery
     */
    @Override
    public Optional<Role> handle(GetRoleByNameQuery query) {
        return roleRepository.findByName(query.name());
    }
}
