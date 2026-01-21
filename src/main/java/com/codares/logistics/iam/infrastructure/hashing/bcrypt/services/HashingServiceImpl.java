package com.codares.logistics.iam.infrastructure.hashing.bcrypt.services;

import com.codares.logistics.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de hashing de contraseñas usando BCrypt.
 * <p>
 * Esta clase implementa la interfaz {@link BCryptHashingService} y proporciona
 * funcionalidad para codificar y validar contraseñas de usuarios utilizando el
 * algoritmo de hashing BCrypt, que es adaptativo y resistente a ataques de fuerza bruta.
 * </p>
 * <p>
 * BCrypt (Blowfish crypt) es un algoritmo de hashing de contraseñas construido sobre
 * el cifrado Blowfish. Características:
 * <ul>
 *   <li>Adaptativo: El costo computacional aumenta con el tiempo</li>
 *   <li>Salted: Incluye salt automáticamente para cada hash</li>
 *   <li>Resistente: Protege contra ataques de diccionario y fuerza bruta</li>
 *   <li>Estándar: Soportado nativamente por Spring Security</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see BCryptHashingService
 * @see BCryptPasswordEncoder
 */
@Service
public class HashingServiceImpl implements BCryptHashingService {
    /**
     * Codificador de contraseñas BCrypt de Spring Security.
     * <p>
     * Instancia centralizada para todas las operaciones de encoding y matching de contraseñas.
     * </p>
     */
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor que inicializa el codificador BCrypt.
     * <p>
     * Crea una instancia de {@link BCryptPasswordEncoder} con configuración por defecto
     * (strength = 10, lo que significa 2^10 = 1024 iteraciones).
     * </p>
     */
    HashingServiceImpl() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Codifica una contraseña en texto plano utilizando BCrypt.
     * <p>
     * Aplica el algoritmo BCrypt a la contraseña proporcionada, generando automáticamente
     * un salt y un hash único. El resultado puede almacenarse seguramente en la base de datos.
     * Cada invocación genera un hash diferente incluso para la misma contraseña, gracias al salt aleatorio.
     * </p>
     * <p>
     * Comportamiento:
     * <ul>
     *   <li>Entrada: Contraseña en texto plano (CharSequence)</li>
     *   <li>Salida: Hash BCrypt con salt incluido (60 caracteres)</li>
     *   <li>Formato de salida: $2a$10$[salt (22 chars)][hash (31 chars)]</li>
     *   <li>Irreversible: No es posible obtener la contraseña original del hash</li>
     * </ul>
     * </p>
     *
     * @param rawPassword la contraseña en texto plano a codificar
     * @return String el hash BCrypt de la contraseña (60 caracteres)
     * @throws IllegalArgumentException si la contraseña es demasiado larga (>72 bytes)
     *
     * @see #matches(CharSequence, String)
     */
    @Override
    public String encode(CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Valida si una contraseña en texto plano coincide con su hash BCrypt.
     * <p>
     * Compara la contraseña proporcionada con el hash almacenado, ejecutando el hash nuevamente
     * con el salt extraído del hash original y comparando los resultados.
     * Esta operación es resistente a ataques de timing gracias a la implementación de Spring.
     * </p>
     * <p>
     * Comportamiento:
     * <ul>
     *   <li>Extrae el salt del hash almacenado</li>
     *   <li>Aplica BCrypt a la contraseña proporcionada con ese salt</li>
     *   <li>Compara los hashes de forma segura (resistente a timing attacks)</li>
     *   <li>Retorna true solo si coinciden exactamente</li>
     * </ul>
     * </p>
     *
     * @param rawPassword la contraseña en texto plano a validar
     * @param encodedPassword el hash BCrypt almacenado (obtenido de {@link #encode(CharSequence)})
     * @return boolean true si la contraseña coincide con el hash, false en caso contrario
     *
     * @see #encode(CharSequence)
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}
