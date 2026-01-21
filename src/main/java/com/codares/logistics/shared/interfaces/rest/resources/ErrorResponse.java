package com.codares.logistics.shared.interfaces.rest.resources;

import java.time.LocalDateTime;

/**
 * DTO inmutable que representa la estructura estándar de respuesta para errores en la API.
 * <p>
 * Todas las excepciones capturadas por el {@code GlobalExceptionHandler} se transforman
 * en esta estructura antes de ser devueltas al cliente, proporcionando una respuesta
 * consistente y amigable que incluye contexto útil para la depuración y el manejo
 * programático de errores por parte de clientes frontend.
 * </p>
 * <p>
 * Incluye tanto el código HTTP estándar ({@code error}) como el código de negocio
 * tipificado ({@code code}) para permitir que los clientes manejen errores de forma
 * granular sin parsear mensajes de texto.
 * </p>
 *
 * @param timestamp Fecha y hora en que ocurrió el error (ISO 8601).
 * @param status    Código de estado HTTP numérico (ej: 404, 409, 400).
 * @param error     Descripción HTTP estándar del error (ej: "Not Found", "Conflict").
 * @param code      Código de negocio tipificado (ej: "CLIENTE_NO_ENCONTRADO", "DUPLICADO").
 * @param message   Mensaje descriptivo del error dirigido al cliente.
 * @param path      Ruta (endpoint) que generó el error.
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {}
