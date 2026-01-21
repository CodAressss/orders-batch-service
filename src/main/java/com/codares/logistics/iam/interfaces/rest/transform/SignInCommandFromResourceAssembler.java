package com.codares.logistics.iam.interfaces.rest.transform;

import com.codares.logistics.iam.domain.model.commands.SignInCommand;
import com.codares.logistics.iam.interfaces.rest.resources.SignInResource;

/**
 * Ensamblador para transformar DTO REST → Comando de Dominio (Sign-In).
 * <p>
 * Implementa el patrón Assembler para convertir {@link SignInResource} (DTO de entrada REST)
 * a {@link SignInCommand} (objeto de dominio). Actúa como puente entre la capa de interfaz REST
 * y la capa de dominio, permitiendo que cambios en una no afecten la otra.
 * </p>
 * <p>
 * <strong>Propósito:</strong>
 * </p>
 * <ul>
 *   <li>Traducir datos REST a Commands de dominio</li>
 *   <li>Centralizar lógica de transformación SignInResource → SignInCommand</li>
 *   <li>Facilitar cambios en estructura de API REST sin afectar dominio</li>
 *   <li>Mantener separación de responsabilidades entre capas</li>
 * </ul>
 * <p>
 * <strong>Flujo de Procesamiento:</strong>
 * </p>
 * <ol>
 *   <li>Cliente envía POST /authentication/sign-in con JSON SignInResource</li>
 *   <li>Spring deserializa JSON a objeto SignInResource</li>
 *   <li>AuthenticationController delega a este Assembler para transformar</li>
 *   <li>Assembler extrae username y password del DTO</li>
 *   <li>Crea SignInCommand (objeto de dominio)</li>
 *   <li>CommandService recibe Command para procesarlo</li>
 * </ol>
 * <p>
 * <strong>Transformación de Datos:</strong>
 * </p>
 * <ul>
 *   <li>SignInResource.username() → SignInCommand.username()</li>
 *   <li>SignInResource.password() → SignInCommand.password()</li>
 * </ul>
 * <p>
 * <strong>Notas de Implementación:</strong>
 * </p>
 * <ul>
 *   <li>Método estático para facilitar invocación desde Controlador</li>
 *   <li>Sin estado - solo realiza transformación</li>
 *   <li>NO valida datos - solo traduce estructura</li>
 *   <li>Validación ocurre después en CommandService o Value Objects</li>
 * </ul>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see SignInResource
 * @see SignInCommand
 * @see com.codares.logistics.iam.interfaces.rest.AuthenticationController#signIn(SignInResource)
 * @see com.codares.logistics.iam.domain.services.UserCommandService
 */
public class SignInCommandFromResourceAssembler {

    /**
     * Transforma un DTO REST SignInResource a Comando de Dominio SignInCommand.
     * <p>
     * Extrae los datos de autenticación (username y password) del DTO REST
     * y los empaqueta en un Command listo para ser procesado por el CommandService.
     * </p>
     * <p>
     * <strong>Responsabilidades:</strong>
     * </p>
     * <ul>
     *   <li>Traducir estructura de datos REST → Dominio</li>
     *   <li>NO validar datos (eso es responsabilidad del Command/Value Object)</li>
     *   <li>NO contener lógica de negocio</li>
     * </ul>
     * <p>
     * <strong>Notas Técnicas:</strong>
     * </p>
     * <ul>
     *   <li>Los datos se extraen directamente del record (no necesita getters)</li>
     *   <li>El Command resultante es un record inmutable de dominio</li>
     *   <li>La contraseña se pasa sin modificar (sin encriptar aún)</li>
     * </ul>
     *
     * @param signInResource DTO con credenciales de entrada (username, password)
     * @return SignInCommand objeto de dominio listo para CommandService
     * @see SignInResource
     * @see SignInCommand
     */
    public static SignInCommand toCommandFromResource(SignInResource signInResource) {
        return new SignInCommand(signInResource.username(), signInResource.password());
    }
}
