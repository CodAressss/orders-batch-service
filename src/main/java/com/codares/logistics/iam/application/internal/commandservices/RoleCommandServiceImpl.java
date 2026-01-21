package com.codares.logistics.iam.application.internal.commandservices;

import com.codares.logistics.iam.domain.model.commands.SeedRolesCommand;
import com.codares.logistics.iam.domain.model.entities.Role;
import com.codares.logistics.iam.domain.model.valueobjects.Roles;
import com.codares.logistics.iam.domain.services.RoleCommandService;
import com.codares.logistics.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Implementación del servicio de comandos para roles.
 * <p>
 * Esta clase implementa la interfaz {@link RoleCommandService} y proporciona la lógica
 * para manejar comandos relacionados con los roles del sistema, como sembrar roles iniciales.
 * Garantiza la idempotencia al verificar que los roles no existan antes de crearlos.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see RoleCommandService
 * @see SeedRolesCommand
 */
@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param roleRepository el repositorio de roles para persistencia
     */
    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Maneja el comando para sembrar roles en el sistema.
     * <p>
     * Este método itera sobre todos los roles disponibles en la enumeración {@link Roles}
     * y los persiste en la base de datos si aún no existen. Esta operación es idempotente,
     * permitiendo ejecutarse múltiples veces sin crear duplicados.
     * </p>
     *
     * @param command el comando {@link SeedRolesCommand} que dispara la siembra de roles
     * @see Roles
     * @see SeedRolesCommand
     */
    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(Roles.values()).forEach(role -> {
            if(!roleRepository.existsByName(role)) {
                roleRepository.save(new Role(Roles.valueOf(role.name())));
            }
        } );
    }
}
