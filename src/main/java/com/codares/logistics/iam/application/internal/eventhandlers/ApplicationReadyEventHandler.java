package com.codares.logistics.iam.application.internal.eventhandlers;

import com.codares.logistics.iam.domain.model.commands.SeedRolesCommand;
import com.codares.logistics.iam.domain.services.RoleCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * Manejador de eventos de inicialización de la aplicación.
 * <p>
 * Esta clase escucha el evento {@link ApplicationReadyEvent} de Spring Boot y ejecuta
 * la inicialización de roles del sistema cuando la aplicación ha terminado de iniciar.
 * Garantiza que los roles necesarios estén presentes en la base de datos desde el inicio.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see ApplicationReadyEvent
 */
@Service
public class ApplicationReadyEventHandler {
    private final RoleCommandService roleCommandService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    /**
     * Constructor para inyección de dependencias.
     *
     * @param roleCommandService el servicio de comandos de roles para la siembra inicial
     */
    public ApplicationReadyEventHandler(RoleCommandService roleCommandService) {
        this.roleCommandService = roleCommandService;
    }

    /**
     * Maneja el evento de aplicación lista.
     * <p>
     * Este método se ejecuta automáticamente cuando Spring Boot ha completado la inicialización.
     * Dispara el comando para sembrar los roles del sistema, garantizando que estén disponibles
     * para su uso en toda la aplicación.
     * </p>
     *
     * @param event el evento {@link ApplicationReadyEvent} que dispara este manejador
     * @see ApplicationReadyEvent
     * @see SeedRolesCommand
     */
    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();
        LOGGER.info("Starting to verify if roles seeding is needed for {} at {}", applicationName, currentTimestamp());
        var seedRolesCommand = new SeedRolesCommand();
        roleCommandService.handle(seedRolesCommand);
        LOGGER.info("Roles seeding verification finished for {} at {}", applicationName, currentTimestamp());
    }

    private Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
