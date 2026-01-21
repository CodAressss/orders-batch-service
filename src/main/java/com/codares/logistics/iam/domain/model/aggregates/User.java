package com.codares.logistics.iam.domain.model.aggregates;

import com.codares.logistics.iam.domain.model.entities.Role;
import com.codares.logistics.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Raíz de Agregado para Usuarios (User Aggregate Root).
 * <p>
 * Esta clase representa el agregado raíz para la entidad Usuario en el contexto acotado de Identidad y Acceso (IAM).
 * Gestiona la identidad del usuario, sus credenciales y sus roles en el sistema. Extiende
 * {@link AuditableAbstractAggregateRoot} para incluir capacidades de auditoría automática y publicación de eventos de dominio.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Mantener la integridad del perfil de usuario (nombre de usuario, contraseña, roles)</li>
 *   <li>Validar invariantes de negocio (unicidad de nombre de usuario, contraseña válida)</li>
 *   <li>Gestionar roles y permisos asociados</li>
 *   <li>Registrar eventos de dominio (creación, modificación, eliminación de usuario)</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see AuditableAbstractAggregateRoot
 * @see Role
 */
@Getter
@Setter
@Entity
public class User extends AuditableAbstractAggregateRoot<User> {

    /**
     * Identificador único universal (UUID) del usuario.
     * <p>
     * Se genera automáticamente utilizando UUID v4 en la capa de persistencia.
     * Mapeado al tipo nativo 'uuid' de PostgreSQL para máxima eficiencia.
     * Este campo es inmutable una vez creado y no puede ser actualizado.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Nombre de usuario único del sistema.
     * <p>
     * Identificador único para el usuario. Utilizado para autenticación y referencia en logs.
     * Tiene restricción de unicidad en la base de datos para garantizar que no haya duplicados.
     * </p>
     * <ul>
     *   <li>Longitud máxima: 50 caracteres</li>
     *   <li>Restricción: NOT BLANK, UNIQUE</li>
     * </ul>
     */
    @NotBlank
    @Size(max = 50)
    @Column(unique = true)
    private String username;

    /**
     * Contraseña cifrada del usuario.
     * <p>
     * Almacena la contraseña del usuario en formato hasheado utilizando BCrypt.
     * Nunca se almacena en texto plano por razones de seguridad.
     * La contraseña debe cumplir con requisitos mínimos de complejidad.
     * </p>
     * <ul>
     *   <li>Longitud máxima: 120 caracteres (hash BCrypt)</li>
     *   <li>Restricción: NOT BLANK</li>
     *   <li>Algoritmo: BCrypt</li>
     * </ul>
     */
    @NotBlank
    @Size(max = 120)
    private String password;

    /**
     * Conjunto de roles asignados al usuario.
     * <p>
     * Representa los permisos y responsabilidades del usuario en el sistema.
     * La relación es muchos-a-muchos, permitiendo que un usuario tenga múltiples roles
     * y un rol sea asignado a múltiples usuarios.
     * Los roles se cargan ávidamente (EAGER) para garantizar disponibilidad inmediata.
     * </p>
     * <ul>
     *   <li>Relación: ManyToMany</li>
     *   <li>Fetch: EAGER (carga inmediata)</li>
     *   <li>Cascade: ALL (propagar operaciones)</li>
     *   <li>Tabla de unión: user_roles</li>
     * </ul>
     *
     * @see Role
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(	name = "user_roles",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    /**
     * Constructor sin argumentos (requerido por JPA).
     * <p>
     * Inicializa el conjunto de roles como un HashSet vacío.
     * </p>
     */
    public User() {
        this.roles = new HashSet<>();
    }

    /**
     * Constructor con nombre de usuario y contraseña.
     * <p>
     * Crea un usuario con las credenciales especificadas e inicializa el conjunto de roles vacío.
     * La contraseña debe estar hasheada antes de ser pasada a este constructor.
     * </p>
     *
     * @param username el nombre de usuario único del sistema
     * @param password la contraseña hasheada del usuario
     * @throws IllegalArgumentException si el nombre de usuario o la contraseña son nulos o vacíos
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
    }

    /**
     * Constructor con nombre de usuario, contraseña y roles iniciales.
     * <p>
     * Crea un usuario con las credenciales especificadas y asigna los roles proporcionados.
     * Este constructor es útil durante el registro de nuevos usuarios con roles predefinidos.
     * </p>
     *
     * @param username el nombre de usuario único del sistema
     * @param password la contraseña hasheada del usuario
     * @param roles la lista de roles a asignar al usuario. Si es nula o vacía, se asigna el rol por defecto (ROLE_USER)
     * @throws IllegalArgumentException si el nombre de usuario o la contraseña son nulos o vacíos
     */
    public User(String username, String password, List<Role> roles) {
        this(username, password);
        addRoles(roles);
    }

    /**
     * Añade un rol individual al usuario.
     * <p>
     * Permite agregar un nuevo rol al conjunto de roles existentes. Si el rol ya existe,
     * el HashSet no lo duplicará gracias a su semántica de conjuntos.
     * </p>
     *
     * @param role el rol a añadir. No debe ser nulo
     * @return la instancia del usuario para permitir encadenamiento de métodos (Builder Pattern)
     * @throws NullPointerException si el rol es nulo
     */
    public User addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    /**
     * Añade múltiples roles al usuario.
     * <p>
     * Permite agregar una lista de roles al conjunto de roles existentes.
     * La lista se valida utilizando {@link Role#validateRoleSet(List)} para garantizar
     * que siempre tenga al menos el rol por defecto (ROLE_USER).
     * </p>
     *
     * @param roles la lista de roles a añadir. Si es nula o vacía, se añade el rol por defecto
     * @return la instancia del usuario para permitir encadenamiento de métodos (Builder Pattern)
     * @see Role#validateRoleSet(List)
     */
    public User addRoles(List<Role> roles) {
        var validatedRoleSet = Role.validateRoleSet(roles);
        this.roles.addAll(validatedRoleSet);
        return this;
    }

}
