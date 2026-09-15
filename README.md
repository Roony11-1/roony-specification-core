# roony-specification-core

Motor y modelo base para la construcción de filtros dinámicos.

`roony-specification-core` define una representación independiente de framework para expresar condiciones de filtrado y operadores, permitiendo que estas condiciones sean posteriormente adaptadas a distintas tecnologías de persistencia.

```text
FilterConditions
       │
       ├───────────────┐
       ▼               ▼
    JPA / Criteria   Otros adaptadores
       │
       ▼
   Predicate
```

El módulo no depende de Spring y está diseñado para servir como núcleo de la familia `roony-specification`.

## ¿Qué problema resuelve?

Los filtros dinámicos suelen terminar acoplados directamente a la tecnología utilizada para consultar los datos.

Por ejemplo, una API puede recibir:

```text
GET /productos?estado=ACTIVO&precio=gte|100
```

pero la interpretación de esos parámetros no debería depender directamente de Spring Data, JPA o de una implementación concreta de persistencia.

Este módulo separa ambas responsabilidades:

```text
Entrada externa
      │
      ▼
Condiciones de filtrado
      │
      ▼
roony-specification-core
      │
      ▼
Adaptador de persistencia
```

De esta forma, el core define **qué significa un filtro**, mientras que los módulos de integración determinan **cómo ejecutarlo**.

## Características

* Modelo independiente para condiciones de filtrado.
* Operadores de comparación y filtrado.
* Soporte para filtros de uno o múltiples valores.
* Soporte para propiedades anidadas.
* Aliases para nombres de propiedades.
* Conversión de valores a tipos Java.
* Validación de sintaxis y valores.
* Excepciones específicas mediante `FilterException`.
* Sin dependencia de Spring.
* Diseñado para ser utilizado por diferentes adaptadores de persistencia.

## Instalación

### Maven

```xml
<dependency>
    <groupId>io.github.roony11-1</groupId>
    <artifactId>roony-specification-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

La versión de JPA utilizada por la aplicación debe ser proporcionada por el consumidor cuando corresponda.

## Uso rápido

Una condición puede representarse a partir de un campo y su valor:

```java
List<FilterCondition> conditions = new ArrayList<>();

FilterParser.parseAndAdd(
        "precio",
        "gte|100",
        conditions
);
```

Las condiciones pueden posteriormente ser entregadas a un adaptador de persistencia:

```text
FilterConditions
      │
      ▼
Adaptador
      │
      ▼
