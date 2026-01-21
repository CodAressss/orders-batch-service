package com.codares.logistics.iam.application.internal.outboundservices.hashing;

/**
 * Servicio de Hashing para Codificación de Contraseñas.
 * <p>
 * Esta interfaz define el contrato para codificar y validar contraseñas de forma segura
 * utilizando algoritmos criptográficos irreversibles (típicamente BCrypt). Es parte de la
 * Anti-Corruption Layer que abstrae la implementación específica de hashing del dominio.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Codificar contraseñas en texto plano a hashes seguros</li>
 *   <li>Verificar que una contraseña en texto plano coincida con un hash almacenado</li>
 *   <li>Garantizar seguridad mediante sal aleatoria y múltiples iteraciones</li>
 * </ul>
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see org.springframework.security.crypto.password.PasswordEncoder
 */
public interface HashingService {
    /**
     * Codifica una contraseña en texto plano a un hash seguro.
     * <p>
     * Utiliza un algoritmo de hashing criptográfico irreversible con sal aleatoria.
     * La misma contraseña produce hashes diferentes en cada invocación debido a la sal.
     * </p>
     *
     * @param rawPassword la contraseña en texto plano a codificar
     * @return String el hash seguro de la contraseña
     * @throws IllegalArgumentException si la contraseña es null
     */
    String encode(CharSequence rawPassword);

    /**
     * Valida que una contraseña en texto plano coincida con un hash almacenado.
     * <p>
     * Compara de forma segura una contraseña en texto plano contra un hash previamente
     * codificado, evitando ataques de timing mediante comparación constante.
     * </p>
     *
     * @param rawPassword la contraseña en texto plano a validar
     * @param encodedPassword el hash almacenado en base de datos
     * @return boolean {@code true} si la contraseña coincide con el hash, {@code false} en caso contrario
     * @throws IllegalArgumentException si algún parámetro es null
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);

}
