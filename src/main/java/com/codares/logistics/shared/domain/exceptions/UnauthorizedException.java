package com.codares.logistics.shared.domain.exceptions;

import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;

/**
 * Excepción lanzada cuando una solicitud no tiene credenciales válidas de autenticación.
 * <p>
 * Esta excepción se lanza en el filtro de autenticación JWT cuando:
 * </p>
 * <ul>
 *   <li>No se proporciona token Bearer en el header Authorization</li>
 *   <li>El token Bearer es inválido o expirado</li>
 *   <li>La firma del token ha sido alterada</li>
 * </ul>
 * <p>
 * Se transforma a HTTP 401 (Unauthorized) por el {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Sistema de Gestión de Identidades
 * @version 1.0
 * @since 1.0
 * @see com.codares.logistics.shared.interfaces.rest.handlers.GlobalExceptionHandler
 */
public class UnauthorizedException extends DomainException {

    /**
     * Construye una excepción de autenticación inválida.
     *
     * @param message mensaje descriptivo del error
     */
    public UnauthorizedException(String message) {
        super(message, GlobalError.NO_AUTORIZADO);
    }

    /**
     * Construye una excepción de autenticación inválida con causa.
     *
     * @param message mensaje descriptivo del error
     * @param cause excepción que originó este error
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, GlobalError.NO_AUTORIZADO, cause);
    }
}
