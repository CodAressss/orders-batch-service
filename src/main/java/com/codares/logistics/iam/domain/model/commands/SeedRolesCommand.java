package com.codares.logistics.iam.domain.model.commands;

/**
 * Comando de Siembra de Roles (Seed Roles).
 * <p>
 * Record inmutable que dispara la siembra (inicialización) de los roles predefinidos del sistema.
 * Este es un comando vacío que actúa como señal para ejecutar la siembra de roles sin transportar datos adicionales.
 * </p>
 * <p>
 * Propósito: Garantizar que todos los roles definidos en la enumeración {@link com.codares.logistics.iam.domain.model.valueobjects.Roles}
 * estén presentes en la base de datos al iniciar la aplicación de manera idempotente.
 * </p>
 * <p>
 * Flujo de ejecución:
 * <ol>
 *   <li>Spring Boot emite {@link org.springframework.boot.context.event.ApplicationReadyEvent}</li>
 *   <li>{@link com.codares.logistics.iam.application.internal.eventhandlers.ApplicationReadyEventHandler} escucha el evento</li>
 *   <li>Crea instancia de SeedRolesCommand</li>
 *   <li>Dispara {@link com.codares.logistics.iam.domain.services.RoleCommandService#handle(SeedRolesCommand)}</li>
 *   <li>El servicio itera sobre todos los roles y los persiste si no existen (idempotencia)</li>
 * </ol>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.application.internal.eventhandlers.ApplicationReadyEventHandler
 * @see com.codares.logistics.iam.domain.services.RoleCommandService
 * @see com.codares.logistics.iam.domain.model.valueobjects.Roles
 */
public record SeedRolesCommand() {
}
