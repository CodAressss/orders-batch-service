package com.codares.logistics.operations.application.internal.domainservices;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.codares.logistics.operations.domain.model.commands.ProcessBatchCommand;
import com.codares.logistics.operations.domain.model.valueobjects.OperationsConstants;
import com.codares.logistics.operations.domain.model.valueobjects.OperationsError;
import com.codares.logistics.operations.domain.model.valueobjects.OrderData;
import com.codares.logistics.operations.domain.model.valueobjects.OrderStatus;
import com.codares.logistics.operations.domain.model.valueobjects.RowErrorDetail;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del Domain Service para procesamiento de filas CSV.
 * <p>
 * Contiene la lógica de negocio pura para validar pedidos.
 * No tiene dependencias de infraestructura, solo recibe datos pre-cargados.
 * </p>
 * <p>
 * Valida cada fila contra las reglas de negocio definidas en los requisitos:
 * <ul>
 *   <li>Formato de número de pedido (alfanumérico)</li>
 *   <li>Existencia de cliente activo</li>
 *   <li>Existencia de zona</li>
 *   <li>Compatibilidad refrigeración-zona</li>
 *   <li>Fecha de entrega no pasada (timezone America/Lima)</li>
 *   <li>Estado válido (PENDIENTE, CONFIRMADO, ENTREGADO)</li>
 *   <li>Unicidad de número de pedido</li>
 * </ul>
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
public class OrderProcessingDomainServiceImpl implements OrderProcessingDomainService {

    @Override
    public ProcessingResult processRows(List<ProcessBatchCommand.CsvRow> csvRows, ValidationContext context) {
        log.debug("Procesando {} filas con contexto pre-cargado", csvRows.size());

        List<OrderData> validOrders = new ArrayList<>();
        List<RowErrorDetail> errors = new ArrayList<>();
        
        // Copia mutable para detectar duplicados intra-archivo
        Set<String> processedOrderNumbers = new HashSet<>(context.existingOrderNumbers());

        for (ProcessBatchCommand.CsvRow row : csvRows) {
            var validationResult = validateRow(row, context, processedOrderNumbers);
            
            if (validationResult.isValid()) {
                validOrders.add(validationResult.orderData());
                // Agregar a procesados para detectar duplicados intra-archivo
                processedOrderNumbers.add(row.numeroPedido());
            } else {
                errors.add(validationResult.error());
            }
        }

        log.debug("Procesamiento completado: {} válidos, {} errores", 
            validOrders.size(), errors.size());

        return new ProcessingResult(validOrders, errors);
    }

