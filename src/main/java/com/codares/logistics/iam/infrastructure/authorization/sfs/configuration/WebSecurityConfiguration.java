package com.codares.logistics.iam.infrastructure.authorization.sfs.configuration;

import com.codares.logistics.iam.infrastructure.authorization.sfs.services.JwtAuthenticationConverter;
import com.codares.logistics.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * Configuración de Seguridad Web de Spring Security.
 * <p>
 * Esta clase centraliza la configuración completa de seguridad del marco Spring Security
 * para la aplicación. Implementa un modelo de autenticación stateless basado en OAuth2
 * Resource Server con JWT (JSON Web Tokens).
 * </p>
 * <p>
 * <strong>Configuraciones Principales:</strong>
 * </p>
 * <ul>
 *   <li><strong>CORS (Cross-Origin Resource Sharing):</strong> Permite solicitudes desde cualquier origen con métodos GET, POST, PUT, DELETE</li>
 *   <li><strong>CSRF:</strong> Deshabilitado porque se usa autenticación stateless con JWT (no vulnerable a CSRF)</li>
 *   <li><strong>Sesión:</strong> Configurada como STATELESS (cada solicitud debe incluir JWT en Authorization header)</li>
 *   <li><strong>OAuth2 Resource Server:</strong> Usa JWT con conversión automática de roles desde claims</li>
 *   <li><strong>Rutas Públicas:</strong> Solo autenticación y documentación Swagger permitidas sin token</li>
 *   <li><strong>Método-Seguridad:</strong> Habilitada para usar @PreAuthorize, @Secured a nivel de método</li>
 * </ul>
 * <p>
 * <strong>Flujo de Autenticación:</strong>
 * </p>
 * <ol>
 *   <li>Cliente envía credenciales al endpoint POST /api/v1/authentication/signin</li>
 *   <li>AuthenticationManager valida usuario/contraseña con DaoAuthenticationProvider</li>
 *   <li>Si es válido, se genera token JWT con roles del usuario</li>
 *   <li>Cliente incluye token en header "Authorization: Bearer &lt;token&gt;"</li>
 *   <li>BearerAuthorizationRequestFilter extrae y valida el token en cada solicitud</li>
 *   <li>JwtAuthenticationConverter convierte JWT claims a Spring Security GrantedAuthority</li>
 *   <li>SecurityContextHolder almacena la autenticación para la solicitud</li>
 * </ol>
 * <p>
 * <strong>Componentes Inyectados:</strong>
 * </p>
 * <ul>
 *   <li>{@link UserDetailsService} - Carga usuario y roles desde base de datos</li>
 *   <li>{@link BCryptHashingService} - Validación de contraseñas con BCrypt</li>
 *   <li>{@link AuthenticationEntryPoint} - Maneja solicitudes no autorizadas (HTTP 401)</li>
 *   <li>{@link JwtAuthenticationConverter} - Convierte JWT a Authentication de Spring</li>
 * </ul>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see org.springframework.security.config.annotation.web.builders.HttpSecurity
 * @see org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
 * @see org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

    private final UserDetailsService userDetailsService;

    private final BCryptHashingService hashingService;

    private final AuthenticationEntryPoint unauthorizedRequestHandler;

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    /**
     * Crea el AuthenticationManager de Spring Security.
     * <p>
     * El AuthenticationManager es el componente central que orquesta el proceso de autenticación,
     * delegando a los AuthenticationProvider registrados (como DaoAuthenticationProvider)
     * para validar las credenciales del usuario.
     * </p>
     *
     * @param authenticationConfiguration configuración que contiene el AuthenticationManager
     * @return AuthenticationManager instancia configurada y lista para usar
     * @throws Exception si hay error en la construcción del manager
     * @see org.springframework.security.authentication.AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Crea el DaoAuthenticationProvider para autenticación usuario/contraseña.
     * <p>
     * Este provider carga los detalles del usuario desde BD (UserDetailsService) y
     * valida que la contraseña proporcionada coincida con la almacenada usando BCrypt.
     * </p>
     * <p>
     * <strong>Flujo de Validación:</strong>
     * </p>
     * <ol>
     *   <li>AuthenticationManager recibe UsernamePasswordAuthenticationToken</li>
     *   <li>DaoAuthenticationProvider carga usuario con UserDetailsService</li>
     *   <li>Compara contraseña proporcionada vs. almacenada usando PasswordEncoder</li>
     *   <li>Si coinciden, retorna Authentication con usuario y roles</li>
     * </ol>
     *
     * @return DaoAuthenticationProvider configurado con UserDetailsService y PasswordEncoder
     * @see org.springframework.security.authentication.dao.DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(hashingService);
        return authenticationProvider;
    }

    /**
     * Crea el PasswordEncoder de Spring Security (BCrypt adapter).
     * <p>
     * El PasswordEncoder es responsable de codificar nuevas contraseñas y validar
     * contraseñas proporcionadas durante autenticación. Utiliza el servicio de hashing
     * BCrypt que implementa la interfaz PasswordEncoder.
     * </p>
     *
     * @return PasswordEncoder que encapsula la lógica de BCrypt
     * @see org.springframework.security.crypto.password.PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return hashingService;
    }

    /**
     * Crea la cadena de filtros de seguridad de la aplicación.
     * <p>
     * Configura todas las políticas de seguridad HTTP incluyendo:
     * </p>
     * <ul>
     *   <li>CORS: Permite solicitudes desde cualquier origen</li>
     *   <li>CSRF: Deshabilitado (arquitectura stateless no vulnerable)</li>
     *   <li>Sesión: STATELESS (cada solicitud es independiente)</li>
     *   <li>Autorización: Rutas públicas vs. protegidas</li>
     *   <li>OAuth2 Resource Server: JWT como mecanismo de autenticación</li>
     *   <li>Manejo de excepciones: Punto de entrada para no autorizados</li>
     * </ul>
     * <p>
     * <strong>Rutas Públicas (sin autenticación):</strong>
     * </p>
     * <ul>
     *   <li>/api/v1/authentication/** - Endpoints de login/signup</li>
     *   <li>/v3/api-docs/** - Documentación OpenAPI</li>
     *   <li>/swagger-ui.html, /swagger-ui/** - UI de Swagger</li>
     *   <li>/swagger-resources/**, /webjars/** - Recursos de Swagger</li>
     * </ul>
     * <p>
     * <strong>Rutas Protegidas:</strong> Cualquier otra solicitud requiere JWT válido
     * en header "Authorization: Bearer &lt;token&gt;"
     * </p>
     *
     * @param http configurador de seguridad HTTP
     * @return SecurityFilterChain cadena de filtros de seguridad configurada
     * @throws Exception si hay error en la configuración
     * @see org.springframework.security.config.annotation.web.builders.HttpSecurity
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(configurer -> configurer.configurationSource(request -> {
            var cors = new CorsConfiguration();
            cors.setAllowedOrigins(List.of("*"));
            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
            cors.setAllowedHeaders(List.of("*"));
            return cors;
        }));
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedRequestHandler))
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(
                                "/api/v1/authentication/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        http.authenticationProvider(authenticationProvider());
        return http.build();

    }

    /**
     * Constructor que inyecta todas las dependencias necesarias para la configuración.
     *
     * @param userDetailsService servicio para cargar usuario y roles desde BD
     * @param hashingService servicio de hashing BCrypt para validación de contraseñas
     * @param authenticationEntryPoint punto de entrada para solicitudes no autorizadas (HTTP 401)
     * @param jwtAuthenticationConverter convertidor de JWT a Spring Security Authentication
     */
    public WebSecurityConfiguration(
            @Qualifier("defaultUserDetailsService") UserDetailsService userDetailsService,
            BCryptHashingService hashingService,
            AuthenticationEntryPoint authenticationEntryPoint,
            JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.userDetailsService = userDetailsService;
        this.hashingService = hashingService;
        this.unauthorizedRequestHandler = authenticationEntryPoint;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }
}
