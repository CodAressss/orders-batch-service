package com.codares.logistics.iam.infrastructure.authorization.sfs.services;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Conversor de Tokens JWT a Autenticación de Spring Security.
 * <p>
 * Convierte un token JWT en un objeto de Autenticación ({@link AbstractAuthenticationToken}) de Spring Security.
 * Extrae los roles desde los claims del JWT y los convierte en {@link GrantedAuthority} para autorización.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Parsear y convertir tokens JWT</li>
 *   <li>Extraer roles desde el claim personalizado "roles" del JWT</li>
 *   <li>Crear autoridades de Spring Security a partir de los roles</li>
 *   <li>Mantener integración con OAuth2 Resource Server</li>
 * </ul>
 * </p>
 * <p>
 * Este componente es parte de la implementación del Servidor de Recursos OAuth2 y sigue
 * el patrón Adaptador para integrar JWT con Spring Security de forma elegante.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see org.springframework.core.convert.converter.Converter
 * @see org.springframework.security.oauth2.jwt.Jwt
 * @see org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * Converts a JWT into an Authentication token.
     * <p>
     * Extracts the username from the 'sub' claim and roles from the 'roles' claim.
     * If no roles are present, an empty collection is used.
     * </p>
     *
     * @param jwt the JWT token to convert
     * @return AbstractAuthenticationToken containing the authentication information
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extracts authorities from JWT claims.
     * <p>
     * Looks for a 'roles' claim in the JWT and converts each role to a GrantedAuthority.
     * Ensures that roles have the 'ROLE_' prefix as expected by Spring Security.
     * </p>
     *
     * @param jwt the JWT token
     * @return Collection of GrantedAuthority objects
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extract roles from JWT claims
        List<String> roles = jwt.getClaimAsStringList("roles");
        
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(role -> {
                    // Ensure role has ROLE_ prefix
                    if (!role.startsWith("ROLE_")) {
                        return "ROLE_" + role;
                    }
                    return role;
                })
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
