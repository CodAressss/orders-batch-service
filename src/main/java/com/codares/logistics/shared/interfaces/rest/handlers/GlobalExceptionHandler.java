package com.codares.logistics.shared.interfaces.rest.handlers;

import com.codares.logistics.shared.domain.exceptions.DomainException;
import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;
import com.codares.logistics.shared.domain.exceptions.ResourceAlreadyExistsException;
import com.codares.logistics.shared.domain.exceptions.ResourceNotFoundException;
import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;
import com.codares.logistics.shared.interfaces.rest.resources.ErrorResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador Secundario (Controlador de Excepciones) que proporciona el manejo centralizado
 * y estandarizado de todas las excepciones en la aplicación.
 * <p>
 * Este componente implementa el patrón de aspecto cruzado (cross-cutting concern) utilizando
 * {@code @RestControllerAdvice} para interceptar excepciones lanzadas desde cualquier
 * controlador REST. Transforma las excepciones de dominio en respuestas HTTP con estructura
 * consistente mediante {@code ErrorResponse}, garantizando que el cliente reciba información
 * clara y utilizable independientemente del tipo de error ocurrido.
 * </p>
 * <p>
 * Extiende {@link ResponseEntityExceptionHandler} para aprovechar el manejo avanzado de
 * excepciones del framework de Spring, permitiendo la captura de errores estándar de
 * validación y serialización (400, 405, 415, etc.) con el mismo formato JSON que las
 * excepciones de dominio.
 * </p>
 * <p>
 * <strong>Mapeo de excepciones a códigos HTTP:</strong>
 * <ul>
 *   <li>{@code ResourceNotFoundException} → 404 (Not Found)</li>
 *   <li>{@code ResourceAlreadyExistsException} → 409 (Conflict)</li>
 *   <li>{@code InvalidArgumentException} → 400 (Bad Request)</li>
 *   <li>{@code DomainException} → 422 (Unprocessable Entity)</li>
 *   <li>Excepciones genéricas no controladas → 500 (Internal Server Error)</li>
 *   <li>Excepciones del framework Spring → Mapeadas a su código HTTP correspondiente</li>
 * </ul>
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    /**
     * Sobrescribe el manejador de excepciones internas del framework para aplicar
     * formato consistente a los errores estándar de Spring Boot.
     * <p>
     * Sin esta sobrescritura, errores como "JSON mal formado", "método no permitido"
     * o "tipo de contenido no soportado" devolverían un cuerpo diferente al que espera
     * el cliente, rompiendo la consistencia de respuestas. Este método asegura que
     * excepciones del framework (validación, serialización, etc.) sean transformadas
     * en {@code ErrorResponse} con la misma estructura que las excepciones de dominio.
     * </p>
     *
     * @param ex El mensaje de la excepción del framework.
     * @param body Cuerpo de la respuesta generado automáticamente (puede ser nulo).
     * @param headers Encabezados HTTP a incluir en la respuesta.
     * @param status Código de estado HTTP resuelto por Spring.
     * @param request La solicitud web original.
     * @return Un {@code ResponseEntity} con {@code ErrorResponse} formateado o el cuerpo original si existe.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode status, // Nota: Spring Boot 3 usa HttpStatusCode
            WebRequest request) {

        // Si el cuerpo ya viene nulo (Spring no generó uno propio), ponemos el nuestro.
        if (body == null) {
            String reason = HttpStatus.resolve(status.value()) != null 
                    ? HttpStatus.resolve(status.value()).getReasonPhrase() 
                    : "Internal Error";
            
            String businessCode = GlobalError.FORMATO_INVALIDO.name();
            
            ErrorResponse errorResponse = new ErrorResponse(
                    LocalDateTime.now(),
                    status.value(),
                    reason,
                    businessCode,
                    ex.getMessage(),
                    request.getDescription(false).replace("uri=", "")
            );
            return new ResponseEntity<>(errorResponse, headers, status);
        }

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    /**
     * Maneja las excepciones de tipo {@code ResourceNotFoundException}.
     * <p>
     * Captura cuando un servicio de aplicación intenta acceder a un recurso (entidad de
     * dominio) que no existe en el sistema, transformando el error en una respuesta
     * HTTP 404 (Not Found) con descripción clara del recurso no localizado.
     * </p>
     *
     * @param exception La excepción lanzada indicando qué recurso no fue encontrado.
     * @param request La solicitud web que generó la excepción.
     * @return {@code ResponseEntity} con HTTP 404 y {@code ErrorResponse}.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Maneja las excepciones de tipo {@code ResourceAlreadyExistsException}.
     * <p>
     * Captura cuando un comando de creación intenta crear un recurso con una identidad
     * única (ej: ID, número de referencia) que ya existe en el sistema, indicando un
     * conflicto. Transforma el error en una respuesta HTTP 409 (Conflict).
     * </p>
     *
     * @param exception La excepción lanzada indicando qué recurso ya existe.
     * @param request La solicitud web que generó la excepción.
     * @return {@code ResponseEntity} con HTTP 409 y {@code ErrorResponse}.
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException exception,
            WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT, request);
    }

    /**
     * Maneja las excepciones de tipo {@code InvalidArgumentException}.
     * <p>
     * Captura cuando los parámetros de entrada de un comando no cumplen con las
     * restricciones de validación del dominio (ej: valores nulos, fuera de rango,
     * formato inválido, restricciones de negocio). Transforma el error en una
     * respuesta HTTP 400 (Bad Request).
     * </p>
     *
     * @param exception La excepción lanzada indicando cuál argumento es inválido y por qué.
     * @param request La solicitud web que generó la excepción.
     * @return {@code ResponseEntity} con HTTP 400 y {@code ErrorResponse}.
     */
    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgumentException(
            InvalidArgumentException exception,
            WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Maneja las excepciones de tipo {@code DomainException}.
     * <p>
     * Captura cualquier excepción de negocio que no sea manejada por los métodos
     * específicos anteriores. Estas excepciones representan violaciones de reglas de
     * negocio complejas que no encajan en categorías estándar (404, 409, 400).
     * Transforma el error en una respuesta HTTP 422 (Unprocessable Entity).
     * </p>
     *
     * @param exception La excepción lanzada de tipo {@code DomainException}.
     * @param request La solicitud web que generó la excepción.
     * @return {@code ResponseEntity} con HTTP 422 y {@code ErrorResponse}.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException exception,
            WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    /**
     * Maneja las excepciones genéricas no controladas.
     * <p>
     * Actúa como mecanismo de defensa final para cualquier excepción inesperada que
     * no sea de dominio. Devuelve una respuesta HTTP 500 (Internal Server Error) con
     * un mensaje genérico para el cliente (sin exponer detalles técnicos por razones
     * de seguridad). Se recomienda agregar logging aquí para rastrear errores inesperados.
     * </p>
     *
     * @param exception La excepción no capturada por handlers específicos.
     * @param request La solicitud web que generó la excepción.
     * @return {@code ResponseEntity} con HTTP 500 y {@code ErrorResponse} genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception,
            WebRequest request) {
        
        // LOG del error real para debugging
        log.error("Error no controlado en endpoint {}: {}", 
            request.getDescription(false), exception.getMessage(), exception);

        // Mensaje genérico para el cliente (Seguridad)
        String genericMessage = "Ocurrió un error interno inesperado. Por favor contacte al administrador.";
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                GlobalError.ERROR_INTERNO.name(),
                genericMessage,
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Construye una respuesta de error estandarizada.
     * <p>
     * Método privado que encapsula la lógica común para transformar una excepción
     * en un DTO {@code ErrorResponse}. Extrae automáticamente la ruta de la solicitud,
     * captura el timestamp actual, y estructura todos los datos necesarios en un
     * formato consistente que el cliente puede procesar fácilmente.
     * </p>
     *
     * @param exception La excepción capturada a transformar.
     * @param status El código de estado HTTP a asignar.
     * @param request La solicitud web para extraer contexto (path, parámetros, etc.).
     * @return {@code ResponseEntity} contiendo el {@code ErrorResponse} y el estado HTTP indicado.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception exception,
            HttpStatus status,
            WebRequest request) {
        
        String businessCode = extractBusinessCode(exception);
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                businessCode,
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Extrae el código de negocio tipificado de una excepción.
     * <p>
     * Si la excepción es de tipo {@code DomainException}, extrae el código del
     * {@code BusinessError} asociado. Para otras excepciones, retorna un código
     * genérico de error interno.
     * </p>
     *
     * @param exception La excepción de la cual extraer el código.
     * @return El código de negocio como String (ej: "CLIENTE_NO_ENCONTRADO").
     */
    private String extractBusinessCode(Exception exception) {
        if (exception instanceof DomainException domainException) {
            return domainException.getErrorType().name();
        }
        return GlobalError.ERROR_INTERNO.name();
    }
}
