package com.codares.logistics.operations.interfaces.rest;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codares.logistics.operations.application.internal.commandservices.BatchLoadProcessingService;
import com.codares.logistics.operations.domain.model.aggregates.BatchLoad;
import com.codares.logistics.operations.domain.model.commands.ProcessBatchCommand;
import com.codares.logistics.operations.domain.model.queries.GetBatchLoadByIdQuery;
import com.codares.logistics.operations.domain.model.valueobjects.BatchLoadSummary;
import com.codares.logistics.operations.domain.services.BatchLoadQueryService;
import com.codares.logistics.operations.interfaces.rest.resources.BatchLoadResponseResource;
import com.codares.logistics.operations.interfaces.rest.transform.BatchLoadResponseResourceFromEntityAssembler;
import com.codares.logistics.shared.domain.exceptions.InvalidArgumentException;
import com.codares.logistics.shared.domain.exceptions.ResourceAlreadyExistsException;
import com.codares.logistics.shared.domain.model.valueobjects.GlobalError;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la carga masiva de pedidos desde archivo CSV.
 * <p>
 * Responsabilidades (SRP):
 * <ul>
 *   <li>Recibir archivo multipart y header Idempotency-Key</li>
 *   <li>Calcular hash SHA-256 del archivo</li>
 *   <li>Parsear CSV a objetos CsvRow</li>
 *   <li>Delegar procesamiento al orquestador (BatchLoadProcessingService)</li>
 *   <li>Transformar respuesta a Resource (REST DTO)</li>
 * </ul>
 * </p>
 * <p>
 * El Controller NO contiene lógica de negocio ni validaciones.
 * Solo realiza parsing y transformación de datos.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Carga de Pedidos", description = "Endpoint para carga masiva de pedidos desde CSV")
public class OrderLoadController {

    private static final int EXPECTED_COLUMN_COUNT = 6;

    private final BatchLoadProcessingService batchLoadProcessingService;
    private final BatchLoadQueryService batchLoadQueryService;

    /**
     * Carga masiva de pedidos desde archivo CSV.
     * <p>
     * Procesa el archivo línea por línea, valida cada pedido contra reglas de negocio
     * y persiste solo los válidos. Retorna resumen con estadísticas y errores detallados.
     * </p>
     * <p>
     * El header {@code Idempotency-Key} junto con el hash SHA-256 del archivo garantizan
     * idempotencia: si se envía el mismo archivo con la misma clave, no se reprocesa.
     * </p>
     *
     * @param idempotencyKey clave única de idempotencia (obligatorio)
     * @param file           archivo CSV con pedidos a cargar
     * @return resumen del procesamiento con estadísticas y errores
     */
    @PostMapping(value = "/cargar", consumes = "multipart/form-data")
    @Operation(
        summary = "Cargar pedidos desde archivo CSV",
        description = "Procesa un archivo CSV con pedidos, valida cada fila y persiste los válidos. " +
                      "Requiere header Idempotency-Key para garantizar idempotencia."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Archivo procesado exitosamente (puede tener errores parciales)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BatchLoadResponseResource.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación en el archivo o formato inválido"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Archivo ya fue procesado previamente (idempotencia)"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Todas las filas del archivo fallaron validación"
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<BatchLoadResponseResource> uploadOrders(
            @Parameter(
                description = "Clave única de idempotencia para evitar reprocesos",
                required = true,
                example = "order-batch-2025-01-18-001"
            )
            @RequestHeader("Idempotency-Key") String idempotencyKey,

            @Parameter(
                description = "Archivo CSV con pedidos a cargar",
                required = true
            )
            @RequestParam("file") MultipartFile file) {

        log.info("Recibida solicitud de carga. Idempotency-Key={}, filename={}, size={}",
            idempotencyKey, file.getOriginalFilename(), file.getSize());

        // Validar header Idempotency-Key
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidArgumentException(
                "El header Idempotency-Key es obligatorio",
                GlobalError.CAMPO_OBLIGATORIO
            );
        }

        // Validar que se envió un archivo
        if (file.isEmpty()) {
            throw new InvalidArgumentException(
                "El archivo está vacío",
                GlobalError.CAMPO_OBLIGATORIO
            );
        }

        try {
            // Leer contenido del archivo
            byte[] fileBytes = file.getBytes();

            // Calcular hash SHA-256
            String fileHash = DigestUtils.sha256Hex(fileBytes);
            log.debug("Hash SHA-256 calculado: {}", fileHash);

            // Parsear CSV a lista de CsvRow
            List<ProcessBatchCommand.CsvRow> csvRows = parseCsv(fileBytes);
            log.info("CSV parseado: {} filas de datos", csvRows.size());

            // Crear command y delegar al orquestador
            ProcessBatchCommand command = new ProcessBatchCommand(
                idempotencyKey,
                fileHash,
                csvRows
            );

            BatchLoadSummary summary = batchLoadProcessingService.execute(command);

            // Obtener BatchLoad con errores para construir respuesta completa
            BatchLoad batchLoad = batchLoadQueryService
                .handle(new GetBatchLoadByIdQuery(summary.batchLoadId()))
                .orElseThrow(() -> new IllegalStateException(
                    "BatchLoad no encontrado después de procesamiento"
                ));

            // Transformar a resource
            BatchLoadResponseResource response = BatchLoadResponseResourceFromEntityAssembler
                .toResourceFromSummary(summary, batchLoad);

            log.info("Carga completada. Total={}, Guardados={}, Errores={}",
                response.totalProcesados(), response.guardados(), response.conError());

            // Determinar HTTP status basado en resultado
            // - 201 CREATED: Si hay al menos 1 fila guardada exitosamente
            // - 422 UNPROCESSABLE_ENTITY: Si todas las filas fallaron
            HttpStatus status = response.guardados() > 0 
                ? HttpStatus.CREATED 
                : HttpStatus.UNPROCESSABLE_ENTITY;

            return ResponseEntity.status(status).body(response);

        } catch (ResourceAlreadyExistsException e) {
            // Re-lanzar para que GlobalExceptionHandler lo maneje como 409 CONFLICT
            throw e;
        } catch (InvalidArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error procesando archivo CSV", e);
            throw new InvalidArgumentException(
                "Error procesando archivo CSV: " + e.getMessage(),
                GlobalError.FORMATO_INVALIDO
            );
        }
    }

