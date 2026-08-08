package io.github.roony11_1.specification.core;

import java.util.List;

public class FilterParser 
{
    public static void parseAndAdd(String field, String value, List<FilterCondition> conditions) 
    {
        if (value == null || value.isEmpty()) 
            return;
        
        // Operador explícito (formato: operador|valor1|valor2)
        if (value.contains("|")) 
        {
            String[] parts = value.split("\\|", -1);
            FilterOperator operator;
            try 
            {
                operator = FilterOperator.valueOf(parts[0].toUpperCase());
            } 
            catch (IllegalArgumentException e) 
            {
                throw new FilterException("Operador inválido: " + parts[0]);
            }

            if (operator.isUnary()) 
            {
                conditions.add(new FilterCondition(field, operator, null, null));
                return;
            }

            String val1 = parts.length > 1 ? parts[1] : null;
            String val2 = parts.length > 2 ? parts[2] : null;

            if (operator.isBinary()) 
            {
                if (val1 == null || val1.isEmpty() || val2 == null || val2.isEmpty()) 
                {
                    throw new FilterException("El operador " + operator + " requiere dos valores: " + value);
                }
            }
            else if (val1 == null || val1.isEmpty()) 
            {
                throw new FilterException("El operador " + operator + " requiere un valor: " + value);
            }

            conditions.add(new FilterCondition(field, operator, val1, val2));
            return;
        }
        
        // Formato "in:valor1,valor2,valor3"
        if (value.startsWith("in:") || value.startsWith("IN:")) 
        {
            String values = value.substring(3);
            if (values.isEmpty()) 
            {
                throw new FilterException("El operador IN requiere al menos un valor: " + value);
            }
            conditions.add(new FilterCondition(field, FilterOperator.IN, values));
            return;
        }
        
        // Formato "between:valor1,valor2"
        if (value.startsWith("between:") || value.startsWith("BETWEEN:")) 
        {
            String values = value.substring(8);
            String[] parts = values.split(",");
            if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) 
            {
                throw new FilterException("El operador BETWEEN requiere dos valores separados por coma: " + value);
            }
            conditions.add(new FilterCondition(field, FilterOperator.BETWEEN, parts[0], parts[1]));
            return;
        }
        
        // Filtro simple por igualdad
        conditions.add(new FilterCondition(field, FilterOperator.EQ, value));
    }
}