package com.codares.logistics.shared.domain.exceptions;

import com.codares.logistics.shared.domain.model.valueobjects.BusinessError;
import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;

/**
 * Excepción lanzada cuando se intenta acceder a un recurso que no existe en el sistema.
 * <p>
 * Esta excepción es capturada por el {@code GlobalExceptionHandler} para devolver una respuesta
 * HTTP 404 (Not Found) al cliente. Se utiliza en los servicios de aplicación cuando una
 * consulta o comando requiere un recurso existente que no puede ser encontrado.
 * </p>
 * <p>
 * Soporta tanto uso simple (con código genérico {@link GlobalError#RECURSO_NO_ENCONTRADO})
 * como uso tipificado (con código específico del Bounded Context).
 * </p>
 * <p>
 * Ejemplo de uso simple (API REST unitaria):
 * <pre>{@code
 * throw new ResourceNotFoundException("Pedido con ID 123 no encontrado");
 * }</pre>
 * </p>
 * <p>
 * Ejemplo de uso tipificado (Batch o lógica específica):
 * <pre>{@code
 * throw new ResourceNotFoundException("Cliente CLI-999 no existe", OrderError.CLIENTE_NO_ENCONTRADO);
 * }</pre>
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public class ResourceNotFoundException extends DomainException {

    /**
     * Construye una nueva {@code ResourceNotFoundException} con el mensaje de error especificado.
     * <p>
     * Utiliza {@link GlobalError#RECURSO_NO_ENCONTRADO} como código de error por defecto.
     * </p>
     *
     * @param message El mensaje descriptivo indicando qué recurso no fue encontrado.
     */
    public ResourceNotFoundException(String message) {
        super(message, GlobalError.RECURSO_NO_ENCONTRADO);
    }

    /**
     * Construye una nueva {@code ResourceNotFoundException} con mensaje y código de error tipificado.
     * <p>
     * Permite especificar un código de error específico del Bounded Context para casos
     * donde se requiere mayor granularidad semántica (ej: procesamiento batch).
     * </p>
     *
     * @param message   El mensaje descriptivo indicando qué recurso no fue encontrado.
     * @param errorType Código de error específico del contexto (ej: {@code OrderError.CLIENTE_NO_ENCONTRADO}).
     */
    public ResourceNotFoundException(String message, BusinessError errorType) {
        super(message, errorType);
    }

    /**
     * Construye una nueva {@code ResourceNotFoundException} con mensaje, código de error y causa raíz.
     *
     * @param message   El mensaje descriptivo indicando qué recurso no fue encontrado.
     * @param errorType Código de error específico del contexto.
     * @param cause     La excepción que causó que el recurso no fuera encontrado.
     */
    public ResourceNotFoundException(String message, BusinessError errorType, Throwable cause) {
        super(message, errorType, cause);
    }
}
