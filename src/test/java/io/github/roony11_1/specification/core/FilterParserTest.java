package io.github.roony11_1.specification.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterParserTest {

    private List<FilterCondition> parse(String field, String value) {
        List<FilterCondition> conditions = new ArrayList<>();
        FilterParser.parseAndAdd(field, value, conditions);
        return conditions;
    }

    @Test
    void valorNuloOVacioNoAgregaCondicion() {
        assertThat(parse("nombre", null)).isEmpty();
        assertThat(parse("nombre", "")).isEmpty();
    }

    @Test
    void valorSimpleEsEquality() {
        var conditions = parse("nombre", "Congelados");
        assertThat(conditions).hasSize(1);
        assertThat(conditions.get(0).getOperator()).isEqualTo(FilterOperator.EQ);
        assertThat(conditions.get(0).getValue()).isEqualTo("Congelados");
    }

    @Test
    void operadorExplicito() {
        var conditions = parse("temperaturaMin", "gt|5");
        assertThat(conditions.get(0).getOperator()).isEqualTo(FilterOperator.GT);
        assertThat(conditions.get(0).getValue()).isEqualTo("5");
    }

    @Test
    void operadorExplicitoCaseInsensitive() {
        var conditions = parse("nombre", "LIKE|Cong");
        assertThat(conditions.get(0).getOperator()).isEqualTo(FilterOperator.LIKE);
    }

    @Test
    void operadorInvalidoLanzaFilterException() {
        assertThatThrownBy(() -> parse("nombre", "FOO|abc"))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("FOO");
    }

    @Test
    void operadorComparacionSinValorLanzaFilterException() {
        assertThatThrownBy(() -> parse("temperaturaMin", "gt|"))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("requiere un valor");
    }

    @Test
    void betweenConDosValores() {
        var conditions = parse("temperaturaMin", "between|0|10");
        assertThat(conditions.get(0).getOperator()).isEqualTo(FilterOperator.BETWEEN);
        assertThat(conditions.get(0).getValue()).isEqualTo("0");
        assertThat(conditions.get(0).getValue2()).isEqualTo("10");
    }

    @Test
    void betweenSinSegundoValorLanzaFilterException() {
        assertThatThrownBy(() -> parse("temperaturaMin", "between|0|"))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("dos valores");
    }

    @Test
    void isNullFunciona() {
        var conditions = parse("descripcion", "IS_NULL|");
        assertThat(conditions.get(0).getOperator()).isEqualTo(FilterOperator.IS_NULL);
        assertThat(conditions.get(0).getValue()).isNull();
    }

    @Test
    void isNotNullFunciona() {
        var conditions = parse("descripcion", "IS_NOT_NULL|");
        assertThat(conditions.get(0).getOperator()).isEqualTo(FilterOperator.IS_NOT_NULL);
    }

    @Test
    void sintaxisInConComas() {
        var conditions = parse("id", "in:1,2,3");
        assertThat(conditions.get(0).getOperator()).isEqualTo(FilterOperator.IN);
        assertThat(conditions.get(0).getValue()).isEqualTo("1,2,3");
    }

    @Test
    void sintaxisInVaciaLanzaFilterException() {
        assertThatThrownBy(() -> parse("id", "in:"))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("IN");
    }

    @Test
    void sintaxisBetweenConComas() {
        var conditions = parse("temperaturaMin", "between:0,10");
        assertThat(conditions.get(0).getOperator()).isEqualTo(FilterOperator.BETWEEN);
        assertThat(conditions.get(0).getValue()).isEqualTo("0");
        assertThat(conditions.get(0).getValue2()).isEqualTo("10");
    }

    @Test
    void sintaxisBetweenMalformadaLanzaFilterException() {
        assertThatThrownBy(() -> parse("temperaturaMin", "between:0"))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("BETWEEN");
        assertThatThrownBy(() -> parse("temperaturaMin", "between:0,"))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("BETWEEN");
    }

    @Test
    void sintaxisInConOperadorExplicito() {
        var conditions = parse("id", "IN|1,2,3");
        assertThat(conditions.get(0).getOperator()).isEqualTo(FilterOperator.IN);
        assertThat(conditions.get(0).getValue()).isEqualTo("1,2,3");
    }
}