Consulta
```

El core no necesita conocer si la consulta será ejecutada mediante Spring Data JPA, Jakarta Criteria, Hibernate u otra implementación.

## Sintaxis de filtros

Los filtros pueden representar una igualdad simple:

```text
nombre=Juan
```

que conceptualmente corresponde a:

```text
nombre EQ Juan
```

También pueden utilizar operadores explícitos:

```text
precio=gt|1000
estado=neq|INACTIVO
email=like|@gmail.com
nombre=ilike|juan
precio=gte|18
precio=lt|30
precio=lte|30
```

Los operadores pueden escribirse en mayúsculas o minúsculas.

### Operadores multi-valor

Algunos operadores trabajan con múltiples valores:

```text
estado=in:ACTIVO,INACTIVO
precio=between:100,1000
```

que representan:

```text
estado IN (ACTIVO, INACTIVO)
precio BETWEEN 100 AND 1000
```

Si un valor contiene `|` como parte de su contenido y no como operador, debe utilizarse su representación URL encoded:

```text
%7C
```

## Operadores

Los operadores disponibles se representan mediante `FilterOperator`:

| Operador      | Descripción                                     |
| ------------- | ----------------------------------------------- |
| `EQ`          | Igualdad                                        |
| `NE`          | Diferente                                       |
| `LIKE`        | Coincidencia de texto                           |
| `ILIKE`       | Coincidencia de texto sin distinguir mayúsculas |
| `GT`          | Mayor que                                       |
| `GTE`         | Mayor o igual                                   |
| `LT`          | Menor que                                       |
| `LTE`         | Menor o igual                                   |
| `IN`          | Pertenencia a una colección                     |
| `BETWEEN`     | Rango de valores                                |
| `IS_NULL`     | Valor nulo                                      |
| `IS_NOT_NULL` | Valor no nulo                                   |

Los operadores también definen las características de los valores que requieren.

Por ejemplo:

```text
IS_NULL
IS_NOT_NULL
```

son operadores unarios y no requieren un valor.

Mientras que:

```text
BETWEEN
```

requiere dos valores.

## Conversión de tipos

Los valores provenientes de entradas externas suelen representarse inicialmente como `String`.

`ValueConverter` permite convertir estos valores al tipo Java correspondiente.

Entre los tipos soportados se encuentran:

* `Integer`
* `Long`
* `Double`
* `Float`
* `Boolean`
* `Enum`
* `LocalDate`
* `LocalDateTime`
* `Instant`
* `OffsetDateTime`
* `UUID`
* `String`

Las fechas y horas utilizan formatos compatibles con ISO-8601.

Cuando un valor no puede convertirse correctamente, se genera un `FilterException` con información sobre el error.

## Campos anidados

Las condiciones pueden utilizar rutas de propiedades mediante notación con puntos.

Por ejemplo:

```text
direccion.ciudad=Monterrey
```

representa conceptualmente:

```text
direccion.ciudad EQ "Monterrey"
```

Esto permite expresar filtros sobre propiedades pertenecientes a estructuras o relaciones anidadas.

## Aliases

Los aliases permiten exponer nombres diferentes a los nombres reales de las propiedades.

Por ejemplo:

```java
.withAliases(Map.of(
        "cat", "categoria",
        "edad", "cliente.edad"
))
```

permite utilizar:

```text
cat=ELECTRONICA
edad=gte|18
```

mientras que internamente las condiciones hacen referencia a:

```text
categoria
cliente.edad
```

Esto permite desacoplar el contrato externo de la estructura interna de las entidades.

## Construcción manual

Las condiciones también pueden construirse directamente sin utilizar una cadena de query parameters:

```java
List<FilterCondition> conditions = new ArrayList<>();

FilterParser.parseAndAdd(
        "precio",
        "gte|18",
        conditions
);
```

Posteriormente estas condiciones pueden ser utilizadas por el adaptador correspondiente.

## Arquitectura

`roony-specification-core` es la capa central de la familia `roony-specification`.

```text
                    roony-specification-core
                              │
                    FilterConditions
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
   roony-specification-jpa          Otros adaptadores
              │
              ▼
       Jakarta Criteria API
              │
              ▼
       JPA / Hibernate
```

En una aplicación Spring Data JPA:

```text
HTTP Query Parameters
        │
        ▼
query-params
        │
        ▼
specification-core
        │
        ▼
specification-jpa
        │
        ▼
specification-spring
        │
        ▼
Spring Data JPA
```

Cada módulo mantiene una responsabilidad específica:

* `roony-specification-core` — modelo y semántica de los filtros.
* `roony-specification-query-params` — interpretación de parámetros de consulta.
* `roony-specification-jpa` — adaptación a Jakarta Criteria API.
* `roony-specification-spring` — integración con Spring Data JPA.

## Errores

Los errores relacionados con filtros se representan mediante `FilterException`.

Se utiliza ante situaciones como:

* Sintaxis inválida.
* Operadores inexistentes.
* Valores con tipos incompatibles.
* Cantidad incorrecta de valores.
* Rutas de propiedades inválidas.

La excepción pertenece al core y no depende de una tecnología web concreta.

Las integraciones superiores pueden adaptar estos errores al mecanismo de manejo de errores de su framework.

## Dependencias

El objetivo del core es mantener una superficie de dependencias mínima y no acoplar el modelo de filtros a un framework específico.

Las implementaciones de persistencia y las integraciones con frameworks se encuentran en módulos separados.

## Proyecto relacionado

* `roony-specification-query-params` — Conversión de query parameters a condiciones de filtrado.
* `roony-specification-jpa` — Adaptación de las condiciones a Jakarta Criteria API.
* `roony-specification-spring` — Integración con Spring Data JPA.
* `roony-error` — Manejo y representación de errores en integraciones compatibles.

## Versionado

El proyecto utiliza versionado semántico mediante versiones publicadas en Maven Central.

La versión actual es:

```text
1.1.0
```

## Licencia

Este proyecto está disponible bajo la licencia MIT.