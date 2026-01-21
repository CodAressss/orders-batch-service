package com.codares.logistics.iam.domain.model.entities;

import com.codares.logistics.iam.domain.model.valueobjects.Roles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

/**
 * Entidad de Rol.
 * <p>
 * Representa un rol (permiso) que puede ser asignado a usuarios en el sistema.
 * Los roles definen el conjunto de permisos y responsabilidades de un usuario,
 * actuando como intermediarios en la estrategia de autorización basada en roles (RBAC).
 * </p>
 * <p>
 * Características:
 * <ul>
 *   <li>Los roles son reutilizables y pueden asignarse a múltiples usuarios</li>
 *   <li>Se persisten en la base de datos con identidad única (Long id)</li>
 *   <li>El nombre del rol se almacena como STRING Enum para mantener integridad semántica</li>
 *   <li>Proporciona métodos de utilidad para conversión y validación</li>
 * </ul>
 * </p>
 * <p>
 * Roles predefinidos del sistema:
 * <ul>
 *   <li>ROLE_ADMIN: Acceso administrativo completo</li>
 *   <li>ROLE_USER: Acceso limitado de usuario estándar (rol por defecto)</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see Roles
 * @see User
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class Role {
    /**
     * Identificador único del rol.
     * <p>
     * Se genera automáticamente en la base de datos utilizando estrategia de identidad.
     * Este identificador es inmutable una vez asignado.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del rol en el sistema.
     * <p>
     * Almacenado como String Enum para garantizar que solo existan valores predefinidos
     * en la enumeración {@link Roles}. Tiene restricción de unicidad implícita a través
     * del enum.
     * </p>
     * <ul>
     *   <li>Longitud máxima: 20 caracteres</li>
     *   <li>Tipo de almacenamiento: STRING Enum</li>
     *   <li>Valores permitidos: {@link Roles#ROLE_ADMIN}, {@link Roles#ROLE_USER}</li>
     * </ul>
     *
     * @see Roles
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Roles name;

    /**
     * Constructor con nombre de rol.
     * <p>
     * Crea una nueva instancia de rol con el nombre especificado.
     * El nombre debe ser un valor válido de la enumeración {@link Roles}.
     * </p>
     *
     * @param name el nombre del rol desde la enumeración {@link Roles}
     * @throws NullPointerException si el nombre es nulo
     */
    public Role(Roles name) {
        this.name = name;
    }

    /**
     * Obtiene el nombre del rol como cadena de texto.
     * <p>
     * Convierte el valor enum del nombre del rol a su representación String.
     * Útil para logging, serialización y depuración.
     * </p>
     *
     * @return el nombre del rol como String (ej: "ROLE_ADMIN", "ROLE_USER")
     */
    public String getStringName() {
        return name.name();
    }

    /**
     * Obtiene el rol por defecto del sistema.
     * <p>
     * El rol por defecto es ROLE_USER, asignado automáticamente a nuevos usuarios
     * que no especifican roles explícitamente en el registro.
     * </p>
     *
     * @return una nueva instancia de {@link Role} con el nombre {@link Roles#ROLE_USER}
     */
    public static Role getDefaultRole() {
        return new Role(Roles.ROLE_USER);
    }

    /**
     * Convierte una cadena de texto al rol correspondiente.
     * <p>
     * Factory method que traduce una representación String de nombre de rol
     * a una instancia de {@link Role}. Útil para conversión desde fuentes externas
     * (JSON, CSV, bases de datos).
     * </p>
     *
     * @param name el nombre del rol como cadena (debe coincidir exactamente con {@link Roles})
     * @return una nueva instancia de {@link Role} con el nombre convertido
     * @throws IllegalArgumentException si la cadena no coincide con ningún nombre de rol válido
     */
    public static Role toRoleFromName(String name) {
        return new Role(Roles.valueOf(name));
    }

    /**
     * Valida y normaliza un conjunto de roles.
     * <p>
     * Garantiza que un usuario siempre tenga al menos el rol por defecto (ROLE_USER).
     * Si la lista proporcionada es nula o está vacía, retorna una lista con el rol por defecto.
     * Esta validación protege contra la creación de usuarios sin roles.
     * </p>
     *
     * @param roles la lista de roles a validar. Puede ser nula o estar vacía
     * @return una lista validada de roles. Mínimo: lista con el rol por defecto
     * @see #getDefaultRole()
     */
    public static List<Role> validateRoleSet(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of(getDefaultRole());
        }
        return roles;
    }

}
