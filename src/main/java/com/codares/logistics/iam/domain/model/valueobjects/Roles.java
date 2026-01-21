package com.codares.logistics.iam.domain.model.valueobjects;

/**
 * Enumeración de Roles del Sistema.
 * <p>
 * Define los roles predefinidos disponibles en el sistema para la autorización basada en roles (RBAC).
 * Estos roles son los únicos valores permitidos para la asignación a usuarios.
 * </p>
 * <p>
 * Roles disponibles:
 * <ul>
 *   <li>{@link #ROLE_USER}: Rol estándar para usuarios normales. Acceso limitado a funcionalidades básicas. Es el rol por defecto asignado a nuevos usuarios.</li>
 *   <li>{@link #ROLE_ADMIN}: Rol administrativo. Acceso completo a todas las funcionalidades del sistema incluyendo gestión de usuarios y configuración.</li>
 * </ul>
 * </p>
 * <p>
 * Características:
 * <ul>
 *   <li>Valores predefinidos: No puede haber roles adicionales sin modificar este enum</li>
 *   <li>Persistencia: Se almacenan como STRING en la base de datos (columna 'name' en tabla 'role')</li>
 *   <li>Seguridad: La enumeración garantiza que solo existan valores válidos</li>
 *   <li>Escalabilidad: Nuevos roles pueden agregarse modificando este enum</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see Role\n * @see User
 */
public enum Roles {
    /**
     * Rol de usuario estándar.
     * <p>
     * Representa el acceso limitado para usuarios normales del sistema.
     * Es asignado automáticamente como rol por defecto a nuevos usuarios.
     * </p>
     */
    ROLE_USER,

    /**
     * Rol de administrador del sistema.
     * <p>
     * Representa acceso administrativo completo.
     * Permite gestionar usuarios, roles, configuración global y otras funcionalidades protegidas.
     * </p>
     */
    ROLE_ADMIN,
}
