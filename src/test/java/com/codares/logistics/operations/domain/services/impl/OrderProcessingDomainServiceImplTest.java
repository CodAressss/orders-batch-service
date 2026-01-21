package com.codares.logistics.operations.domain.services.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.codares.logistics.operations.application.internal.domainservices.OrderProcessingDomainServiceImpl;
import com.codares.logistics.operations.domain.model.commands.ProcessBatchCommand.CsvRow;
import com.codares.logistics.operations.domain.model.valueobjects.OperationsError;
import com.codares.logistics.operations.domain.services.OrderProcessingDomainService.ValidationContext;

/**
 * Suite de pruebas unitarias para {@link OrderProcessingDomainServiceImpl}.
 * <p>
 * Valida la lógica de negocio pura del procesamiento de pedidos.
 * Al ser un servicio de dominio puro (Stateless), no requiere Mocks ni contexto de Spring,
 * lo que garantiza una ejecución extremadamente rápida.
 * </p>
 * <p>
 * <strong>Estrategia:</strong> Se inyecta manualmente un {@link ValidationContext}
 * para simular el estado de la base de datos (clientes, zonas, pedidos previos).
 * </p>
 */
@DisplayName("Reglas de Negocio - Procesamiento de Pedidos")
class OrderProcessingDomainServiceImplTest {

    private OrderProcessingDomainServiceImpl service;

    // Constantes para legibilidad de los tests
    private static final String CLIENTE_EXISTENTE = "CLI-100";
    private static final String CLIENTE_INEXISTENTE = "CLI-999";
    private static final String ZONA_FRIO = "ZONA1";   // Soporta frío
    private static final String ZONA_NORMAL = "ZONA2"; // No soporta frío
    private static final String PEDIDO_EXISTENTE = "P999";

    @BeforeEach
    void setUp() {
        // Instanciamos directamente (Arquitectura Hexagonal Pura)
        service = new OrderProcessingDomainServiceImpl();
    }

    // ===========================
    // TEST 1: Happy Path
    // ===========================

    @Test
    @DisplayName("Debe procesar exitosamente una fila cuando todos los datos son válidos")
    void debe_retornar_exito_cuando_fila_es_valida() {
        // Arrange: Preparamos el contexto "del mundo"
        var contexto = new ValidationContext(
            Set.of(CLIENTE_EXISTENTE),           // Clientes activos
            Map.of(ZONA_FRIO, true, ZONA_NORMAL, false), // Configuración de Zonas
            Set.of(PEDIDO_EXISTENTE)             // Pedidos ya en BD
        );

        var filaValida = new CsvRow(
            1, "P001", CLIENTE_EXISTENTE, LocalDate.now().plusDays(5).toString(),
            "PENDIENTE", ZONA_FRIO, true
        );

        // Act
        var resultado = service.processRows(List.of(filaValida), contexto);

        // Assert
        assertThat(resultado.errors())
            .as("No debería haber errores en un flujo exitoso")
            .isEmpty();
        
        assertThat(resultado.validOrders())
            .as("Debe haber exactamente 1 pedido válido")
            .hasSize(1);
        
        var pedido = resultado.validOrders().get(0);
        assertThat(pedido.orderNumber()).isEqualTo("P001");
        assertThat(pedido.requiresRefrigeration()).isTrue();
    }

    // ======================================
    // TEST 2: Cliente No Existe
    // ======================================

    @Test
    @DisplayName("Debe reportar error si el cliente no existe en el catálogo")
    void debe_fallar_cuando_cliente_no_existe() {
        // Arrange
        var contexto = new ValidationContext(
            Set.of(CLIENTE_EXISTENTE), 
            Map.of(ZONA_FRIO, true), 
            Set.of()
        );

        var fila = new CsvRow(
            1, "P002", CLIENTE_INEXISTENTE, LocalDate.now().plusDays(5).toString(),
            "PENDIENTE", ZONA_FRIO, false
        );

        // Act
        var resultado = service.processRows(List.of(fila), contexto);

        // Assert
        assertThat(resultado.validOrders()).isEmpty();
        assertThat(resultado.errors()).hasSize(1);
        
        var error = resultado.errors().get(0);
        assertThat(error.errorCode())
            .as("El código de error debe indicar cliente no encontrado")
            .isEqualTo(OperationsError.CLIENTE_NO_ENCONTRADO.name());
    }

    // =============================================
    // TEST 3: Zona No Tiene Refrigeración
    // =============================================

