package io.github.roony11_1.specification.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueConverterTest {

    @Test
    void nullYVacioDevuelvenNull() {
        assertThat(ValueConverter.convertValue(Integer.class, null)).isNull();
        assertThat(ValueConverter.convertValue(Integer.class, "")).isNull();
    }

    @Test
    void targetTypeNullDevuelveElValorCrudo() {
        assertThat(ValueConverter.convertValue(null, "abc")).isEqualTo("abc");
    }

    @Test
    void convierteString() {
        assertThat(ValueConverter.convertValue(String.class, "abc")).isEqualTo("abc");
    }

    @Test
    void conviertePrimitivos() {
        assertThat(ValueConverter.convertValue(Integer.class, "42")).isEqualTo(42);
        assertThat(ValueConverter.convertValue(Long.class, "42")).isEqualTo(42L);
        assertThat(ValueConverter.convertValue(Double.class, "3.5")).isEqualTo(3.5);
        assertThat(ValueConverter.convertValue(Float.class, "3.5")).isEqualTo(3.5f);
        assertThat(ValueConverter.convertValue(Boolean.class, "true")).isEqualTo(true);
    }

    @Test
    void convierteEnum() {
        assertThat(ValueConverter.convertValue(TestEnum.class, "A")).isEqualTo(TestEnum.A);
    }

    @Test
    void enumInvalidoLanzaFilterException() {
        assertThatThrownBy(() -> ValueConverter.convertValue(TestEnum.class, "X"))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("TestEnum");
    }

    @Test
    void convierteFechas() {
        assertThat(ValueConverter.convertValue(LocalDate.class, "2026-08-01"))
                .isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(ValueConverter.convertValue(LocalDateTime.class, "2026-08-01T10:30:00"))
                .isEqualTo(LocalDateTime.of(2026, 8, 1, 10, 30, 0));
        assertThat(ValueConverter.convertValue(Instant.class, "2026-08-01T10:30:00Z"))
                .isEqualTo(Instant.parse("2026-08-01T10:30:00Z"));
        assertThat(ValueConverter.convertValue(OffsetDateTime.class, "2026-08-01T10:30:00+02:00"))
                .isEqualTo(OffsetDateTime.parse("2026-08-01T10:30:00+02:00"));
    }

    @Test
    void fechaMalformadaLanzaFilterException() {
        assertThatThrownBy(() -> ValueConverter.convertValue(LocalDate.class, "01-08-2026"))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("yyyy-MM-dd");
        assertThatThrownBy(() -> ValueConverter.convertValue(Instant.class, "ayer"))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("ISO-8601");
    }

    @Test
    void convierteUuid() {
        var id = "123e4567-e89b-12d3-a456-426614174000";
        assertThat(ValueConverter.convertValue(UUID.class, id)).isEqualTo(UUID.fromString(id));
    }

    @Test
    void uuidInvalidoLanzaFilterException() {
        assertThatThrownBy(() -> ValueConverter.convertValue(UUID.class, "no-es-uuid"))
                .isInstanceOf(FilterException.class)
                .hasMessageContaining("UUID");
    }

    @Test
    void tipoDesconocidoDevuelveElValorCrudo() {
        assertThat(ValueConverter.convertValue(StringBuilder.class, "abc")).isEqualTo("abc");
    }

    @Test
    void convertListConvierteCadaElemento() {
        List<Object> result = ValueConverter.convertList(Long.class, "1,2,3");
        assertThat(result).containsExactly(1L, 2L, 3L);
    }

    @Test
    void convertListToleraEspacios() {
        List<Object> result = ValueConverter.convertList(String.class, "a, b ,c");
        assertThat(result).containsExactly("a", "b", "c");
    }

    @Test
    void convertListVacioLanzaFilterException() {
        assertThatThrownBy(() -> ValueConverter.convertList(Long.class, ""))
                .isInstanceOf(FilterException.class);
    }

    @Test
    void convertListConElementoVacioLanzaFilterException() {
        assertThatThrownBy(() -> ValueConverter.convertList(Long.class, "1,,3"))
                .isInstanceOf(FilterException.class);
    }

    enum TestEnum { A, B }
}