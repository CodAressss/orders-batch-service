package com.codares.logistics.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Random;

public class CsvDataGenerator {

    private static final String HEADER = "numeroPedido,clienteId,fechaEntrega,estado,zonaEntrega,requiereRefrigeracion";
    private static final Random random = new Random();

    public static void main(String[] args) throws IOException {
        // Generamos 100 filas válidas
        generateValidCsv(100);
        // Generamos 100 filas donde aprox el 30% tiene errores
        generateMixedErrorCsv(100);
        System.out.println("✅ Archivos CSV generados exitosamente en la raíz del proyecto.");
    }

    // Genera un CSV donde TODO debería procesarse OK
    private static void generateValidCsv(int rows) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("dataset_validos_" + rows + ".csv"))) {
            writer.println(HEADER);

            // 1. Primero insertamos los casos EXACTOS del ejemplo del Word para verificar
            writer.println("P001,CLI-123,2025-08-10,PENDIENTE,ZONA1,true");
            writer.println("P002,CLI-999,2025-08-12,ENTREGADO,ZONA5,false");

            // 2. Rellenamos el resto con datos aleatorios válidos (empezando en P003)
            for (int i = 3; i <= rows; i++) {
                String pedido = String.format("P%03d", i); // P003, P004...
                
                // Generamos CLI-1 hasta CLI-50 (que insertamos en el SQL masivo)
                String cliente = "CLI-" + (random.nextInt(50) + 1); 
                
                String zonaNum = String.valueOf(random.nextInt(5) + 1); // 1 a 5
                String zona = "ZONA" + zonaNum;
                
                // Lógica de negocio SQL: Zonas impares (1,3,5) soportan frío. Pares (2,4) no.
                boolean esZonaFria = Integer.parseInt(zonaNum) % 2 != 0;
                
                // Si la zona es fría, pedimos frío aleatoriamente. Si no, obligamos false.
                boolean requiereFrio = esZonaFria && random.nextBoolean();

                String fecha = LocalDate.now().plusDays(random.nextInt(30) + 1).toString();
                String estado = getRandomState();

                writer.printf("%s,%s,%s,%s,%s,%s%n", pedido, cliente, fecha, estado, zona, requiereFrio);
            }
        }
    }

    // Genera un CSV con errores intencionales para probar tu reporte de errores
    private static void generateMixedErrorCsv(int rows) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("dataset_errores_" + rows + ".csv"))) {
            writer.println(HEADER);
            for (int i = 1; i <= rows; i++) {
                String pedido = String.format("E%03d", i); 
                String cliente = "CLI-" + (random.nextInt(50) + 1);
                String zona = "ZONA1";
                String fecha = LocalDate.now().plusDays(5).toString();
                String estado = "PENDIENTE";
                boolean requiereFrio = false;

                // Inyectamos un error cada 3 filas
                if (i % 3 == 0) { 
                    int errorType = random.nextInt(5); // 5 tipos de errores
                    switch (errorType) {
                        case 0 -> cliente = "CLI-NOEXISTE"; // Error: Integridad Referencial
                        case 1 -> zona = "ZONA999";       // Error: Integridad Referencial
                        case 2 -> {                       // Error: Regla de Negocio (Frío)
                            zona = "ZONA2"; // ZONA2 es par, en SQL dice suporte_refrigeracion=false
                            requiereFrio = true; // Error: Pide frío en zona sin soporte
                        }
                        case 3 -> fecha = "2020-01-01";   // Error: Fecha pasada
                        case 4 -> cliente = "CLI-INACTIVO"; // Error: Cliente existe pero inactivo (si implementaste esa validación)
                    }
                }

                writer.printf("%s,%s,%s,%s,%s,%s%n", pedido, cliente, fecha, estado, zona, requiereFrio);
            }
        }
    }

    private static String getRandomState() {
        String[] states = {"PENDIENTE", "CONFIRMADO", "ENTREGADO"};
        return states[random.nextInt(states.length)];
    }
}