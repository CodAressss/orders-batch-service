package com.codares.logistics.iam.infrastructure.persistence.jpa.repositories;

import com.codares.logistics.iam.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de Acceso a Datos para la Entidad Usuario.
 * <p>
 * Esta interfaz proporciona operaciones CRUD y consultas especializadas para la entidad {@link User}
 * en la base de datos. Extiende {@link JpaRepository} para heredar funcionalidades estándar de Hibernate/Spring Data.
 * </p>
 * <p>
 * Características especiales:
 * <ul>
 *   <li>Soporta UUID como tipo de identificador primario</li>
 *   <li>Incluye consultas JPQL personalizadas con JOIN FETCH para optimización</li>
 *   <li>Evita problemas de LazyInitializationException con eager loading de roles</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.model.aggregates.User
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>
{
    /**
     * Busca un usuario por su nombre de usuario con roles cargados eagerly.
     * <p>
     * Utiliza LEFT JOIN FETCH para asegurar que los roles se cargan en una única consulta,
     * evitando {@link org.hibernate.LazyInitializationException} al acceder a los roles fuera
     * del contexto de la sesión de Hibernate. Esto es crítico para autenticación stateless.
     * </p>
     *
     * @param username el nombre de usuario único a buscar. No puede ser null
     * @return {@link Optional} con el usuario y sus roles si existe, {@link Optional#empty()} en caso contrario
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsername(String username);

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado.
     * <p>
     * Realiza una consulta eficiente COUNT para determinar existencia sin cargar la entidad completa.
     * Útil para validaciones de unicidad antes de crear nuevos usuarios.
     * </p>
     *
     * @param username el nombre de usuario a verificar
     * @return {@code true} si el usuario existe, {@code false} en caso contrario
     */
    boolean existsByUsername(String username);

}
