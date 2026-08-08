# roony-specification-core

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-21%2B-blue)

Filtros dinámicos para consultas **JPA / JPA Criteria**, sin dependencias de Spring. Construye predicados a partir de pares `campo → valor` (vía query params), de forma segura y tipada, listo para usar con Spring Data JPA, Quarkus/Hibernate o JPA puro.

## Instalación

```xml
<dependency>
    <groupId>io.github.roony11-1</groupId>
    <artifactId>roony-specification-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

> `jakarta.persistence-api` se declara con scope `provided`: tu proyecto ya trae el proveedor JPA.

## Uso rápido

En un repositorio Spring Data JPA:

```java
@Service
public class ProductoService {

    private final ProductoRepository repository;

    public List<Producto> buscar(Map<String, String> filtros) {
        return repository.findAll((root, query, cb) -> new FilterPredicateBuilder()
                .withConditions(filtros)          // mapea el query string a condiciones
                .withAliases(Map.of("cat", "categoria"))
                .toPredicate(root, query, cb));
    }
}
```

Así, una petición como `GET /productos?estado=ACTIVO&precio=gte|100&cat=ELECTRONICA&page=0&size=20` se traduce en predicados. Los parámetros `page`, `size` y `sort` se ignoran automáticamente (para que no den error en la Criteria).

## Sintaxis de filtros

### Igualdad simple

`?nombre=Juan` → `nombre EQ Juan`

### Operadores explícitos (`operador|valor`)

Puedes usar el operador en mayúsculas o minúsculas:

| Query param | Predicado |
|---|---|
| `?precio=gt\|1000` | `precio GT 1000` |
| `?estado=neq\|INACTIVO` | `estado NE INACTIVO` |
| `?email=like\|@gmail.com` | `email LIKE '%@gmail.com%'` |
| `?nombre=ilike\|juan` | `nombre UPPER LIKE '%JUAN%'` (case-insensitive) |
| `?precio=gte\|18` | `precio GTE 18` |
| `?precio=lt\|30` | `precio LT 30` |
| `?precio=lte\|30` | `precio LTE 30` |
| `?estado=is_null` / `is_not_null` | `estado IS NULL` / `IS NOT NULL` |

> Si un valor contiene `|` que no forma parte de la sintaxis, debe url-encodearse como `%7C`.

### Operadores multi-valor

```text
?estado=in:ACTIVO,INACTIVO       → estado IN (ACTIVO, INACTIVO)
?precio=between:100,1000          → precio BETWEEN 100 AND 1000
```

### Lista de operadores (`FilterOperator`)

`EQ`, `NE`, `LIKE`, `ILIKE`, `GT`, `GTE`, `LT`, `LTE`, `IN`, `BETWEEN`, `IS_NULL`, `IS_NOT_NULL`.

- `isUnary()`: `IS_NULL`, `IS_NOT_NULL` (no requieren valor)
- `isBinary()`: `BETWEEN` (requiere dos valores)

## Conversión automática de tipos

`ValueConverter` convierte el string del query al tipo Java de la columna (`path.getJavaType()`):

- Primitivos y wrappers: `Integer`, `Long`, `Double`, `Float`, `Boolean`
- Enums (por nombre)
- `LocalDate`, `LocalDateTime`, `Instant`, `OffsetDateTime` (ISO-8601)
- `UUID`
- `String`

Si el formato es inválido lanza `FilterException` con un mensaje legible.

## Campos anidados

Soporta rutas con puntos para relaciones: `?direccion.ciudad=Monterrey` → `direccion.ciudad EQ 'Monterrey'`.

## Aliases

Con `withAliases(Map)` puedes exponer nombres cortos o protegidos en la API y mapearlos al campo real de la entidad:

```java
.withAliases(Map.of(
    "cat", "categoria",
    "edad", "cliente.edad"
))
```

## Construcción manual

También puedes escribir condiciones directamente:

```java
List<FilterCondition> conditions = new ArrayList<>();
FilterParser.parseAndAdd("precio", "gte|18", conditions);

Specification<Producto> spec = (root, query, cb) -> new FilterPredicateBuilder()
        .withConditions(conditions)
        .toPredicate(root, query, cb);
```

## Errores

`FilterException` (un `RuntimeException`) se lanza ante sintaxis inválida, valores mal tipados o rutas de campo inexistentes. El puente `roony-specification-error-spring` la convierte en una respuesta de error `400` con formato `roony-error`.

---

MIT License · [Roony11-1](https://github.com/roony11-1)