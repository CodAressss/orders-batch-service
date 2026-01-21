package com.codares.logistics.operations.interfaces.rest.resources;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO de respuesta para el endpoint de carga masiva de pedidos.
 * <p>
 * Contiene estadísticas del procesamiento y detalle de errores agrupados por tipo.
 * </p>
 *
 * @param batchLoadId       ID único del lote procesado
 * @param totalProcesados   cantidad total de filas procesadas
 * @param guardados         cantidad de pedidos persistidos exitosamente
 * @param conError          cantidad de filas con errores de validación
 * @param erroresPorTipo    mapa de errores agrupados por tipo (ej: CLIENTE_NO_ENCONTRADO -> count)
 * @param detalleErrores    lista detallada de errores por fila
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record BatchLoadResponseResource(
    UUID batchLoadId,
    int totalProcesados,
    int guardados,
    int conError,
    Map<String, Long> erroresPorTipo,
    List<ErrorDetailResource> detalleErrores
) {}