    /**
     * Parsea el contenido del archivo CSV a lista de CsvRow.
     * <p>
     * El Controller es responsable del parsing (SRP).
     * Formato esperado: numeroPedido,clienteId,fechaEntrega,estado,zonaEntrega,requiereRefrigeracion
     * </p>
     *
     * @param fileBytes contenido del archivo en bytes
     * @return lista de filas parseadas
     * @throws InvalidArgumentException si el formato es inválido
     */
    private List<ProcessBatchCommand.CsvRow> parseCsv(byte[] fileBytes) {
        List<ProcessBatchCommand.CsvRow> rows = new ArrayList<>();

        try (CSVReader csvReader = new CSVReaderBuilder(
                new InputStreamReader(
                    new java.io.ByteArrayInputStream(fileBytes),
                    StandardCharsets.UTF_8
                )
            ).build()) {

            String[] header = csvReader.readNext();
            if (header == null) {
                throw new InvalidArgumentException(
                    "El archivo CSV está vacío",
                    GlobalError.FORMATO_INVALIDO
                );
            }

            // Validar header
            if (header.length < EXPECTED_COLUMN_COUNT) {
                throw new InvalidArgumentException(
                    String.format("Se esperaban %d columnas pero se encontraron %d",
                        EXPECTED_COLUMN_COUNT, header.length),
                    GlobalError.FORMATO_INVALIDO
                );
            }

            String[] line;
            int rowNumber = 1;  // Línea 1 es el header, datos empiezan en 2

            while ((line = csvReader.readNext()) != null) {
                rowNumber++;

                // Skip líneas vacías
                if (line.length == 0 || (line.length == 1 && line[0].isBlank())) {
                    continue;
                }

                if (line.length < EXPECTED_COLUMN_COUNT) {
                    log.warn("Línea {} tiene menos columnas de las esperadas: {}",
                        rowNumber, line.length);
                    // Rellenar con valores vacíos para poder reportar el error
                    String[] padded = new String[EXPECTED_COLUMN_COUNT];
                    System.arraycopy(line, 0, padded, 0, line.length);
                    for (int i = line.length; i < EXPECTED_COLUMN_COUNT; i++) {
                        padded[i] = "";
                    }
                    line = padded;
                }

                ProcessBatchCommand.CsvRow csvRow = new ProcessBatchCommand.CsvRow(
                    rowNumber,
                    sanitize(line[0]),
                    sanitize(line[1]),
                    sanitize(line[2]),
                    sanitize(line[3]),
                    sanitize(line[4]),
                    parseBoolean(line[5])
                );

                rows.add(csvRow);
            }

        } catch (CsvValidationException e) {
            throw new InvalidArgumentException(
                "Error de validación CSV: " + e.getMessage(),
                GlobalError.FORMATO_INVALIDO
            );
        } catch (java.io.IOException e) {
            throw new InvalidArgumentException(
                "Error leyendo archivo CSV: " + e.getMessage(),
                GlobalError.FORMATO_INVALIDO
            );
        }

        if (rows.isEmpty()) {
            throw new InvalidArgumentException(
                "El archivo CSV no contiene filas de datos",
                GlobalError.FORMATO_INVALIDO
            );
        }

        return rows;
    }

    /**
     * Sanitiza un valor CSV removiendo espacios en blanco al inicio/fin.
     *
     * @param value valor a sanitizar
     * @return valor sin espacios o cadena vacía si es null
     */
    private String sanitize(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Parsea un string a boolean de forma flexible.
     *
     * @param value valor a parsear (true/false, sí/no, 1/0)
     * @return boolean parseado, false por defecto
     */
    private boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return "true".equals(normalized) 
            || "1".equals(normalized) 
            || "sí".equals(normalized) 
            || "si".equals(normalized);
    }
}
