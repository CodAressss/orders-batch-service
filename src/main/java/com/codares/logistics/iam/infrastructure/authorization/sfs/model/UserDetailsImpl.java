package com.codares.logistics.iam.infrastructure.authorization.sfs.model;

import com.codares.logistics.iam.domain.model.aggregates.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementación de UserDetails para Spring Security.
 * <p>
 * Esta clase es responsable de proporcionar los detalles del usuario al framework de Spring Security.
 * Implementa la interfaz {@link UserDetails} y actúa como adaptador entre el modelo de dominio {@link User}
 * y la abstracción de Spring Security.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Transportar información del usuario a Spring Security</li>
 *   <li>Mantener estado de la cuenta (expiración, bloqueo, credenciales vigentes)</li>
 *   <li>Proporcionar autoridades (roles y permisos) del usuario</li>
 *   <li>Ocultar la contraseña mediante {@link JsonIgnore}</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see org.springframework.security.core.userdetails.UserDetails\n */
@Getter
@EqualsAndHashCode
public class UserDetailsImpl implements UserDetails {

    private final String username;
    @JsonIgnore
    private final String password;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructor que inicializa la instancia de UserDetailsImpl.
     * <p>
     * Establece todos los detalles del usuario necesarios para autenticación y autorización.
     * Por defecto, todos los estados de la cuenta se establecen como vigentes y habilitados.
     * </p>
     *
     * @param username el nombre de usuario. No puede ser null
     * @param password la contraseña hasheada (será ignorada en serialización JSON)
     * @param authorities la colección de autoridades (roles) del usuario
     */
    public UserDetailsImpl(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
    }

    /**
     * This method is responsible for building the UserDetailsImpl object from the User object.
     * @param user The user object.
     * @return The UserDetailsImpl object.
     */
    public static UserDetailsImpl build(User user) {
        var authorities = user.getRoles().stream()
                .map(role -> role.getName().name())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new UserDetailsImpl(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }

}
