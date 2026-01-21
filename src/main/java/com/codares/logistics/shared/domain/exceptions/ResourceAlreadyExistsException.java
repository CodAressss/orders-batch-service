package com.codares.logistics.shared.domain.exceptions;

import com.codares.logistics.shared.domain.model.valueobjects.BusinessError;
import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe en el sistema.
 * <p>
 * Esta excepción es capturada por el {@code GlobalExceptionHandler} para devolver una respuesta
 * HTTP 409 (Conflict) al cliente. Se utiliza en los servicios de aplicación cuando un comando
 * de creación intenta crear un recurso con una identidad única que ya está registrada en el
 * sistema.
 * </p>
 * <p>
 * Soporta tanto uso simple (con código genérico {@link GlobalError#RECURSO_DUPLICADO})
 * como uso tipificado (con código específico del Bounded Context).
 * </p>
 * <p>
 * Ejemplo de uso simple:
 * <pre>{@code
 * throw new ResourceAlreadyExistsException("Pedido con número 'PED-001' ya existe");
 * }</pre>
 * </p>
 * <p>
 * Ejemplo de uso tipificado (Batch):
 * <pre>{@code
 * throw new ResourceAlreadyExistsException("Número de pedido duplicado: P001", OrderError.DUPLICADO);
 * }</pre>
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public class ResourceAlreadyExistsException extends DomainException {

    /**
     * Construye una nueva {@code ResourceAlreadyExistsException} con el mensaje de error especificado.
     * <p>
     * Utiliza {@link GlobalError#RECURSO_DUPLICADO} como código de error por defecto.
     * </p>
     *
     * @param message El mensaje descriptivo indicando qué recurso ya existe.
     */
    public ResourceAlreadyExistsException(String message) {
        super(message, GlobalError.RECURSO_DUPLICADO);
    }

    /**
     * Construye una nueva {@code ResourceAlreadyExistsException} con mensaje y código de error tipificado.
     * <p>
     * Permite especificar un código de error específico del Bounded Context para casos
     * donde se requiere mayor granularidad semántica (ej: procesamiento batch).
     * </p>
     *
     * @param message   El mensaje descriptivo indicando qué recurso ya existe.
     * @param errorType Código de error específico del contexto (ej: {@code OrderError.DUPLICADO}).
     */
    public ResourceAlreadyExistsException(String message, BusinessError errorType) {
        super(message, errorType);
    }

    /**
     * Construye una nueva {@code ResourceAlreadyExistsException} con mensaje, código de error y causa raíz.
     *
     * @param message   El mensaje descriptivo indicando qué recurso ya existe.
     * @param errorType Código de error específico del contexto.
     * @param cause     La excepción que causó el conflicto (ej: {@code DataIntegrityViolationException}).
     */
    public ResourceAlreadyExistsException(String message, BusinessError errorType, Throwable cause) {
        super(message, errorType, cause);
    }
}
