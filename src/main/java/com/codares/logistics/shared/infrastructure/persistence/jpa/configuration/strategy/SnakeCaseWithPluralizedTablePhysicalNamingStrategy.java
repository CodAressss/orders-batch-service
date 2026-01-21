package com.codares.logistics.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import static io.github.encryptorcode.pluralize.Pluralize.pluralize;

/**
 * Estrategia de nomenclatura física SnakeCase con pluralización.
 * <p>
 * Esta clase implementa una estrategia personalizada de nomenclatura para Hibernate que:
 * - Convierte nombres de tablas y columnas de CamelCase a snake_case
 * - Pluraliza automáticamente los nombres de tablas
 * - Mantiene consistencia en la nomenclatura de la base de datos
 * </p>
 * <p>
 * Ejemplo de conversión:
 * - Clase: {@code CapturedPokemon} → Tabla: {@code captured_pokemons}
 * - Atributo: {@code apiSpeciesId} → Columna: {@code api_species_id}
 * </p>
 * <p>
 * Implementa la interfaz {@link PhysicalNamingStrategy} de Hibernate.
 * </p>
 *
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 * @see PhysicalNamingStrategy
 */
public class SnakeCaseWithPluralizedTablePhysicalNamingStrategy implements PhysicalNamingStrategy {
    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Convierte el nombre del esquema a snake_case.
     *
     * @param identifier nombre del esquema
     * @param jdbcEnvironment entorno JDBC
     * @return nombre del esquema en snake_case
     */
    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Convierte el nombre de la tabla a snake_case y lo pluraliza.
     * <p>
     * Ejemplo: {@code CapturedPokemon} → {@code captured_pokemons}
     * </p>
     *
     * @param identifier nombre de la tabla
     * @param jdbcEnvironment entorno JDBC
     * @return nombre de la tabla en snake_case y pluralizado
     */
    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(this.toPlural(identifier));
    }

    /**
     * Convierte el nombre de la secuencia a snake_case.
     *
     * @param identifier nombre de la secuencia
     * @param jdbcEnvironment entorno JDBC
     * @return nombre de la secuencia en snake_case
     */
    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Convierte el nombre de la columna a snake_case.
     * <p>
     * Ejemplo: {@code apiSpeciesId} → {@code api_species_id}
     * </p>
     *
     * @param identifier nombre de la columna
     * @param jdbcEnvironment entorno JDBC
     * @return nombre de la columna en snake_case
     */
    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Convierte un identificador a formato snake_case.
     * <p>
     * Aplica una expresión regular para insertar guiones bajos entre
     * letras minúsculas y mayúsculas, luego convierte todo a minúsculas.
     * </p>
     *
     * @param identifier identificador a convertir
     * @return identificador en snake_case
     */
    private Identifier toSnakeCase(final Identifier identifier) {
        if (identifier == null) return null;

        final String regex = "([a-z])([A-Z])";
        final String replacement = "$1_$2";
        final String newName = identifier.getText()
                .replaceAll(regex, replacement)
                .toLowerCase();
        return Identifier.toIdentifier(newName);
    }

    /**
     * Pluraliza un identificador.
     * <p>
     * Utiliza la librería Pluralize para convertir nombres singulares
     * a su forma plural en inglés.
     * </p>
     *
     * @param identifier identificador a pluralizar
     * @return identificador pluralizado
     */
    private Identifier toPlural(final Identifier identifier) {
        final String newName = pluralize(identifier.getText());
        return Identifier.toIdentifier(newName);
    }
}
