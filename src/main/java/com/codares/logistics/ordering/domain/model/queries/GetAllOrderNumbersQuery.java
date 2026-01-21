package com.codares.logistics.ordering.domain.model.queries;

/**
 * Query para obtener todos los números de pedido existentes.
 * <p>
 * Uso: Pre-carga para validación de duplicados en batch.
 * Retorna solo los números de pedido (no entidades completas)
 * para optimizar memoria en procesamiento masivo.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record GetAllOrderNumbersQuery() {}
