package com.codares.logistics.iam.domain.services;

import com.codares.logistics.iam.domain.model.commands.SeedRolesCommand;

/**
 * Servicio de Dominio para Comandos de Roles.
 * <p>
 * Esta interfaz define el contrato para manejar comandos relacionados con la gestión de roles
 * en el contexto acotado de Gestión de Identidades y Acceso (IAM).
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 */
public interface RoleCommandService {
    /**
     * Maneja el comando de siembra de roles.
     * <p>
     * Ejecuta la inicialización idempotente de todos los roles predefinidos del sistema.
     * Si un rol ya existe en la base de datos, se omite; si no existe, se persiste.
     * </p>
     *
     * @param command el comando {@link SeedRolesCommand} que dispara la siembra
     */
    void handle(SeedRolesCommand command);
}
