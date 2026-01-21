# Orders Batch Service

Servicio de procesamiento batch de pedidos para logística, implementado con Domain-Driven Design (DDD) y arquitectura hexagonal.

## 📋 Descripción

Sistema backend para la carga masiva de pedidos desde archivos CSV, con validaciones de negocio, idempotencia y manejo robusto de errores.

### Características principales

- ✅ **Carga masiva de pedidos** desde CSV
- 🔐 **Autenticación JWT** con Spring Security
- 🛡️ **Idempotencia** mediante clave + hash SHA-256
- 📊 **Procesamiento batch** con reportes detallados
- 🔄 **Validaciones de negocio** (fechas, zonas, refrigeración)
- 🗄️ **PostgreSQL** con migraciones Flyway
- 📝 **Auditoría completa** de errores por fila

---

## 🚀 Ejecución Local

### Prerequisitos

- **JDK 17+** (recomendado: OpenJDK 17 o superior)
- **PostgreSQL 14+** (puerto 5432)
- **Maven 3.8+**
- **Git**

### 1️⃣ Clonar el repositorio

```bash
git clone https://github.com/CodAressss/orders-batch-service
cd orders-batch-service
```

### 2️⃣ Configurar PostgreSQL

Crear la base de datos:

```sql
CREATE DATABASE orders_batch_db;
CREATE USER postgres WITH PASSWORD 'coloca_tu_password';
GRANT ALL PRIVILEGES ON DATABASE orders_batch_db TO postgres;
```

> ⚠️ **Nota**: Ajusta las credenciales en `src/main/resources/application.properties` si usas valores diferentes.

### 3️⃣ Ejecutar la aplicación

```bash
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

### 4️⃣ Verificar Swagger UI

Accede a la documentación interactiva:

```
http://localhost:8080/swagger-ui.html
```

---

## 🧪 Pruebas con Postman

### 📥 Importar Colección de Postman

#### Opción 1: Importación Local (Recomendado)

La colección está disponible en la siguiente ubicacion:

1. **Carpeta documentation:** `documentation/postman/`

**Pasos para importar:**

1. Abre Postman
2. Click en **Import** (arriba a la izquierda)
3. Selecciona **Upload Files**
4. Navega a `Orders Batch Service API - Complete.postman_collection.json` en la siguiente carpeta `documentation/postman/Orders Batch Service API - Complete.postman_collection.json`
5. Click en **Import**

#### Opción 2: Link Público (Vista Previa - Sin Archivos CSV)

📌 **[Ver colección en Postman Workspace](https://www.postman.com/codares/workspace/order-batch-service/collection/33909711-77b88765-2075-497a-956b-447edd6f9053?action=share&creator=33909711)**

⚠️ **Limitación:** El link público permite ver la estructura, pero **los archivos CSV no se pueden cargar automáticamente a través del link**. Para pruebas funcionales, usa la importación local (Opción 1).

---

### 📋 Estructura de la Colección

**8 Requests organizados en 3 grupos:**

```
├── 1. Authentication
│   ├── Sign Up - Registrar Usuario
│   └── Sign In - Obtener Token JWT
├── 2. Batch Load - 3 Datasets
│   ├── Load Dataset 1 - 150 Válidos (P001-P150)
│   ├── Load Dataset 2 - 100 Válidos (P151-P250)
│   └── Load Dataset 3 - 100 con Errores (E001-E100)
└── 3. Edge Cases & Security
    ├── Idempotencia - 409 Conflict
    ├── Seguridad - Sin Authorization (401)
    └── Validación - Sin Idempotency-Key (400)
