package com.codares.logistics.operations.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.Map;

import com.codares.logistics.operations.domain.ports.outbound.ExternalCatalogService;
import com.codares.logistics.operations.domain.ports.outbound.ExternalOrdersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.codares.logistics.operations.domain.model.aggregates.BatchLoad;
import com.codares.logistics.operations.domain.model.commands.FinalizeBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.InitiateBatchLoadCommand;
import com.codares.logistics.operations.domain.model.commands.ProcessBatchCommand;
import com.codares.logistics.operations.domain.model.commands.ProcessBatchCommand.CsvRow;
import com.codares.logistics.operations.domain.model.valueobjects.BatchLoadStatus;
import com.codares.logistics.operations.domain.model.valueobjects.BatchLoadSummary;
import com.codares.logistics.operations.domain.model.valueobjects.OperationsError;
import com.codares.logistics.operations.domain.model.valueobjects.RowErrorDetail;
import com.codares.logistics.operations.domain.services.BatchLoadCommandService;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService.ProcessingResult;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService.ValidationContext;
import com.codares.logistics.operations.infrastructure.persistence.jpa.repositories.BatchLoadRepository;
import com.codares.logistics.shared.domain.exceptions.ResourceAlreadyExistsException;

/**
 * Suite de pruebas unitarias para {@link BatchLoadProcessingService}.
 * <p>
 * Valida la orquestación del proceso Batch, incluyendo:
 * <ul>
 * <li>Flujo exitoso (Happy Path)</li>
 * <li>Idempotencia (Archivos duplicados)</li>
 * <li>Manejo de errores de negocio</li>
 * </ul>
 * </p>
 * Configurado con {@code Strictness.LENIENT} para evitar falsos positivos en stubs no utilizados durante flujos alternativos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Orquestador Batch - Pruebas Unitarias")
class BatchLoadProcessingServiceTest {

    @Mock
    private BatchLoadRepository batchLoadRepository;

    @Mock
    private BatchLoadCommandService batchLoadCommandService;

    @Mock
    private OrderProcessingDomainService orderProcessingDomainService;

    @Mock
    private ExternalCatalogService catalogService;

    @Mock
    private ExternalOrdersService ordersService;

    private BatchLoadProcessingService service;

    // Hash válido simulado (64 caracteres hexadecimales para cumplir validación SHA-256)
    private static final String HASH_VALIDO = "a".repeat(64);

    @BeforeEach
    void setUp() {
        service = new BatchLoadProcessingService(
            batchLoadRepository,
            batchLoadCommandService,
            orderProcessingDomainService,
            catalogService,
            ordersService
        );
    }

    // ======================================
    // TEST 1: Happy Path - Batch Exitoso
    // ======================================

    @Test
    @DisplayName("Debe procesar el lote exitosamente cuando todas las filas son válidas")
    void debe_procesar_lote_exitosamente_cuando_filas_son_validas() {
        // Arrange (Preparar)
        UUID batchId = UUID.randomUUID();
        String idempotencyKey = "IK-EXITOSO-001";
        LocalDate fechaFutura = LocalDate.now().plusDays(5);

        var filasCsv = List.of(
            new CsvRow(1, "P001", "CLI-100", fechaFutura.toString(), "PENDIENTE", "ZONA1", false)
        );
        var comando = new ProcessBatchCommand(idempotencyKey, HASH_VALIDO, filasCsv);

        // 1. Mock Idempotencia: No existe procesamiento previo
        when(batchLoadRepository.findByIdempotencyKeyAndFileHash(any(), any()))
            .thenReturn(Optional.empty());

        // 2. Mock Iniciar Carga
        BatchLoad batchIniciado = mock(BatchLoad.class);
        when(batchIniciado.getId()).thenReturn(batchId);
        when(batchLoadCommandService.handle(any(InitiateBatchLoadCommand.class)))
            .thenReturn(batchIniciado);

        // 3. Mock Pre-carga de Contexto
        lenient().when(catalogService.getAllActiveClientIds()).thenReturn(Set.of());
        lenient().when(catalogService.getZonesWithRefrigerationSupport()).thenReturn(Map.of());
        lenient().when(ordersService.getAllOrderNumbers()).thenReturn(Set.of());

        // 4. Mock Dominio: Retorna éxito (lista de válidos, sin errores)
        var resultadoExitoso = new ProcessingResult(List.of(), List.of());
        when(orderProcessingDomainService.processRows(anyList(), any(ValidationContext.class)))
            .thenReturn(resultadoExitoso);

        // 5. Mock Finalizar
        BatchLoad batchFinalizado = mock(BatchLoad.class);
        when(batchFinalizado.getId()).thenReturn(batchId);
        when(batchFinalizado.getTotalProcessed()).thenReturn(1);
        when(batchFinalizado.getSuccessCount()).thenReturn(1);
        
        when(batchLoadCommandService.handle(any(FinalizeBatchLoadCommand.class)))
            .thenReturn(batchFinalizado);

        // Act (Ejecutar)
        BatchLoadSummary resumen = service.execute(comando);

        // Assert (Verificar)
        assertThat(resumen).isNotNull();
        assertThat(resumen.batchLoadId()).isEqualTo(batchId);
        
        verify(batchLoadCommandService).handle(any(InitiateBatchLoadCommand.class));
        verify(orderProcessingDomainService).processRows(anyList(), any(ValidationContext.class));
        verify(batchLoadCommandService).handle(any(FinalizeBatchLoadCommand.class));
    }

    // =============================================
    // TEST 2: Idempotencia - Batch Ya Completado
    // =============================================

    @Test
    @DisplayName("Debe retornar el resumen histórico si el archivo ya fue procesado completamente")
    void debe_retornar_resumen_historico_si_ya_existe_completado() {
        // Arrange
        String idempotencyKey = "IK-YA-EXISTE";
        var comando = new ProcessBatchCommand(idempotencyKey, HASH_VALIDO, List.of());

        // Mock: Ya existe y está en estado COMPLETED
        BatchLoad batchExistente = mock(BatchLoad.class);
        when(batchExistente.getStatus()).thenReturn(BatchLoadStatus.COMPLETED);
        when(batchExistente.getId()).thenReturn(UUID.randomUUID());
        
        when(batchLoadRepository.findByIdempotencyKeyAndFileHash(any(), any()))
            .thenReturn(Optional.of(batchExistente));

        // Act
        BatchLoadSummary resumen = service.execute(comando);

        // Assert
        assertThat(resumen).isNotNull();
        
        // Verificar que NO se llamó al dominio ni a comandos de escritura (Idempotencia real)
        verify(orderProcessingDomainService, never()).processRows(anyList(), any());
        verify(batchLoadCommandService, never()).handle(any(InitiateBatchLoadCommand.class));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción si el archivo se está procesando actualmente (Concurrencia)")
    void debe_lanzar_excepcion_si_esta_en_proceso() {
        // Arrange
        var comando = new ProcessBatchCommand("IK-EN-PROCESO", HASH_VALIDO, List.of());
        
        BatchLoad batchEnProceso = mock(BatchLoad.class);
        when(batchEnProceso.getStatus()).thenReturn(BatchLoadStatus.PROCESSING);
        
        when(batchLoadRepository.findByIdempotencyKeyAndFileHash(any(), any()))
            .thenReturn(Optional.of(batchEnProceso));
            
        // Act & Assert
        assertThatThrownBy(() -> service.execute(comando))
            .isInstanceOf(ResourceAlreadyExistsException.class)
            .hasMessageContaining("está siendo procesado");
    }

    // =========================================================
    // TEST 3: Validación Fallida (Errores de negocio)
    // =========================================================

    @Test
    @DisplayName("Debe registrar y contar los errores devueltos por el dominio")
    void debe_registrar_errores_de_dominio() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        
        var comando = new ProcessBatchCommand("IK-CON-ERRORES", HASH_VALIDO, 
            List.of(new CsvRow(1, "P", "C", "F", "E", "Z", false)));

        // Idempotencia OK
        when(batchLoadRepository.findByIdempotencyKeyAndFileHash(any(), any()))
            .thenReturn(Optional.empty());

        // Iniciar OK
        BatchLoad batchIniciado = mock(BatchLoad.class);
        when(batchIniciado.getId()).thenReturn(batchId);
        lenient().when(batchLoadCommandService.handle(any(InitiateBatchLoadCommand.class)))
            .thenReturn(batchIniciado);

        // Pre-carga mocks (lenient para evitar falsos positivos)
        lenient().when(catalogService.getAllActiveClientIds()).thenReturn(Set.of());
        lenient().when(catalogService.getZonesWithRefrigerationSupport()).thenReturn(Map.of());
        lenient().when(ordersService.getAllOrderNumbers()).thenReturn(Set.of());

        // Mock Dominio retornando 1 error de negocio
        var detalleError = new RowErrorDetail(1, OperationsError.CADENA_FRIO_NO_SOPORTADA.name(), "Error frío");
        var resultadoConError = new ProcessingResult(List.of(), List.of(detalleError));
        
        when(orderProcessingDomainService.processRows(anyList(), any(ValidationContext.class)))
            .thenReturn(resultadoConError);

        // Finalizar
        BatchLoad batchFinalizado = mock(BatchLoad.class);
        when(batchFinalizado.getId()).thenReturn(batchId);
        when(batchFinalizado.getErrorCount()).thenReturn(1); // Simulamos que se guardó 1 error
        
        lenient().when(batchLoadCommandService.handle(any(FinalizeBatchLoadCommand.class)))
            .thenReturn(batchFinalizado);

        // Act
        BatchLoadSummary resumen = service.execute(comando);

        // Assert
        assertThat(resumen.failureCount())
            .as("El resumen debe reflejar 1 error de negocio")
            .isEqualTo(1);
        
        // Verificar que se llamó a finalizar (donde se guardan los errores)
        verify(batchLoadCommandService).handle(any(FinalizeBatchLoadCommand.class));
    }
}