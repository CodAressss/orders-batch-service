package com.codares.logistics.shared.domain.exceptions;

import com.codares.logistics.shared.domain.model.valueobjects.BusinessError;
import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;
import lombok.Getter;

/**
 * Excepción base abstracta para todas las excepciones de dominio del Shared Kernel.
 * <p>
 * Esta excepción actúa como clase padre para todas las excepciones específicas del negocio
 * que pueden ocurrir en el sistema. Implementa el patrón de excepciones tipificadas mediante
 * {@link BusinessError}, permitiendo que cada error transporte un código semántico que puede
 * ser utilizado tanto por el {@code GlobalExceptionHandler} para respuestas REST, como por
 * el procesamiento batch para acumular errores.
 * </p>
 * <p>
 * Al ser una excepción de tiempo de ejecución ({@code RuntimeException}), no requiere ser
 * declarada explícitamente en las firmas de métodos, facilitando su uso en operaciones de
 * transacción donde Spring Data puede manejarla automáticamente.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 * @see BusinessError
 * @see GlobalError
 */
@Getter
public class DomainException extends RuntimeException {

    /**
     * Código de error tipificado que identifica semánticamente el tipo de error de negocio.
     * Puede ser un {@link GlobalError} para errores genéricos, o un enum específico del
     * Bounded Context (ej: {@code OrderError}) para errores de dominio específicos.
     */
    private final BusinessError errorType;

    /**
     * Construye una nueva {@code DomainException} con el mensaje de error especificado.
     * <p>
     * Utiliza {@link GlobalError#ERROR_INTERNO} como código de error por defecto.
     * </p>
     *
     * @param message El mensaje descriptivo del error de negocio.
     */
    public DomainException(String message) {
        super(message);
        this.errorType = GlobalError.ERROR_INTERNO;
    }

    /**
     * Construye una nueva {@code DomainException} con mensaje y código de error tipificado.
     * <p>
     * Este es el constructor principal para reglas de negocio específicas, permitiendo
     * transportar tanto el mensaje descriptivo como el código semántico del error.
     * </p>
     *
     * @param message   El mensaje descriptivo del error de negocio.
     * @param errorType Código de error específico del contexto (ej: {@code OrderError.CLIENTE_INACTIVO}).
     */
    public DomainException(String message, BusinessError errorType) {
        super(message);
        this.errorType = errorType != null ? errorType : GlobalError.ERROR_INTERNO;
    }

    /**
     * Construye una nueva {@code DomainException} con mensaje, código de error y causa raíz.
     *
     * @param message   El mensaje descriptivo del error de negocio.
     * @param errorType Código de error específico del contexto.
     * @param cause     La excepción que causó este error.
     */
    public DomainException(String message, BusinessError errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType != null ? errorType : GlobalError.ERROR_INTERNO;
    }

    /**
     * Construye una nueva {@code DomainException} con mensaje y causa raíz.
     * <p>
     * Utiliza {@link GlobalError#ERROR_INTERNO} como código de error por defecto.
     * </p>
     *
     * @param message El mensaje descriptivo del error de negocio.
     * @param cause   La excepción que causó este error.
     */
    public DomainException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = GlobalError.ERROR_INTERNO;
    }
}