    /**
     * Valida una fila individual contra todas las reglas de negocio.
     */
    private RowValidationResult validateRow(
            ProcessBatchCommand.CsvRow row, 
            ValidationContext context,
            Set<String> processedOrderNumbers) {

        int rowNumber = row.rowNumber();

        // 1. Validar formato número de pedido (alfanumérico, no vacío)
        if (row.numeroPedido() == null || row.numeroPedido().isBlank()) {
            return RowValidationResult.failure(new RowErrorDetail(
                rowNumber,
                OperationsError.NUMERO_PEDIDO_INVALIDO.name(),
                "El número de pedido es obligatorio"
            ));
        }

        if (!isAlphanumeric(row.numeroPedido())) {
            return RowValidationResult.failure(new RowErrorDetail(
                rowNumber,
                OperationsError.NUMERO_PEDIDO_INVALIDO.name(),
                "El número de pedido debe ser alfanumérico: " + row.numeroPedido()
            ));
        }

        // 2. Validar unicidad (BD + intra-archivo)
        if (processedOrderNumbers.contains(row.numeroPedido())) {
            return RowValidationResult.failure(new RowErrorDetail(
                rowNumber,
                OperationsError.PEDIDO_DUPLICADO.name(),
                "Ya existe un pedido con el número: " + row.numeroPedido()
            ));
        }

        // 3. Validar cliente existe y está activo
        if (row.clienteId() == null || row.clienteId().isBlank()) {
            return RowValidationResult.failure(new RowErrorDetail(
                rowNumber,
                OperationsError.CLIENTE_NO_ENCONTRADO.name(),
                "El ID de cliente es obligatorio"
            ));
        }

        if (!context.activeClientIds().contains(row.clienteId())) {
            return RowValidationResult.failure(new RowErrorDetail(
                rowNumber,
                OperationsError.CLIENTE_NO_ENCONTRADO.name(),
                "Cliente no encontrado o inactivo: " + row.clienteId()
            ));
        }

        // 4. Validar estado válido
        if (!OrderStatus.isValid(row.estado())) {
            return RowValidationResult.failure(new RowErrorDetail(
                rowNumber,
                OperationsError.ESTADO_INVALIDO.name(),
                "Estado inválido: " + row.estado() + ". Valores válidos: " + java.util.Arrays.toString(OrderStatus.values())
            ));
        }
        String estado = OrderStatus.fromString(row.estado()).name();

        // 5. Validar zona existe
        if (row.zonaId() == null || row.zonaId().isBlank()) {
            return RowValidationResult.failure(new RowErrorDetail(
                rowNumber,
                OperationsError.ZONA_NO_ENCONTRADA.name(),
                "El ID de zona es obligatorio"
            ));
        }

        if (!context.zonesWithRefrigeration().containsKey(row.zonaId())) {
            return RowValidationResult.failure(new RowErrorDetail(
                rowNumber,
                OperationsError.ZONA_NO_ENCONTRADA.name(),
                "Zona no encontrada: " + row.zonaId()
            ));
        }

        // 6. Validar refrigeración compatible con zona
        if (row.requiresRefrigeration()) {
            Boolean zoneSupportsColdChain = context.zonesWithRefrigeration().get(row.zonaId());
            if (zoneSupportsColdChain == null || !zoneSupportsColdChain) {
                return RowValidationResult.failure(new RowErrorDetail(
                    rowNumber,
                    OperationsError.CADENA_FRIO_NO_SOPORTADA.name(),
                    "La zona " + row.zonaId() + " no soporta cadena de frío"
                ));
            }
        }

        // 7. Validar y parsear fecha de entrega
        LocalDate fechaEntrega;
        try {
            if (row.fechaEntrega() == null || row.fechaEntrega().isBlank()) {
                return RowValidationResult.failure(new RowErrorDetail(
                    rowNumber,
                    OperationsError.FECHA_ENTREGA_PASADA.name(),
                    "La fecha de entrega es obligatoria"
                ));
            }
            fechaEntrega = LocalDate.parse(row.fechaEntrega());
        } catch (DateTimeParseException e) {
            return RowValidationResult.failure(new RowErrorDetail(
                rowNumber,
                OperationsError.FECHA_ENTREGA_PASADA.name(),
                "Formato de fecha inválido: " + row.fechaEntrega() + ". Use formato YYYY-MM-DD"
            ));
        }

        // 8. Validar fecha no es pasada (timezone America/Lima)
        LocalDate today = LocalDate.now(OperationsConstants.BUSINESS_TIMEZONE);
        if (fechaEntrega.isBefore(today)) {
            return RowValidationResult.failure(new RowErrorDetail(
                rowNumber,
                OperationsError.FECHA_ENTREGA_PASADA.name(),
                "La fecha de entrega no puede ser pasada: " + fechaEntrega
            ));
        }

        // Todas las validaciones pasaron - crear OrderData
        var orderData = new OrderData(
            row.numeroPedido(),
            row.clienteId(),
            fechaEntrega,
            estado,
            row.zonaId(),
            row.requiresRefrigeration()
        );

        return RowValidationResult.success(orderData);
    }

    /**
     * Verifica si un string es alfanumérico (letras, números, guiones).
     */
    private boolean isAlphanumeric(String value) {
        return value.matches("^[a-zA-Z0-9\\-_]+$");
    }

    /**
     * Resultado interno de validación de una fila.
     */
    private record RowValidationResult(
        boolean isValid,
        OrderData orderData,
        RowErrorDetail error
    ) {
        static RowValidationResult success(OrderData orderData) {
            return new RowValidationResult(true, orderData, null);
        }

        static RowValidationResult failure(RowErrorDetail error) {
            return new RowValidationResult(false, null, error);
        }
    }
}
