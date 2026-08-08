package io.github.roony11_1.specification.core;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FilterPredicateBuilderTest {

    private final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    private final Root<Object> root = mock(Root.class);
    private final CriteriaQuery<Object> query = mock(CriteriaQuery.class);
    private final Predicate predicate = mock(Predicate.class);

    @SuppressWarnings("unchecked")
    private Path<Object> stubPath(String field, Class<?> javaType) {
        Path<Object> path = mock(Path.class);
        when(path.getJavaType()).thenReturn((Class) javaType);
        String[] parts = field.split("\\.");
        Path<Object> current = root;
        for (int i = 0; i < parts.length; i++) {
            Path<Object> next = (i == parts.length - 1) ? path : mock(Path.class);
            when(current.get(parts[i])).thenReturn(next);
            current = next;
        }
        return path;
    }

    @Test
    void sinCondicionesDevuelveConjuncion() {
        var builder = new FilterPredicateBuilder();
        when(cb.conjunction()).thenReturn(predicate);

        assertThat(builder.toPredicate(root, query, cb)).isSameAs(predicate);
    }

    @Test
    void withConditionsMapExcluyeParametrosDePaginacion() {
        var builder = new FilterPredicateBuilder()
                .withConditions(Map.of("nombre", "Congelados", "page", "1", "size", "10", "sort", "id"));

        Path<Object> path = stubPath("nombre", String.class);
        when(cb.equal(path, "Congelados")).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        builder.toPredicate(root, query, cb);

        verify(cb).equal(path, "Congelados");
    }

    @Test
    void equalityConvierteElValorSegunTipo() {
        var builder = new FilterPredicateBuilder()
                .withCondition(new FilterCondition("id", FilterOperator.EQ, "5"));

        Path<Object> path = stubPath("id", Long.class);
        when(cb.equal(path, 5L)).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        builder.toPredicate(root, query, cb);

        verify(cb).equal(path, 5L);
    }

    @Test
    void inConvierteCadaElementoDeLaLista() {
        var builder = new FilterPredicateBuilder()
                .withCondition(new FilterCondition("id", FilterOperator.IN, "1,2,3"));

        Path<Object> path = stubPath("id", Long.class);
        when(path.in(List.of(1L, 2L, 3L))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        builder.toPredicate(root, query, cb);

        verify(path).in(List.of(1L, 2L, 3L));
    }

    @Test
    void isNullGeneraPredicadoSinValor() {
        var builder = new FilterPredicateBuilder()
                .withCondition(new FilterCondition("descripcion", FilterOperator.IS_NULL, null));

        Path<Object> path = stubPath("descripcion", String.class);
        when(cb.isNull(path)).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        builder.toPredicate(root, query, cb);

        verify(cb).isNull(path);
    }

    @Test
    void betweenConvierteAmbosValores() {
        var builder = new FilterPredicateBuilder()
                .withCondition(new FilterCondition("temperaturaMin", FilterOperator.BETWEEN, "0", "10"));

        Path<Object> path = stubPath("temperaturaMin", Double.class);
        Path<Comparable> comparablePath = mock(Path.class);
        when(path.as(Comparable.class)).thenReturn(comparablePath);
        when(cb.between(eq(comparablePath), eq((Comparable) 0.0), eq((Comparable) 10.0))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        builder.toPredicate(root, query, cb);

        verify(cb).between(eq(comparablePath), eq((Comparable) 0.0), eq((Comparable) 10.0));
    }

    @Test
    void resuelvePathsAnidados() {
        var builder = new FilterPredicateBuilder()
                .withCondition(new FilterCondition("sucursal.nombre", FilterOperator.EQ, "Central"));

        Path<Object> nombre = stubPath("sucursal.nombre", String.class);
        when(cb.equal(nombre, "Central")).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        builder.toPredicate(root, query, cb);

        verify(cb).equal(nombre, "Central");
    }

    @Test
    void campoInexistenteLanzaFilterException() {
        var builder = new FilterPredicateBuilder()
                .withCondition(new FilterCondition("noExiste", FilterOperator.EQ, "x"));

        when(root.get("noExiste")).thenThrow(new IllegalArgumentException("no such field"));

        assertThatThrownBy(() -> builder.toPredicate(root, query, cb))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("noExiste");
    }

    @Test
    void conversionInvalidaLanzaFilterException() {
        var builder = new FilterPredicateBuilder()
                .withCondition(new FilterCondition("temperaturaMin", FilterOperator.GT, "no-numero"));

        Path<Object> path = stubPath("temperaturaMin", Double.class);

        assertThatThrownBy(() -> builder.toPredicate(root, query, cb))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("temperaturaMin");
    }

    @Test
    void combinaMultiplesCondicionesConAnd() {
        var builder = new FilterPredicateBuilder()
                .withCondition(new FilterCondition("nombre", FilterOperator.EQ, "Congelados"))
                .withCondition(new FilterCondition("activo", FilterOperator.EQ, "true"));

        Path<Object> nombre = stubPath("nombre", String.class);
        Path<Object> activo = stubPath("activo", Boolean.class);
        when(cb.equal(nombre, "Congelados")).thenReturn(predicate);
        when(cb.equal(activo, true)).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        builder.toPredicate(root, query, cb);

        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void withAliasesTraduceKeysPublicasAPaths() {
        var builder = new FilterPredicateBuilder()
                .withAliases(Map.of("sucursal", "sucursal.nombre"))
                .withConditions(Map.of("sucursal", "Central"));

        Path<Object> nombre = stubPath("sucursal.nombre", String.class);
        when(cb.equal(nombre, "Central")).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        builder.toPredicate(root, query, cb);

        verify(cb).equal(nombre, "Central");
    }

    @Test
    void withAliasesSinMapeoUsaLaKeyDirecta() {
        var builder = new FilterPredicateBuilder()
                .withAliases(Map.of("sucursal", "sucursal.nombre"))
                .withConditions(Map.of("nombre", "Congelados"));

        Path<Object> nombre = stubPath("nombre", String.class);
        when(cb.equal(nombre, "Congelados")).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        builder.toPredicate(root, query, cb);

        verify(cb).equal(nombre, "Congelados");
    }

    @Test
    void neGeneraNotEqual() {
        var builder = new FilterPredicateBuilder()
                .withCondition(new FilterCondition("nombre", FilterOperator.NE, "X"));

        Path<Object> path = stubPath("nombre", String.class);
        when(cb.notEqual(path, "X")).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        builder.toPredicate(root, query, cb);

        verify(cb).notEqual(path, "X");
    }
}