package com.codares.logistics.ordering.infrastructure.persistence.jpa.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codares.logistics.ordering.domain.model.aggregates.Order;

/**
 * Repositorio JPA para el agregado Order.
 * <p>
 * Proporciona operaciones de persistencia (CRUD) y consultas especializadas
 * para el agregado Order, delegando en Spring Data JPA.
 * </p>
 * <p>
 * Los métodos usan @Query explícitas para acceder a las propiedades internas
 * de los Value Objects embebidos (ej. orderNumber.value).
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Verifica si existe un pedido con el número de pedido especificado.
     *
     * @param orderNumber el número del pedido a buscar
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.orderNumber.value = :orderNumber")
    boolean existsByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * Obtiene un pedido por su número de pedido.
     *
     * @param orderNumber el número del pedido
     * @return un Optional con el Order si existe
     */
    @Query("SELECT o FROM Order o WHERE o.orderNumber.value = :orderNumber")
    Optional<Order> findByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * Obtiene todos los pedidos con un estado específico.
     *
     * @param status el estado a filtrar (PENDIENTE, CONFIRMADO, ENTREGADO)
     * @return lista de Orders con el estado especificado
     */
    @Query("SELECT o FROM Order o WHERE o.status.value = :status")
    List<Order> findByStatus(@Param("status") String status);

    /**
     * Obtiene todos los pedidos con una fecha de entrega específica.
     *
     * @param deliveryDate la fecha de entrega a filtrar
     * @return lista de Orders con esa fecha de entrega
     */
    @Query("SELECT o FROM Order o WHERE o.deliveryDate.value = :deliveryDate")
    List<Order> findByDeliveryDate(@Param("deliveryDate") LocalDate deliveryDate);

    /**
     * Obtiene todos los pedidos de una zona específica.
     *
     * @param zonaId el ID de la zona
     * @return lista de Orders de esa zona
     */
    @Query("SELECT o FROM Order o WHERE o.zonaId.value = :zonaId")
    List<Order> findByZonaId(@Param("zonaId") String zonaId);

    /**
     * Obtiene todos los pedidos de un cliente específico.
     *
     * @param customerId el ID del cliente
     * @return lista de Orders del cliente
     */
    @Query("SELECT o FROM Order o WHERE o.customerId.value = :customerId")
    List<Order> findByCustomerId(@Param("customerId") String customerId);

    // ═══════════════════════════════════════════════════════════════
    // QUERIES BATCH (para validación en procesamiento masivo)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Obtiene todos los números de pedido existentes.
     * <p>
     * Uso: Pre-carga para validación de duplicados en batch.
     * Retorna solo strings (no entidades completas) para optimizar memoria.
     * </p>
     *
     * @return Set con los números de pedido existentes
     */
    @Query("SELECT o.orderNumber.value FROM Order o")
    Set<String> findAllOrderNumbers();
}
