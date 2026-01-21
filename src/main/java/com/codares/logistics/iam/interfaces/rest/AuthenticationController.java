package com.codares.logistics.iam.interfaces.rest;

import com.codares.logistics.iam.domain.services.UserCommandService;
import com.codares.logistics.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.codares.logistics.iam.interfaces.rest.resources.SignInResource;
import com.codares.logistics.iam.interfaces.rest.resources.SignUpResource;
import com.codares.logistics.iam.interfaces.rest.resources.UserResource;
import com.codares.logistics.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.codares.logistics.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.codares.logistics.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.codares.logistics.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para Autenticación y Registro de Usuarios.
 * <p>
 * Este controlador expone los endpoints públicos para que los clientes se autentiquen
 * (obtengan un token JWT) o se registren como nuevos usuarios en el sistema.
 * No requiere autenticación previa.
 * </p>
 * <p>
 * <strong>Endpoints Públicos:</strong>
 * </p>
 * <ul>
 *   <li><code>POST /api/v1/authentication/sign-in</code> - Autenticación (login)</li>
 *   <li><code>POST /api/v1/authentication/sign-up</code> - Registro (signup)</li>
 * </ul>
 * <p>
 * <strong>Flujo de Sign-In:</strong>
 * </p>
 * <ol>
 *   <li>Cliente envía credenciales (username/email + contraseña)</li>
 *   <li>Assembler convierte Resource → Command</li>
 *   <li>CommandService valida credenciales contra BD</li>
 *   <li>Si son válidas, genera token JWT</li>
 *   <li>Retorna usuario + token (HTTP 200)</li>
 *   <li>Cliente almacena token para solicitudes posteriores</li>
 * </ol>
 * <p>
 * <strong>Flujo de Sign-Up:</strong>
 * </p>
 * <ol>
 *   <li>Cliente envía datos de nuevo usuario (username + contraseña)</li>
 *   <li>Assembler convierte Resource → Command</li>
 *   <li>CommandService valida y crea usuario</li>
 *   <li>Contraseña se hashea con BCrypt antes de persistir</li>
 *   <li>Usuario se asigna a rol "USER" por defecto</li>
 *   <li>Retorna usuario creado (HTTP 201)</li>
 * </ol>
 * <p>
 * <strong>Patrón: Assembler Pattern</strong>
 * </p>
 * <p>
 * Los ensambladores traducen entre capas de presentación (REST) y dominio (Commands),
 * manteniendo la separación de responsabilidades y permitiendo cambios en un lado
 * sin afectar al otro.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.domain.model.commands.SignInCommand
 * @see com.codares.logistics.iam.domain.model.commands.SignUpCommand
 * @see com.codares.logistics.iam.domain.services.UserCommandService
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Available Authentication Endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;

    /**
     * Constructor que inyecta el servicio de comandos de usuario.
     *
     * @param userCommandService servicio que orquesta la autenticación y registro
     */
    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Endpoint para autenticación (Sign-In / Login).
     * <p>
     * Valida las credenciales del usuario (username/email + contraseña) y si son correctas,
     * genera un token JWT y devuelve la información del usuario autenticado.
     * </p>
     * <p>
     * <strong>Flujo de Procesamiento:</strong>
     * </p>
     * <ol>
     *   <li>Recibe {@link SignInResource} con credenciales en JSON</li>
     *   <li>Usa {@link SignInCommandFromResourceAssembler} para convertir a Command</li>
     *   <li>Delega a {@link UserCommandService#handle(SignInCommand)} para validar</li>
     *   <li>Si credenciales válidas: genera JWT y retorna usuario + token</li>
     *   <li>Si credenciales inválidas: retorna HTTP 404 (usuario no encontrado)</li>
     * </ol>
     * <p>
     * <strong>Seguridad:</strong> La contraseña se compara usando BCrypt (resistente a timing attacks).
     * El token JWT se genera con roles del usuario incrustados.
     * </p>
     *
     * @param signInResource DTO con username/email y contraseña
     * @return ResponseEntity&lt;AuthenticatedUserResource&gt; usuario + token JWT (HTTP 200)
     *         o HTTP 404 si las credenciales no son válidas
     * @see SignInResource
     * @see AuthenticatedUserResource
     * @see SignInCommandFromResourceAssembler
     */
    @PostMapping("/sign-in")
    @Operation(summary = "Sign-in", description = "Sign-in with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully."),
            @ApiResponse(responseCode = "404", description = "User not found.")})
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var authenticatedUser = userCommandService.handle(signInCommand);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(authenticatedUser.get().getLeft(), authenticatedUser.get().getRight());
        return ResponseEntity.ok(authenticatedUserResource);
    }

    /**
     * Endpoint para registro de nuevo usuario (Sign-Up).
     * <p>
     * Crea un nuevo usuario en el sistema con los datos proporcionados.
     * La contraseña se hashea automáticamente con BCrypt y el usuario se asigna
     * al rol "USER" por defecto.
     * </p>
     * <p>
     * <strong>Flujo de Procesamiento:</strong>
     * </p>
     * <ol>
     *   <li>Recibe {@link SignUpResource} con username/email y contraseña en JSON</li>
     *   <li>Usa {@link SignUpCommandFromResourceAssembler} para convertir a Command</li>
     *   <li>Delega a {@link UserCommandService#handle(SignUpCommand)} para crear usuario</li>
     *   <li>Si creación exitosa: retorna usuario creado (HTTP 201 Created)</li>
     *   <li>Si hay error (ej: usuario duplicado): retorna HTTP 400 (Bad Request)</li>
     * </ol>
     * <p>
     * <strong>Validaciones Realizadas:</strong>
     * </p>
     * <ul>
     *   <li>Username no debe ser nulo o vacío</li>
     *   <li>Contraseña debe cumplir con políticas de seguridad</li>
     *   <li>Username debe ser único en la BD</li>
     * </ul>
     *
     * @param signUpResource DTO con datos del nuevo usuario (username, contraseña, etc.)
     * @return ResponseEntity&lt;UserResource&gt; usuario creado (HTTP 201)
     *         o HTTP 400 si hay error en la creación
     * @see SignUpResource
     * @see UserResource
     * @see SignUpCommandFromResourceAssembler
     */
    @PostMapping("/sign-up")
    @Operation(summary = "Sign-up", description = "Sign-up with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")})
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);

    }
}
