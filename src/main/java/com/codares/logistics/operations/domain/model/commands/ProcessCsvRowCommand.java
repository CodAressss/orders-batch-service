package com.codares.logistics.operations.domain.model.commands;

import java.time.LocalDate;

/**
 * Command para procesar una fila individual del CSV.
 * <p>
 * Contiene primitivos validados a nivel de formato básico.
 * La validación de reglas de negocio ocurre en el handler.
 * </p>
 *
 * @param rowNumber número de fila en el CSV (para reporte de errores)
 * @param numeroPedido número de pedido (alfanumérico)
 * @param clienteId identificador del cliente
 * @param fechaEntrega fecha de entrega del pedido
 * @param estado estado del pedido (PENDIENTE, CONFIRMADO, ENTREGADO)
 * @param zonaId identificador de la zona de entrega
 * @param requiereRefrigeracion si requiere cadena de frío
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
public record ProcessCsvRowCommand(
    int rowNumber,
    String numeroPedido,
    String clienteId,
    LocalDate fechaEntrega,
    String estado,
    String zonaId,
    boolean requiereRefrigeracion
) {}
