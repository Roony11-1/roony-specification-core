package io.github.roony11_1.specification.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ValueConverter 
{
    public static Object convertValue(Class<?> targetType, String value) 
    {
        if (value == null || value.isEmpty()) 
            return null;

        if (targetType == null) 
            return value;
        
        // Enums
        if (targetType.isEnum()) 
        {
            try 
            {
                return Enum.valueOf((Class<Enum>) targetType, value);
            } 
            catch (IllegalArgumentException e) 
            {
                throw new FilterException("Valor inválido para enum " + targetType.getSimpleName() + ": " + value);
            }
        }
        
        // Primitivos y wrappers
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(value);
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(value);
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(value);
        }
        if (targetType == Float.class || targetType == float.class) {
            return Float.valueOf(value);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.valueOf(value);
        }
        if (targetType == String.class) {
            return value;
        }
        
        // Fechas
        if (targetType == LocalDate.class) {
            try {
                return LocalDate.parse(value);
            } catch (Exception e) {
                throw new FilterException("Formato de fecha inválido: " + value + ". Use yyyy-MM-dd");
            }
        }
        if (targetType == LocalDateTime.class) {
            try {
                return LocalDateTime.parse(value);
            } catch (Exception e) {
                throw new FilterException("Formato de fecha-hora inválido: " + value + ". Use yyyy-MM-ddTHH:mm:ss");
            }
        }
        if (targetType == Instant.class) {
            try {
                return Instant.parse(value);
            } catch (Exception e) {
                throw new FilterException("Formato de fecha-hora inválido: " + value + ". Use ISO-8601 (yyyy-MM-ddTHH:mm:ssZ)");
            }
        }
        if (targetType == OffsetDateTime.class) {
            try {
                return OffsetDateTime.parse(value);
            } catch (Exception e) {
                throw new FilterException("Formato de fecha-hora inválido: " + value + ". Use ISO-8601 (yyyy-MM-ddTHH:mm:ss+HH:mm)");
            }
        }
        if (targetType == UUID.class) {
            try {
                return UUID.fromString(value);
            } catch (Exception e) {
                throw new FilterException("UUID inválido: " + value);
            }
        }
        
        return value;
    }

    public static List<Object> convertList(Class<?> targetType, String value) 
    {
        if (value == null || value.isEmpty()) 
        {
            throw new FilterException("El filtro IN requiere al menos un valor");
        }

        String[] parts = value.split(",");
        List<Object> result = new ArrayList<>(parts.length);
        for (String part : parts) 
        {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) 
            {
                throw new FilterException("Valor vacío en filtro IN: " + value);
            }
            result.add(convertValue(targetType, trimmed));
        }
        return result;
    }
}