package com.codares.logistics.ordering.interfaces.acl;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Fachada del Bounded Context de Ordering.
 * <p>
 * Define el contrato para que otros Bounded Contexts consuman servicios del dominio de órdenes
 * sin conocer la complejidad interna de Commands, Queries y Value Objects.
 * </p>
 * <p>
 * Actúa como Anti-Corruption Layer (ACL) protegiendo el contexto de cambios externos
 * mediante una interfaz simplificada que traduce parámetros primitivos a objetos de dominio.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface OrdersContextFacade {

    /**
     * Crea un nuevo pedido en el sistema.
     * <p>
     * Orquesta la creación de una orden validando todas las reglas de negocio del contexto.
     * Traduce los parámetros simples (strings, booleans) a Value Objects del dominio
     * y delega al CommandService para persistencia.
     * </p>
     *
     * @param orderNumber número del pedido (ej. P001)
     * @param customerId ID del cliente (ej. CLI-123)
     * @param deliveryDate fecha de entrega
     * @param status estado inicial (PENDIENTE, CONFIRMADO, ENTREGADO)
     * @param zoneId ID de la zona de entrega (ej. ZONA1)
     * @param requiresRefrigeration si requiere cadena de frío
     * @return UUID del pedido creado
     * @throws ResourceNotFoundException si cliente o zona no existen
     * @throws ResourceAlreadyExistsException si el número de pedido ya existe
     * @throws InvalidArgumentException si algún parámetro viola reglas de negocio
     */
    UUID createOrder(
            String orderNumber,
            String customerId,
            LocalDate deliveryDate,
            String status,
            String zoneId,
            boolean requiresRefrigeration);

    /**
     * Obtiene un pedido por su número.
     * <p>
     * Realiza una búsqueda por el identificador natural (número de pedido)
     * y retorna el UUID del pedido si existe.
     * </p>
     *
     * @param orderNumber número del pedido
     * @return UUID del pedido si existe, null en caso contrario
     */
    UUID getOrderIdByOrderNumber(String orderNumber);

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS BATCH (para validación en procesamiento masivo)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Verifica si existe un pedido con el número dado.
     * <p>
     * Útil para validación de duplicados antes de crear un pedido.
     * </p>
     *
     * @param orderNumber número del pedido
     * @return true si existe, false en caso contrario
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Obtiene todos los números de pedido existentes.
     * <p>
     * Uso recomendado: Pre-cargar antes de procesar CSV para evitar N queries
     * al verificar duplicados.
     * </p>
     *
     * @return Set con los números de pedido existentes
     */
    java.util.Set<String> getAllOrderNumbers();
}
