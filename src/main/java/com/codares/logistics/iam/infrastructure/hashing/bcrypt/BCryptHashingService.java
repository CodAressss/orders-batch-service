package com.codares.logistics.iam.infrastructure.hashing.bcrypt;

import com.codares.logistics.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Interfaz Marcadora para el Servicio de Hashing BCrypt.
 * <p>
 * Esta interfaz marca la combinación de dos roles:
 * <ul>
 *   <li>{@link HashingService}: Interfaz de dominio para codificación de contraseñas</li>
 *   <li>{@link PasswordEncoder}: Interfaz de Spring Security para encoding de contraseñas</li>
 * </ul>
 * </p>
 * <p>
 * Propósito: Actuar como adaptador (ACL - Anti-Corruption Layer) entre la interfaz de dominio
 * y la implementación específica de Spring Security, permitiendo inyección transparente
 * en {@link com.codares.logistics.iam.infrastructure.hashing.bcrypt.services.HashingServiceImpl}.
 * </p>
 * <p>
 * Patrón: Adapter Pattern para mantener independencia entre capas de dominio e infraestructura.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.iam.application.internal.outboundservices.hashing.HashingService
 * @see org.springframework.security.crypto.password.PasswordEncoder
 */
public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
