package com.codares.logistics.iam.infrastructure.persistence.jpa.repositories;

import com.codares.logistics.iam.domain.model.entities.Role;
import com.codares.logistics.iam.domain.model.valueobjects.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de Acceso a Datos para la Entidad Rol.
 * <p>
 * Esta interfaz proporciona operaciones CRUD y consultas especializadas para la entidad {@link Role}
 * en la base de datos. Extiende {@link JpaRepository} para heredar funcionalidades estándar de Hibernate/Spring Data.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Persistencia de roles en la base de datos</li>
 *   <li>Búsqueda de roles por nombre único</li>
 *   <li>Verificación de existencia de roles</li>
 *   <li>Operaciones CRUD estándar heredadas</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.model.entities.Role
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca un rol por su nombre único.
     * <p>
     * Realiza una consulta SELECT por el campo name en la tabla role.
     * El nombre debe ser uno de los valores válidos del enum {@link Roles}.
     * </p>
     *
     * @param name el nombre del rol a buscar (valor del enum {@link Roles})
     * @return {@link Optional} con el rol si existe, {@link Optional#empty()} en caso contrario
     */
    Optional<Role> findByName(Roles name);

    /**
     * Verifica si existe un rol con el nombre especificado.
     * <p>
     * Realiza una consulta eficiente COUNT para determinar existencia sin cargar la entidad completa.
     * Útil para validaciones de idempotencia en la siembra de roles (seeding).
     * </p>
     *
     * @param name el nombre del rol a verificar (valor del enum {@link Roles})
     * @return {@code true} si el rol existe, {@code false} en caso contrario
     */
    boolean existsByName(Roles name);

}
