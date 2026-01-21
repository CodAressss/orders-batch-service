package com.codares.logistics.operations.domain.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codares.logistics.operations.domain.model.commands.ProcessBatchCommand;
import com.codares.logistics.operations.domain.model.valueobjects.OrderData;
import com.codares.logistics.operations.domain.model.valueobjects.RowErrorDetail;

/**
 * Domain Service para procesamiento y validación de filas CSV.
 * <p>
 * Contiene la lógica de negocio pura para validar pedidos:
 * <ul>
 *   <li>Validar formato de datos (VOs)</li>
 *   <li>Validar cliente existe y está activo</li>
 *   <li>Validar zona existe</li>
 *   <li>Validar refrigeración compatible con zona</li>
 *   <li>Validar fecha de entrega no es pasada</li>
 *   <li>Validar unicidad de número de pedido</li>
 * </ul>
 * </p>
 * <p>
 * Este servicio es stateless y no depende de infraestructura.
 * Recibe datos pre-cargados via {@link ValidationContext} para evitar N+1.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public interface OrderProcessingDomainService {

    /**
     * Procesa todas las filas del CSV y retorna el resultado.
     * <p>
     * Para cada fila:
     * <ol>
     *   <li>Valida formato (VOs: número pedido, fecha, estado)</li>
     *   <li>Valida cliente existe en contexto pre-cargado</li>
     *   <li>Valida zona existe y soporta refrigeración si aplica</li>
     *   <li>Valida fecha no es pasada (timezone America/Lima)</li>
     *   <li>Valida unicidad (número pedido no duplicado)</li>
     *   <li>Si todo OK → agrega a lista de válidos</li>
     *   <li>Si falla → agrega error a lista</li>
     * </ol>
     * </p>
     * <p>
     * NO lanza excepciones. Acumula errores para procesamiento batch.
     * </p>
     *
     * @param csvRows filas parseadas del CSV
     * @param context contexto con datos pre-cargados para validación
     * @return resultado del procesamiento con pedidos válidos y errores
     */
    ProcessingResult processRows(List<ProcessBatchCommand.CsvRow> csvRows, ValidationContext context);

    /**
     * Contexto de validación con catálogos pre-cargados.
     * <p>
     * Evita N+1 queries al pre-cargar todos los datos de referencia
     * antes de procesar las filas del CSV.
     * </p>
     *
     * @param activeClientIds IDs de clientes activos (Set para O(1) lookup)
     * @param zonesWithRefrigeration Map de zonas con soporte de refrigeración
     * @param existingOrderNumbers números de pedido ya existentes en BD
     */
    record ValidationContext(
        Set<String> activeClientIds,
        Map<String, Boolean> zonesWithRefrigeration,
        Set<String> existingOrderNumbers
    ) {}

    /**
     * Resultado del procesamiento de filas CSV.
     * <p>
     * Contiene los pedidos válidos listos para persistir y los errores
     * para reportar al usuario.
     * </p>
     *
     * @param validOrders datos de pedidos válidos para crear
     * @param errors detalles de errores por fila
     */
    record ProcessingResult(
        List<OrderData> validOrders,
        List<RowErrorDetail> errors
    ) {}
}