    @Test
    @DisplayName("Debe fallar si se solicita refrigeración en una zona que no la soporta")
    void debe_fallar_cuando_zona_no_soporta_refrigeracion() {
        // Arrange
        var contexto = new ValidationContext(
            Set.of(CLIENTE_EXISTENTE),
            Map.of(ZONA_NORMAL, false), // ZONA_NORMAL = false (Sin frío)
            Set.of()
        );

        var fila = new CsvRow(
            1, "P003", CLIENTE_EXISTENTE, LocalDate.now().plusDays(5).toString(),
            "PENDIENTE", ZONA_NORMAL, true // Pide frío
        );

        // Act
        var resultado = service.processRows(List.of(fila), contexto);

        // Assert
        assertThat(resultado.errors())
            .hasSize(1)
            .extracting("errorCode")
            .as("Debe validar la compatibilidad de la cadena de frío")
            .contains(OperationsError.CADENA_FRIO_NO_SOPORTADA.name());
    }

    // ===================================
    // TEST 4: Duplicado en Base de Datos
    // ===================================

    @Test
    @DisplayName("Debe detectar si el número de pedido ya existe en la base de datos")
    void debe_detectar_duplicado_en_bd() {
        // Arrange
        var contexto = new ValidationContext(
            Set.of(CLIENTE_EXISTENTE),
            Map.of(ZONA_FRIO, true),
            Set.of(PEDIDO_EXISTENTE) // "P999" ya existe en el sistema
        );

        var fila = new CsvRow(
            1, PEDIDO_EXISTENTE, CLIENTE_EXISTENTE, LocalDate.now().plusDays(5).toString(),
            "PENDIENTE", ZONA_FRIO, false
        );

        // Act
        var resultado = service.processRows(List.of(fila), contexto);

        // Assert
        assertThat(resultado.errors())
            .hasSize(1)
            .extracting("errorCode")
            .as("No debe permitir procesar pedidos que ya existen en BD")
            .contains(OperationsError.PEDIDO_DUPLICADO.name());
    }

    // ====================================
    // TEST 5: Duplicado Dentro del Archivo
    // ====================================

    @Test
    @DisplayName("Debe detectar duplicados dentro del mismo archivo (Intra-batch)")
    void debe_detectar_duplicado_dentro_del_archivo() {
        // Arrange
        var contexto = new ValidationContext(
            Set.of(CLIENTE_EXISTENTE),
            Map.of(ZONA_FRIO, true),
            Set.of()
        );

        // Dos filas con el mismo ID de pedido "P-NUEVO"
        var fila1 = new CsvRow(1, "P-NUEVO", CLIENTE_EXISTENTE, LocalDate.now().plusDays(5).toString(), "PENDIENTE", ZONA_FRIO, false);
        var fila2 = new CsvRow(2, "P-NUEVO", CLIENTE_EXISTENTE, LocalDate.now().plusDays(5).toString(), "PENDIENTE", ZONA_FRIO, false);

        // Act
        var resultado = service.processRows(List.of(fila1, fila2), contexto);

        // Assert
        // El primero debe pasar
        assertThat(resultado.validOrders())
            .as("La primera aparición del pedido debe ser válida")
            .hasSize(1);
        assertThat(resultado.validOrders().get(0).orderNumber()).isEqualTo("P-NUEVO");

        // El segundo debe fallar
        assertThat(resultado.errors())
            .as("La segunda aparición debe marcarse como error")
            .hasSize(1);
        
        var error = resultado.errors().get(0);
        assertThat(error.rowNumber()).isEqualTo(2);
        assertThat(error.errorCode()).isEqualTo(OperationsError.PEDIDO_DUPLICADO.name());
    }

    // =============================
    // TEST 6: Fecha Pasada
    // =============================

    @Test
    @DisplayName("Debe fallar si la fecha de entrega es anterior a la fecha actual")
    void debe_fallar_si_fecha_es_pasada() {
        // Arrange
        var contexto = new ValidationContext(
            Set.of(CLIENTE_EXISTENTE),
            Map.of(ZONA_FRIO, true),
            Set.of()
        );

        var fila = new CsvRow(
            1, "P004", CLIENTE_EXISTENTE, LocalDate.now().minusDays(1).toString(), // Fecha = Ayer
            "PENDIENTE", ZONA_FRIO, false
        );

        // Act
        var resultado = service.processRows(List.of(fila), contexto);

        // Assert
        assertThat(resultado.errors())
            .hasSize(1)
            .extracting("errorCode")
            .as("No se permiten fechas en el pasado")
            .contains(OperationsError.FECHA_ENTREGA_PASADA.name());
    }
}