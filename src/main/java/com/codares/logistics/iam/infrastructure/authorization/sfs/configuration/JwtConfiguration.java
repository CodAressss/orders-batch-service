package com.codares.logistics.iam.infrastructure.authorization.sfs.configuration;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Configuración de JWT para OAuth2 Resource Server.
 * <p>
 * Esta clase configura el decodificador de JWT para validar tokens JWT entrantes utilizando la clave secreta.
 * Es parte de la implementación del Servidor de Recursos OAuth2 de Spring Security.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Crear el bean JwtDecoder para validación de tokens</li>
 *   <li>Configurar el algoritmo HMAC-SHA512 (HS512)</li>
 *   <li>Integrar la clave secreta desde las propiedades de configuración</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see org.springframework.security.oauth2.jwt.JwtDecoder
 * @see org.springframework.security.oauth2.jose.jws.MacAlgorithm
 */
@Configuration
public class JwtConfiguration {

    @Value("${authorization.jwt.secret}")
    private String secret;

    /**
     * Crea un bean JwtDecoder para validación de tokens JWT.
     * <p>
     * Utiliza el algoritmo HMAC-SHA512 (HS512) con la clave secreta configurada.
     * Este decodificador es utilizado automáticamente por Spring Security OAuth2 Resource Server
     * para validar todos los tokens JWT entrantes en cada solicitud.
     * </p>
     * <p>
     * La clave secreta se convierte a formato SecretKey compatible con el algoritmo.
     * El decodificador NimbusJwtDecoder maneja automáticamente:
     * <ul>
     *   <li>Verificación de firma del token</li>
     *   <li>Validación de expiración</li>
     *   <li>Validación de estructura del token</li>
     * </ul>
     * </p>
     *
     * @return JwtDecoder configurado con la clave secreta para validación de tokens
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return NimbusJwtDecoder
            .withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS512)
            .build();
    }
}

