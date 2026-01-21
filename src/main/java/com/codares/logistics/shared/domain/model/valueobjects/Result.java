package com.codares.logistics.shared.domain.model.valueobjects;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Value Object funcional que encapsula el resultado de una operación que puede fallar.
 * <p>
 * Implementa el patrón Result/Either para manejar operaciones fallibles sin recurrir
 * a excepciones, lo cual es especialmente útil en procesamiento batch de alto rendimiento
 * donde el costo de lanzar excepciones (stack trace, unwinding) puede degradar el
 * rendimiento significativamente.
 * </p>
 * <p>
 * Este Value Object es inmutable y thread-safe, siguiendo los principios de programación
 * funcional. Proporciona métodos monádicos ({@code map}, {@code flatMap}) para composición
 * fluida de operaciones.
 * </p>
 * <p>
 * <strong>Casos de uso:</strong>
 * <ul>
 *   <li>Validación de filas en carga masiva de archivos CSV</li>
 *   <li>Operaciones de dominio que pueden fallar por reglas de negocio</li>
 *   <li>Acumulación de errores sin interrumpir el flujo de procesamiento</li>
 * </ul>
 * </p>
 * <p>
 * Ejemplo de uso:
 * <pre>{@code
 * Result<Order> result = orderValidator.validate(row);
 * if (result.isSuccess()) {
 *     ordersToSave.add(result.getValue());
 * } else {
 *     errors.add(new ValidationError(row.getLineNumber(), result.getErrorType(), result.getErrorMessage()));
 * }
 * }</pre>
 * </p>
 *
 * @param <T> El tipo de dato contenido en caso de éxito.
 * @author Aldo Baldeon
 * @version 1.0
 * @since 1.0
 * @see BusinessError
 */
public final class Result<T> {

    private final T value;
    private final BusinessError errorType;
    private final String errorMessage;
    private final boolean success;

    private Result(T value, BusinessError errorType, String errorMessage, boolean success) {
        this.value = value;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.success = success;
    }

    /**
     * Crea un resultado exitoso conteniendo el valor especificado.
     *
     * @param value El valor resultante de la operación exitosa. No debe ser nulo.
     * @param <T>   El tipo del valor.
     * @return Un {@code Result} exitoso con el valor encapsulado.
     * @throws NullPointerException si el valor es nulo.
     */
    public static <T> Result<T> success(T value) {
        Objects.requireNonNull(value, "El valor de un Result exitoso no puede ser nulo");
        return new Result<>(value, null, null, true);
    }

    /**
     * Crea un resultado fallido con el tipo de error y mensaje especificados.
     *
     * @param errorType    El código de error tipificado (ej: {@code OrderError.CLIENTE_NO_ENCONTRADO}).
     * @param errorMessage El mensaje descriptivo del error.
     * @param <T>          El tipo del valor que habría contenido en caso de éxito.
     * @return Un {@code Result} fallido con la información del error.
     * @throws NullPointerException si errorType o errorMessage son nulos.
     */
    public static <T> Result<T> failure(BusinessError errorType, String errorMessage) {
        Objects.requireNonNull(errorType, "El tipo de error no puede ser nulo");
        Objects.requireNonNull(errorMessage, "El mensaje de error no puede ser nulo");
        return new Result<>(null, errorType, errorMessage, false);
    }

    /**
     * Indica si la operación fue exitosa.
     *
     * @return {@code true} si el resultado contiene un valor, {@code false} si contiene un error.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Indica si la operación falló.
     *
     * @return {@code true} si el resultado contiene un error, {@code false} si contiene un valor.
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Obtiene el valor contenido en caso de éxito.
     *
     * @return El valor de la operación exitosa.
     * @throws IllegalStateException si se intenta obtener el valor de un resultado fallido.
     */
    public T getValue() {
        if (!success) {
            throw new IllegalStateException("No se puede obtener el valor de un Result fallido. Error: " + errorType);
        }
        return value;
    }

    /**
     * Obtiene el tipo de error en caso de fallo.
     *
     * @return El código de error tipificado, o {@code null} si el resultado fue exitoso.
     */
    public BusinessError getErrorType() {
        return errorType;
    }

    /**
     * Obtiene el mensaje de error en caso de fallo.
     *
     * @return El mensaje descriptivo del error, o {@code null} si el resultado fue exitoso.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Transforma el valor contenido aplicando la función especificada (operación monádica map).
     * <p>
     * Si el resultado es exitoso, aplica la función al valor y retorna un nuevo Result exitoso.
     * Si el resultado es fallido, retorna el mismo error sin aplicar la función.
     * </p>
     *
     * @param mapper La función de transformación a aplicar.
     * @param <U>    El tipo del valor transformado.
     * @return Un nuevo {@code Result} con el valor transformado o el error original.
     */
    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "La función mapper no puede ser nula");
        if (success) {
            return Result.success(mapper.apply(value));
        }
        return Result.failure(errorType, errorMessage);
    }

    /**
     * Transforma el valor aplicando una función que retorna otro Result (operación monádica flatMap).
     * <p>
     * Útil para encadenar operaciones que pueden fallar independientemente.
     * </p>
     *
     * @param mapper La función de transformación que retorna un Result.
     * @param <U>    El tipo del valor en el Result resultante.
     * @return El Result retornado por la función, o el error original si este Result era fallido.
     */
    public <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
        Objects.requireNonNull(mapper, "La función mapper no puede ser nula");
        if (success) {
            return mapper.apply(value);
        }
        return Result.failure(errorType, errorMessage);
    }

    /**
     * Ejecuta la acción especificada si el resultado es exitoso.
     *
     * @param action La acción a ejecutar con el valor.
     * @return Este mismo {@code Result} para encadenamiento fluido.
     */
    public Result<T> onSuccess(Consumer<? super T> action) {
        Objects.requireNonNull(action, "La acción no puede ser nula");
        if (success) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Ejecuta la acción especificada si el resultado es fallido.
     *
     * @param action La acción a ejecutar con el tipo y mensaje de error.
     * @return Este mismo {@code Result} para encadenamiento fluido.
     */
    public Result<T> onFailure(Consumer<Result<T>> action) {
        Objects.requireNonNull(action, "La acción no puede ser nula");
        if (!success) {
            action.accept(this);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result<?> result = (Result<?>) o;
        return success == result.success &&
                Objects.equals(value, result.value) &&
                Objects.equals(errorType, result.errorType) &&
                Objects.equals(errorMessage, result.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, errorType, errorMessage, success);
    }

    @Override
    public String toString() {
        if (success) {
            return "Result.success(" + value + ")";
        }
        return "Result.failure(" + errorType + ", \"" + errorMessage + "\")";
    }
}