```

---

### 🔄 Flujo de Ejecución

#### Paso 1: Registrar un usuario

**Request:** `Sign Up - Registrar Usuario`

```json
{
  "username": "user",
  "password": "user",
  "roles": ["ROLE_USER"]
}
```

#### Paso 2: Iniciar sesión

**Request:** `Sign In - Obtener Token JWT`

```json
{
  "username": "user",
  "password": "user"
}
```

**Respuesta:** El token se captura automáticamente en la variable `authToken`
```json
{
  "id": "uuid",
  "username": "user",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Paso 3: Cargar archivos CSV (3 datasets)

**Endpoint:** `POST /api/v1/operations/orders/load`

**Headers:**
- `Authorization: Bearer {token}`
- `Idempotency-Key: {unique-key}`

**Body:** Form-data
- Key: `file`
- Type: File
- Value: CSV file

---

### 📊 Los 3 Datasets Disponibles

- **Dataset 1** :`samples/dataset_validos_150.csv`
- **Dataset 2** :`samples/dataset_validos_100.csv`
- **Dataset 3** :`samples/dataset_errores_100.csv`
  

| Dataset | Request | Archivo | Idempotency-Key | Registros | Resultado |
|---------|---------|---------|---|---|---|
| **Dataset 1** | Load Dataset 1 | `dataset_validos_150.csv` | `batch-001-valid-150` | 150 | ✅ 150 guardados, 0 errores |
| **Dataset 2** | Load Dataset 2 | `dataset_validos_100.csv` | `batch-002-valid-100` | 100 | ✅ 100 guardados, 0 errores |
| **Dataset 3** | Load Dataset 3 | `dataset_errores_100.csv` | `batch-003-errors-100` | 100 | ⚠️ ~40 guardados, ~60 errores |

**📍 Ubicación de archivos CSV:** Raíz del proyecto

**Dataset 1 - Respuesta exitosa:**
```json
{
  "batchLoadId": "e66a6b24-bf29-488c-94e0-7822aedc448d",
  "totalProcesados": 150,
  "guardados": 150,
  "conError": 0,
  "erroresPorTipo": {},
  "detalleErrores": []
}
```

**Dataset 3 - Respuesta con errores:**
```json
{
  "batchLoadId": "f77b7c35-cg39-588d-bc1f-8933bfdfe449d",
  "totalProcesados": 100,
  "guardados": 40,
  "conError": 60,
  "erroresPorTipo": {
    "CLIENTE_INACTIVO": 10,
    "FECHA_ENTREGA_PASADA": 10,
    "ZONA_INVALIDA": 10,
    "CADENA_FRIO_NO_SOPORTADA": 30
  },
  "detalleErrores": [
    {
      "lineNumber": 1,
      "orderNumber": "E001",
      "errorCode": "CLIENTE_INACTIVO",
      "errorMessage": "Cliente no existe o está inactivo"
    }
  ]
}
```

### 🔐 Tests de Seguridad & Validación

Después de cargar los datasets, puedes probar los edge cases:

1. **Idempotencia (409 Conflict):** Intenta cargar el mismo dataset con la misma clave
2. **Seguridad (401 Unauthorized):** Intenta sin token JWT
3. **Validación (400 Bad Request):** Intenta sin Idempotency-Key

---

## 📦 Estrategia de Batch

### Arquitectura del Procesamiento

El sistema implementa un **procesamiento batch transaccional** con las siguientes características:

#### 1. **Idempotencia Garantizada**

```
Clave de Idempotencia = Idempotency-Key + SHA-256(archivo)
```

- ✅ Evita duplicados: el mismo archivo con la misma clave se rechaza
- ✅ Permite re-intentos: un archivo diferente con la misma clave se procesa
- ✅ Trazabilidad: cada carga queda registrada en `cargas_idempotencias`

#### 2. **Orquestación en Capa de Aplicación**

```
Controller → Application Service → Domain Service → Repository
```

**Flujo completo:**

1. **Validación de archivo** (CSV válido, tamaño)
2. **Cálculo de hash SHA-256**
3. **Verificación de idempotencia**
4. **Parsing del CSV** (lazy, línea por línea)
5. **Validación de cada fila:**
   - Cliente activo
   - Zona válida
   - Fecha de entrega futura
   - Compatibilidad refrigeración-zona
6. **Creación de BatchLoad** (agregado raíz)
7. **Procesamiento masivo:**
   - Pedidos válidos → se guardan en BD
   - Pedidos inválidos → se registran como errores
8. **Persistencia transaccional** (todo o nada)
9. **Generación de reporte** detallado

#### 3. **Validaciones de Negocio**

| Regla | Error Code | Descripción |
|-------|-----------|-------------|
| Cliente inactivo | `CLIENTE_INACTIVO` | El cliente no existe o está desactivado |
| Zona inválida | `ZONA_INVALIDA` | La zona de reparto no existe |
| Fecha pasada | `FECHA_ENTREGA_PASADA` | La fecha de entrega es anterior a hoy |
| Cadena de frío | `CADENA_FRIO_NO_SOPORTADA` | Zona sin soporte de refrigeración |

#### 4. **Estrategia de Errores**

**Filosofía:** *"Procesar todo, reportar errores"*

- ✅ **NO se detiene el procesamiento** si hay errores en algunas filas
- ✅ **Todas las filas se evalúan** (no fail-fast)
- ✅ **Los pedidos válidos se guardan** aunque haya errores en otros
- ✅ **Errores detallados** por fila (número de línea, código, mensaje)
- ✅ **Estadísticas completas** en la respuesta

#### 5. **Modelo de Datos**

```sql
cargas_idempotencias
├── id (UUID)
├── idempotency_key (varchar)
├── file_hash (varchar SHA-256)
├── status (COMPLETED/FAILED)
├── total_processed (int)
├── success_count (int)
└── error_count (int)

load_errors (1:N con cargas_idempotencias)
├── id (UUID)
├── carga_id (FK)
├── row_number (int)
├── error_code (varchar)
└── error_message (varchar)
```

#### 6. **Tamaño de Lote**

Configurado en `application.properties`:

```properties
app.batch.size=500
```

Ajusta según memoria disponible y tamaño promedio de archivos.

---

## 🏗️ Arquitectura DDD

### Bounded Contexts

1. **IAM** (Identity & Access Management)
   - Usuarios, roles, autenticación JWT

2. **Catalog** (Catálogo)
   - Clientes, zonas de reparto

3. **Ordering** (Gestión de Pedidos)
   - Pedidos con su ciclo de vida

4. **Operations** (Operaciones Batch)
   - Cargas masivas, errores, idempotencia

### Estrategia de Naming

**Hibernate Physical Naming Strategy:**
```
SnakeCaseWithPluralizedTablePhysicalNamingStrategy
```

- Clases en Java: `PascalCase` (ej: `BatchLoad`)
- Tablas en BD: `snake_case` plural (ej: `cargas_idempotencias`)
- Columnas: `snake_case` (ej: `file_hash`)

---

## 📂 Estructura del Proyecto

```
src/main/java/com/codares/logistics/
├── iam/                    # Bounded Context: IAM
├── catalog/                # Bounded Context: Catalog
├── ordering/               # Bounded Context: Ordering
├── operations/             # Bounded Context: Operations (Batch Processing)
│   ├── application/
│   │   └── internal/
│   │       ├── commandservices/
│   │       │   ├── BatchLoadCommandServiceImpl.java
│   │       │   └── BatchLoadProcessingService.java
│   │       ├── domainservices/
│   │       │   └── OrderProcessingDomainServiceImpl.java
│   │       ├── outboundservices/
│   │       │   └── acl/
│   │       │       ├── ExternalCatalogServiceImpl.java
│   │       │       └── ExternalOrdersServiceImpl.java
│   │       └── queryservices/
│   │           └── BatchLoadQueryServiceImpl.java
│   │
│   ├── domain/
│   │   ├── model/
│   │   │   ├── aggregates/
│   │   │   │   └── BatchLoad.java
│   │   │   ├── commands/
│   │   │   │   ├── FailBatchLoadCommand.java
│   │   │   │   ├── FinalizeBatchLoadCommand.java
│   │   │   │   ├── InitiateBatchLoadCommand.java
│   │   │   │   ├── ProcessBatchCommand.java
│   │   │   │   └── ProcessCsvRowCommand.java
│   │   │   ├── entities/
│   │   │   │   └── LoadError.java
│   │   │   ├── queries/
│   │   │   │   ├── CheckIdempotencyQuery.java
│   │   │   │   └── GetBatchLoadByIdQuery.java
│   │   │   └── valueobjects/
│   │   │       ├── BatchLoadStatus.java
│   │   │       ├── BatchLoadSummary.java
│   │   │       ├── CsvRowResult.java
│   │   │       ├── FileHash.java
│   │   │       ├── IdempotencyKey.java
│   │   │       ├── OperationsConstants.java
│   │   │       ├── OperationsError.java
│   │   │       ├── OrderData.java
│   │   │       ├── OrderStatus.java
│   │   │       └── RowErrorDetail.java
│   │   ├── ports/
│   │   │   └── outbound/
│   │   │       ├── ExternalCatalogService.java
│   │   │       └── ExternalOrdersService.java
│   │   └── services/
│   │       ├── BatchLoadCommandService.java
│   │       ├── BatchLoadQueryService.java
│   │       └── OrderProcessingDomainService.java
│   │
│   ├── infrastructure/
│   │   └── persistence/
│   │       └── jpa/
│   │           └── repositories/
│   │               └── BatchLoadRepository.java
│   │
│   └── interfaces/
│       └── rest/
│           ├── OrderLoadController.java
│           ├── resources/
│           │   ├── BatchLoadResponseResource.java
│           │   └── ErrorDetailResource.java
│           └── transform/
│               └── BatchLoadResponseResourceFromEntityAssembler.java
│
└── shared/                 # Infraestructura compartida
    └── (Utilidades comunes)
```

### Explicación de Capas (Hexagonal Architecture)

**1. Domain Layer (Núcleo de Negocio)**
- `model/commands/` → Comandos para orquestar operaciones (CQRS)
- `model/queries/` → Queries para consultar datos
- `model/valueobjects/` → Objetos de valor que encapsulan lógica
- `model/aggregates/` → Raíz del agregado (`BatchLoad`)
- `model/entities/` → Entidades con identidad (`LoadError`)
- `services/` → Servicios de dominio (lógica de negocio)
- `ports/` → Puertos (contratos hacia la infraestructura)

**2. Application Layer (Orquestación)**
- `commandservices/` → Implementación de casos de uso (CQRS)
- `domainservices/` → Servicios que coordinan con dominio
- `outboundservices/acl/` → Anti-Corruption Layer (traduce con sistemas externos)
- `queryservices/` → Servicios de lectura (CQRS)

**3. Infrastructure Layer (Persistencia)**
- `persistence/jpa/` → Adaptadores JPA para PostgreSQL
- `repositories/` → Acceso a datos

**4. Interfaces Layer (Entrada)**
- `rest/` → Controladores REST
- `resources/` → DTOs de respuesta
- `transform/` → Mapeos entre Domain y DTOs

---

## 🔧 Configuración de Aplicación

### Perfiles de Spring

- **dev** (por defecto): logs DEBUG, Flyway enabled
- **prod**: logs INFO, validaciones estrictas
- **test**: H2 in-memory, datos de prueba

### Variables de Entorno (Producción)

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/orders_db
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export AUTHORIZATION_JWT_SECRET=production_secret_min_32_chars
```

---

## 🔧 Perfiles de Spring

- **dev** (por defecto): logs DEBUG, Flyway enabled
- **prod**: logs INFO, validaciones estrictas
- **test**: H2 in-memory, datos de prueba

### Variables de Entorno (Producción)

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/orders_db
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export AUTHORIZATION_JWT_SECRET=production_secret_min_32_chars
```

---

## 📊 Formato CSV Esperado

**Cabecera obligatoria (EXACTAMENTE 6 columnas):**
```csv
numeroPedido,clienteId,fechaEntrega,estado,zonaId,requiresRefrigeration
```

**Ejemplo:**
```csv
numeroPedido,clienteId,fechaEntrega,estado,zonaId,requiresRefrigeration
P001,CLI-1,2026-02-15,PENDIENTE,ZONA1,false
P002,CLI-2,2026-02-20,PENDIENTE,ZONA3,true
```

**Validaciones por columna:**

| Columna | Tipo | Formato | Ejemplo | Validaciones |
|---------|------|---------|---------|--------------|
| `numeroPedido` | String | `[A-Z][0-9]{3}` | P001 | 1 letra mayúscula + 3 dígitos (regex exacto) |
| `clienteId` | String | Alfanumérico | CLI-1 | Debe existir y estar activo en BD |
| `fechaEntrega` | Date | YYYY-MM-DD | 2026-02-15 | Debe ser fecha futura (timezone America/Lima) |
| `estado` | String | Enum | PENDIENTE | Valores: **PENDIENTE**, CONFIRMADO, ENTREGADO |
| `zonaId` | String | Alfanumérico | ZONA1 | Debe existir en el catálogo de zonas |
| `requiresRefrigeration` | Boolean | true/false | false | Zona debe soportar refrigeración si es true |

**⚠️ Notas importantes:**
- `numeroPedido`: **Exactamente el patrón P### (1 letra + 3 dígitos)**
  - ✅ Válidos: P001, P999, A123, Z000
  - ❌ Inválidos: PED-00001, PEDIDO-1, P1, P0001
- Exactamente 6 columnas (sin más, sin menos)
- Headers deben coincidir exactamente (incluyendo mayúsculas/minúsculas)
- Campos no pueden estar vacíos
- Las fechas deben ser **posteriores a hoy** (relativo a timezone America/Lima)

---

## ⛔ Casos que Detienen el Procesamiento (400 Bad Request)

Estas validaciones **DETIENEN** el procesamiento del archivo completo y retornan error 400:

### 1. **Errores de Estructura CSV**
- Header con menos de 6 columnas
- Header con más de 6 columnas
- Header con nombres incorrectos

**Ejemplo:**
```csv
numeroPedido,clienteId,fechaEntrega,estado
# ❌ Solo 4 columnas (esperadas 6)
```

### 2. **Errores de Formato de Value Objects** (Validación en Parsing)
- `numeroPedido` no cumple patrón `[A-Z][0-9]{3}` (ej: P001)
  - ❌ `PED-00001` (demasiados caracteres)
  - ❌ `pedido1` (no empieza con mayúscula)
  - ❌ `P1` (solo 1 dígito)
- `clienteId` no cumple patrón `CLI-[0-9]+`
  - ❌ `CLIENTE-1` (formato incorrecto)
  - ❌ `123` (sin prefijo CLI-)
- `zonaId` no cumple patrón `ZONA[0-9]+`
  - ❌ `ZONA-1` (guión no permitido)
  - ❌ `REGION1` (prefijo incorrecto)
- `estado` no es un valor válido en el parsing
- Campos vacíos o nulos

**Resultado:** Error inmediato, 0 registros procesados

### 3. **¿Por qué se detiene?**
- Son errores de **estructura/formato**, no de negocio
- No se puede continuar si la base del CSV es incorrecta
- Mejor fallar rápido que procesar datos malformados

---

## ✅ Casos que Continúan el Procesamiento (200 OK + Reporte de Errores)

Estas validaciones **NO detienen** el procesamiento, se reportan como errores por fila:

| Error | Descripción | Ejemplo |
|-------|-------------|---------|
| `CLIENTE_INACTIVO` | Cliente no existe o está desactivado | CLI-999 no registrado |
| `ZONA_INVALIDA` | Zona no existe en BD | ZONA999 no existe |
| `FECHA_ENTREGA_PASADA` | Fecha anterior a hoy | 2020-01-01 |
| `CADENA_FRIO_NO_SOPORTADA` | Zona no soporta refrigeración | ZONA2 no soporta frío + requiere=true |
| `PEDIDO_DUPLICADO` | Número de pedido ya existe en BD | P001 ya fue guardado |

**Resultado:** Procesa todo, guarda lo válido, reporta errores

```json
{
  "totalProcesados": 100,
  "guardados": 86,
  "conError": 14,
  "erroresPorTipo": {
    "CLIENTE_INACTIVO": 5,
    "FECHA_ENTREGA_PASADA": 9
  },
  "detalleErrores": [...]
}
```

---

## 📂 Estructura de Datasets de Prueba

### 📋 Resumen de los 3 Datasets

Tenemos 3 datasets con diferentes propósitos, ubicados en la raíz del proyecto:

| Dataset | Registros | Números | Propósito | Resultado Esperado |
|---------|-----------|---------|----------|-------------------|
| `dataset_validos_150.csv` | 150 | P001-P150 | Happy path (todos válidos) | **150 guardados, 0 errores** |
| `dataset_validos_100.csv` | 100 | P151-P250 | Continuación secuencial | **100 guardados, 0 errores** |
| `dataset_errores_100.csv` | 100 | E001-E100 | Validar manejo de errores | **0 guardados, 100 errores reportados** |

---

### 1️⃣ **dataset_validos_150.csv** (Todos válidos - Happy Path)

**Características:**
- ✅ Números: P001 a P150
- ✅ Clientes: CLI-1 a CLI-50 (válidos en BD)
- ✅ Fechas: Futuras (2026-02-15 en adelante)
- ✅ Zonas: ZONA1-ZONA4 (válidas en BD)
- ✅ Refrigeración: Mezcla true/false, combinable con zonas

**Resultado:** **150 registros guardados sin errores**

---

### 2️⃣ **dataset_validos_100.csv** (Continuación secuencial)

**Características:**
- ✅ Números: P151 a P250 (continúa desde el anterior, NO duplicados)
- ✅ Clientes: CLI-1 a CLI-50 (válidos en BD)
- ✅ Fechas: Futuras (2026-02-15 en adelante)
- ✅ Zonas: ZONA1-ZONA4 (válidas en BD)
- ✅ Refrigeración: Mezcla true/false

**Importante:** Usar **diferente Idempotency-Key** que el primer dataset para que se procese como nueva carga.

**Resultado:** **100 registros guardados sin errores**

---

### 3️⃣ **dataset_errores_100.csv** (Errores de negocio intencionales)

**Estructura de errores distribuidos:**

| Rango | Registros | Error de Negocio | Motivo | Error Code |
|-------|-----------|------------------|--------|-----------|
| E001-E010 | 10 | **Clientes inexistentes** | `CLI-9999`, `CLI-8888`, etc. (no existen en BD) | `CLIENTE_INACTIVO` |
| E011-E020 | 10 | **Fechas pasadas** | `2020-01-01` a `2020-10-01` | `FECHA_ENTREGA_PASADA` |
| E021-E030 | 10 | **Zonas inválidas** | `ZONA999`, `ZONA888`, etc. (no existen en BD) | `ZONA_INVALIDA` |
| E031-E060 | 30 | **Refrigeración no soportada** | `requiresRefrigeration=true` + `ZONA2` (ZONA2 no soporta frío) | `CADENA_FRIO_NO_SOPORTADA` |
| E061-E100 | 40 | **Datos válidos** (para línea base) | Clientes válidos, fechas futuras, zonas válidas | Deberían guardarse |

**Resultado:** **~70 registros rechazados, ~30 registros guardados**

**Importante:** Los registros E001-E060 serán rechazados con 200 OK (no detienen el procesamiento), pero reportan cada error específico.

---

## 🧪 Flujo Recomendado de Pruebas

```bash
# 1. Primer carga (debe guardar 150 registros)
POST /api/v1/operations/orders/load
Idempotency-Key: batch-001
file: dataset_validos_150.csv
# Esperado: 150 guardados, 0 errores

# 2. Segunda carga (secuencial P151-P250, sin duplicados)
POST /api/v1/operations/orders/load
Idempotency-Key: batch-002  # KEY DIFERENTE
file: dataset_validos_100.csv
# Esperado: 100 guardados, 0 errores

# 3. Validar manejo de errores de negocio
POST /api/v1/operations/orders/load
Idempotency-Key: batch-003  # KEY DIFERENTE
file: dataset_errores_100.csv
# Esperado: ~30 guardados, ~70 errores reportados

# 4. Probar idempotencia (409 Conflict)
POST /api/v1/operations/orders/load
Idempotency-Key: batch-001  # MISMA KEY que paso 1
file: dataset_validos_150.csv
# Esperado: 409 Conflict (ya fue procesado)
```

---

## 🐛 Troubleshooting

### Error: "no existe la relación cargas_idempotencias"

**Solución:** Verificar que Flyway ejecutó las migraciones:

```sql
SELECT * FROM flyway_schema_history;
```

Si está vacía, reiniciar la aplicación o ejecutar manualmente:

```bash
mvn flyway:migrate
```

### Error: "Cliente inactivo"

**Solución:** Verificar datos maestros:

```sql
SELECT * FROM clients WHERE id = 'CLI-XX';
```

Insertar clientes si es necesario (ver `V1__initial_schema.sql`).

### Error: JWT inválido

**Solución:** El token expira en 1 día. Volver a hacer sign-in.

---

## 📜 Licencia

Proyecto educativo - DDD & Hexagonal Architecture
