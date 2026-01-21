package com.codares.logistics.operations.domain.model.valueobjects;

/**
 * Enumeración que representa los estados válidos de un pedido.
 * <p>
 * Define el ciclo de vida del pedido:
 * <ul>
 *   <li>{@code PENDIENTE}: Pedido recién creado, pendiente de confirmación</li>
 *   <li>{@code CONFIRMADO}: Pedido confirmado para despacho</li>
 *   <li>{@code ENTREGADO}: Pedido entregado al cliente</li>
 * </ul>
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public enum OrderStatus {
    /**
     * Pedido recién creado, pendiente de confirmación.
     */
    PENDIENTE,

    /**
     * Pedido confirmado para despacho.
     */
    CONFIRMADO,

    /**
     * Pedido entregado al cliente.
     */
    ENTREGADO;

    /**
     * Verifica si un string representa un estado válido.
     *
     * @param value el valor a verificar (case-insensitive)
     * @return true si es un estado válido, false en caso contrario
     */
    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            valueOf(value.toUpperCase().trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Convierte un string a OrderStatus de forma segura.
     *
     * @param value el valor a convertir (case-insensitive)
     * @return el OrderStatus correspondiente
     * @throws IllegalArgumentException si el valor no es válido
     */
    public static OrderStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El estado no puede ser nulo o vacío");
        }
        return valueOf(value.toUpperCase().trim());
    }
}
