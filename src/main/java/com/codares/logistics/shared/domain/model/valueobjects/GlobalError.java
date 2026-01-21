package com.codares.logistics.shared.domain.model.valueobjects;

/**
 * Enumeración de errores globales y técnicos compartidos por toda la plataforma.
 * <p>
 * Estos códigos de error representan situaciones genéricas que no pertenecen a ningún
 * Bounded Context específico. Son utilizados como valores por defecto cuando una
 * excepción de dominio no tiene un código de error específico asignado, o para
 * errores puramente técnicos.
 * </p>
 * <p>
 * Los Bounded Contexts deben definir sus propios enums de error (ej: {@code OrderError})
 * para situaciones específicas de su dominio, reservando estos códigos globales para
 * casos verdaderamente transversales.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 * @see BusinessError
 */
public enum GlobalError implements BusinessError {

    /**
     * Error interno no controlado. Usado como fallback para excepciones inesperadas.
     */
    ERROR_INTERNO,

    /**
     * El formato de los datos de entrada es inválido (ej: JSON malformado, CSV corrupto).
     */
    FORMATO_INVALIDO,

    /**
     * Un campo obligatorio no fue proporcionado o está vacío.
     */
    CAMPO_OBLIGATORIO,

    /**
     * El recurso solicitado no existe en el sistema. Código genérico para 404.
     */
    RECURSO_NO_ENCONTRADO,

    /**
     * El recurso que se intenta crear ya existe. Código genérico para 409.
     */
    RECURSO_DUPLICADO,

    /**
     * Un argumento proporcionado viola las reglas de validación. Código genérico para 400.
     */
    ARGUMENTO_INVALIDO,

    /**
     * La solicitud no tiene credenciales válidas de autenticación. Código genérico para 401.
     */
    NO_AUTORIZADO
}
