package com.codares.logistics.operations.domain.model.commands;

import java.util.List;

/**
 * Command para procesar un lote de pedidos desde CSV.
 * <p>
 * Contiene primitivos + csvRows ya parseadas por el Controller.
 * El Controller es responsable del parsing (respeta SRP).
 * </p>
 *
 * @param idempotencyKey clave de idempotencia del header
 * @param fileHash hash SHA-256 del archivo
 * @param csvRows filas parseadas del CSV (ya convertidas a objetos)
 */
public record ProcessBatchCommand(
    String idempotencyKey,
    String fileHash,
    List<CsvRow> csvRows
) {

    /**
     * Representa una fila parseada del archivo CSV.
     */
    public record CsvRow(
        int rowNumber,
        String numeroPedido,
        String clienteId,
        String fechaEntrega,
        String estado,
        String zonaId,
        boolean requiresRefrigeration
    ) {}
}
