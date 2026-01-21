-- ============================================================================
-- FLYWAY V1 - SCHEMA INICIAL COMPLETO
-- ============================================================================
-- Crea TODAS las tablas necesarias para la aplicación
-- Los nombres de tabla DEBEN coincidir con la estrategia de Hibernate:
-- SnakeCaseWithPluralizedTablePhysicalNamingStrategy
-- ============================================================================

-- ============================================================================
-- BOUNDED CONTEXT: IAM (Identity and Access Management)
-- ============================================================================
-- Clase: User → Tabla: users (snake_case + plural)
-- Clase: Role → Tabla: roles (snake_case + plural)

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- ============================================================================
-- BOUNDED CONTEXT: CATALOG (Catálogo de Clientes y Zonas)
-- ============================================================================
-- Clase: Client → Tabla: clients (snake_case + plural)
-- Clase: Zone → Tabla: zones (snake_case + plural)

CREATE TABLE clients (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clients_activo ON clients(activo);

CREATE TABLE zones (
    id VARCHAR(20) PRIMARY KEY,
    soporte_refrigeracion BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_zones_soporte_refrigeracion ON zones(soporte_refrigeracion);

-- Insertar datos maestros para pruebas (basado en dataset_validos_100.csv)
INSERT INTO clients (id, name, activo) VALUES
    ('CLI-1', 'Cliente 1', true),
    ('CLI-2', 'Cliente 2', true),
    ('CLI-3', 'Cliente 3', true),
    ('CLI-4', 'Cliente 4', true),
    ('CLI-5', 'Cliente 5', true),
    ('CLI-6', 'Cliente 6', true),
    ('CLI-7', 'Cliente 7', true),
    ('CLI-8', 'Cliente 8', true),
    ('CLI-9', 'Cliente 9', true),
    ('CLI-10', 'Cliente 10', true),
    ('CLI-11', 'Cliente 11', true),
    ('CLI-12', 'Cliente 12', true),
    ('CLI-13', 'Cliente 13', true),
    ('CLI-14', 'Cliente 14', true),
    ('CLI-15', 'Cliente 15', true),
    ('CLI-16', 'Cliente 16', true),
    ('CLI-17', 'Cliente 17', true),
    ('CLI-18', 'Cliente 18', true),
    ('CLI-19', 'Cliente 19', true),
    ('CLI-20', 'Cliente 20', true),
    ('CLI-21', 'Cliente 21', true),
    ('CLI-22', 'Cliente 22', true),
    ('CLI-23', 'Cliente 23', true),
    ('CLI-24', 'Cliente 24', true),
    ('CLI-25', 'Cliente 25', true),
    ('CLI-26', 'Cliente 26', true),
    ('CLI-27', 'Cliente 27', true),
    ('CLI-28', 'Cliente 28', true),
    ('CLI-29', 'Cliente 29', true),
    ('CLI-30', 'Cliente 30', true),
    ('CLI-31', 'Cliente 31', true),
    ('CLI-32', 'Cliente 32', true),
    ('CLI-33', 'Cliente 33', true),
    ('CLI-34', 'Cliente 34', true),
    ('CLI-35', 'Cliente 35', true),
    ('CLI-36', 'Cliente 36', true),
    ('CLI-37', 'Cliente 37', true),
    ('CLI-38', 'Cliente 38', true),
    ('CLI-39', 'Cliente 39', true),
    ('CLI-40', 'Cliente 40', true),
    ('CLI-41', 'Cliente 41', true),
    ('CLI-42', 'Cliente 42', true),
    ('CLI-43', 'Cliente 43', true),
    ('CLI-44', 'Cliente 44', true),
    ('CLI-45', 'Cliente 45', true),
    ('CLI-46', 'Cliente 46', true),
    ('CLI-47', 'Cliente 47', true),
    ('CLI-48', 'Cliente 48', true),
    ('CLI-49', 'Cliente 49', true),
    ('CLI-50', 'Cliente 50', true),
    ('CLI-123', 'Cliente 123', true),
    ('CLI-999', 'Cliente 999', true);

INSERT INTO zones (id, soporte_refrigeracion) VALUES
    ('ZONA1', true),
    ('ZONA2', false),
    ('ZONA3', true),
    ('ZONA4', false),
    ('ZONA5', false);

-- ============================================================================
-- BOUNDED CONTEXT: ORDERING (Gestión de Pedidos)
-- ============================================================================
-- Clase: Order → Tabla: orders (snake_case + plural)

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(10) NOT NULL UNIQUE,
    customer_id VARCHAR(20) NOT NULL,
    zona_id VARCHAR(20) NOT NULL,
    delivery_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    requires_refrigeration BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES clients(id),
    CONSTRAINT fk_orders_zone FOREIGN KEY (zona_id) REFERENCES zones(id)
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_zona_id ON orders(zona_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_delivery_date ON orders(delivery_date);
CREATE INDEX idx_orders_requires_refrigeration ON orders(requires_refrigeration);

-- ============================================================================
-- BOUNDED CONTEXT: OPERATIONS (Procesamiento Batch de CSVs)
-- ============================================================================
-- Clase: BatchLoad → Tabla: cargas_idempotencias (pluralizado por estrategia Hibernate)
-- Clase: LoadError → Tabla: load_errors (snake_case + plural)

CREATE TABLE cargas_idempotencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(50) NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_processed INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    error_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_batch_idempotency UNIQUE (idempotency_key, file_hash)
);

CREATE INDEX idx_cargas_idempotencia_key ON cargas_idempotencias(idempotency_key);
CREATE INDEX idx_cargas_idempotencia_hash ON cargas_idempotencias(file_hash);
CREATE INDEX idx_cargas_idempotencias_status ON cargas_idempotencias(status);

CREATE TABLE load_errors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    carga_id UUID NOT NULL REFERENCES cargas_idempotencias(id) ON DELETE CASCADE,
    row_number INT NOT NULL,
    error_code VARCHAR(50) NOT NULL,
    error_message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_load_errors_carga_id ON load_errors(carga_id);
CREATE INDEX idx_load_errors_error_code ON load_errors(error_code);
