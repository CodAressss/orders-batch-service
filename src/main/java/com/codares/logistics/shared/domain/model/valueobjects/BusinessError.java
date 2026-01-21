package com.codares.logistics.shared.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Contrato base para todos los códigos de error de negocio de la plataforma.
 * <p>
 * Esta interfaz define el contrato que deben implementar todos los enums de errores
 * específicos de cada Bounded Context. Permite que el Shared Kernel maneje errores
 * de forma polimórfica sin conocer las implementaciones concretas, respetando el
 * principio de Inversión de Dependencias (DIP).
 * </p>
 * <p>
 * Cada Bounded Context define su propio enum implementando esta interfaz
 * (ej: {@code OrderError}, {@code InventoryError}), mientras que el Shared Kernel
 * provee {@code GlobalError} para errores técnicos transversales.
 * </p>
 * <p>
 * Ejemplo de implementación en un Bounded Context:
 * <pre>{@code
 * public enum OrderError implements BusinessError {
 *     CLIENTE_NO_ENCONTRADO,
 *     ZONA_INVALIDA,
 *     FECHA_ENTREGA_PASADA
 * }
 * }</pre>
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface BusinessError {

    /**
     * Devuelve el código único del error como String.
     * <p>
     * La anotación {@code @JsonValue} asegura que al serializar a JSON se devuelva
     * el nombre del enum como String en lugar del objeto completo, facilitando
     * la interoperabilidad con clientes frontend.
     * </p>
     *
     * @return El código del error (ej: "CLIENTE_NO_ENCONTRADO", "ZONA_INVALIDA").
     */
    @JsonValue
    String name();
}
