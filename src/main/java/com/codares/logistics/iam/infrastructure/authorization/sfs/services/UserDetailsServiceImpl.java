package com.codares.logistics.iam.infrastructure.authorization.sfs.services;

import com.codares.logistics.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.codares.logistics.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación del Servicio de Detalles de Usuario para Spring Security.
 * <p>
 * Esta clase es responsable de proporcionar los detalles del usuario al framework de Spring Security.
 * Implementa la interfaz {@link UserDetailsService} y actúa como puente entre el repositorio de BD
 * y las abstracciones de autenticación de Spring Security.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Cargar usuarios desde la base de datos por nombre de usuario</li>
 *   <li>Convertir usuarios de dominio a UserDetails de Spring Security</li>
 *   <li>Lanzar excepciones si el usuario no es encontrado</li>
 *   <li>Facilitar autenticación y carga de roles</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see UserDetailsImpl
 * @see com.codares.logistics.iam.infrastructure.persistence.jpa.repositories.UserRepository
 */
@Service(value = "defaultUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Carga los detalles del usuario desde la base de datos por nombre de usuario.
     * <p>
     * Busca el usuario en la BD y lo convierte a un objeto {@link UserDetailsImpl} que
     * puede ser utilizado por Spring Security para autenticación y autorización.
     * Si el usuario no existe, lanza {@link UsernameNotFoundException}.
     * </p>
     *
     * @param username el nombre de usuario a buscar
     * @return {@link UserDetails} con los detalles del usuario encontrado
     * @throws UsernameNotFoundException si el usuario no existe en la base de datos
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return UserDetailsImpl.build(user);
    }
}
