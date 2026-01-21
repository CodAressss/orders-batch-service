package com.codares.logistics.shared.domain.exceptions;

import com.codares.logistics.shared.domain.model.valueobjects.BusinessError;
import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;

/**
 * Excepción lanzada cuando se proporcionan argumentos inválidos o que violan reglas de negocio.
 * <p>
 * Esta excepción es capturada por el {@code GlobalExceptionHandler} para devolver una respuesta
 * HTTP 400 (Bad Request) al cliente. Se utiliza en los servicios de aplicación cuando los
 * parámetros de entrada no cumplen con las reglas de validación del dominio, como valores
 * nulos, fuera de rango, o que incumplen restricciones de negocio.
 * </p>
 * <p>
 * Soporta tanto uso simple (con código genérico {@link GlobalError#ARGUMENTO_INVALIDO})
 * como uso tipificado (con código específico del Bounded Context).
 * </p>
 * <p>
 * Ejemplo de uso simple:
 * <pre>{@code
 * throw new InvalidArgumentException("La cantidad debe ser mayor a cero");
 * }</pre>
 * </p>
 * <p>
 * Ejemplo de uso tipificado (Batch):
 * <pre>{@code
 * throw new InvalidArgumentException("Estado 'ANULADO' no es válido", OrderError.ESTADO_INVALIDO);
 * }</pre>
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public class InvalidArgumentException extends DomainException {

    /**
     * Construye una nueva {@code InvalidArgumentException} con el mensaje de error especificado.
     * <p>
     * Utiliza {@link GlobalError#ARGUMENTO_INVALIDO} como código de error por defecto.
     * </p>
     *
     * @param message El mensaje descriptivo indicando cuál argumento es inválido y por qué.
     */
    public InvalidArgumentException(String message) {
        super(message, GlobalError.ARGUMENTO_INVALIDO);
    }

    /**
     * Construye una nueva {@code InvalidArgumentException} con mensaje y código de error tipificado.
     * <p>
     * Permite especificar un código de error específico del Bounded Context para casos
     * donde se requiere mayor granularidad semántica (ej: procesamiento batch).
     * </p>
     *
     * @param message   El mensaje descriptivo indicando cuál argumento es inválido.
     * @param errorType Código de error específico del contexto (ej: {@code OrderError.ESTADO_INVALIDO}).
     */
    public InvalidArgumentException(String message, BusinessError errorType) {
        super(message, errorType);
    }

    /**
     * Construye una nueva {@code InvalidArgumentException} con mensaje, código de error y causa raíz.
     *
     * @param message   El mensaje descriptivo indicando cuál argumento es inválido.
     * @param errorType Código de error específico del contexto.
     * @param cause     La excepción que causó la validación fallida.
     */
    public InvalidArgumentException(String message, BusinessError errorType, Throwable cause) {
        super(message, errorType, cause);
    }
}
