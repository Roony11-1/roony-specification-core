package io.github.roony11_1.specification.core;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterPredicateBuilder 
{
    private final List<FilterCondition> conditions = new ArrayList<>();
    private Map<String, String> aliases = Map.of();
    private static final Set<String> EXCLUDED_PARAMS = Set.of("page", "size", "sort");
    
    public FilterPredicateBuilder withCondition(FilterCondition condition) 
    {
        conditions.add(condition);
        return this;
    }
    
    public FilterPredicateBuilder withConditions(List<FilterCondition> conditions) 
    {
        this.conditions.addAll(conditions);
        return this;
    }
    
    public FilterPredicateBuilder withAliases(Map<String, String> aliases) 
    {
        if (aliases != null) 
        {
            this.aliases = aliases;
        }
        return this;
    }
    
    public FilterPredicateBuilder withConditions(Map<String, String> filters) 
    {
        if (filters != null) 
        {
            for (Map.Entry<String, String> entry : filters.entrySet()) 
            {
                if (!EXCLUDED_PARAMS.contains(entry.getKey())) 
                {
                    String field = aliases.getOrDefault(entry.getKey(), entry.getKey());
                    FilterParser.parseAndAdd(field, entry.getValue(), conditions);
                }
            }
        }
        return this;
    }
    
    public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) 
    {
        if (conditions.isEmpty()) 
        {
            return cb.conjunction();
        }
        
        List<Predicate> predicates = new ArrayList<>();
        
        for (FilterCondition condition : conditions) 
        {
            try 
            {
                Path<?> path = resolvePath(root, condition.getField());
                predicates.add(buildPredicate(cb, path, condition));
            } 
            catch (FilterException e) 
            {
                throw e;
            }
            catch (Exception e) 
            {
                throw new FilterException("Error procesando filtro para el campo: " + condition.getField(), e);
            }
        }
        
        return cb.and(predicates.toArray(new Predicate[0]));
    }
    
    private Predicate buildPredicate(CriteriaBuilder cb, Path<?> path, FilterCondition condition) 
    {
        FilterOperator operator = condition.getOperator();
        Class<?> javaType = path.getJavaType();

        switch (operator) 
        {
            case IS_NULL:
                return cb.isNull(path);
            case IS_NOT_NULL:
                return cb.isNotNull(path);
            case IN:
                return path.in(ValueConverter.convertList(javaType, 
                    condition.getValue() != null ? condition.getValue().toString() : null));
            case BETWEEN:
                return cb.between(path.as(Comparable.class), 
                    toComparable(javaType, condition.getValue()), 
                    toComparable(javaType, condition.getValue2()));
            case LIKE:
                return cb.like(path.as(String.class), 
                    "%" + (condition.getValue() != null ? condition.getValue() : "") + "%");
            case ILIKE:
                return cb.like(cb.lower(path.as(String.class)), 
                    "%" + (condition.getValue() != null ? condition.getValue().toString().toLowerCase() : "") + "%");
            default:
                Object value = ValueConverter.convertValue(javaType,
                    condition.getValue() != null ? condition.getValue().toString() : null);
                return buildComparison(cb, path, operator, value);
        }
    }
    
    private Predicate buildComparison(CriteriaBuilder cb, Path<?> path, FilterOperator operator, Object value) 
    {
        switch (operator) 
        {
            case EQ:
                return cb.equal(path, value);
            case NE:
                return cb.notEqual(path, value);
            case GT:
                return cb.greaterThan(path.as(Comparable.class), (Comparable) value);
            case GTE:
                return cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case LT:
                return cb.lessThan(path.as(Comparable.class), (Comparable) value);
            case LTE:
                return cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            default:
                throw new FilterException("Operador no soportado: " + operator);
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Comparable toComparable(Class<?> javaType, Object value) 
    {
        return (Comparable) ValueConverter.convertValue(javaType,
            value != null ? value.toString() : null);
    }
    
    private Path<?> resolvePath(Path<?> root, String field) 
    {
        String[] parts = field.split("\\.");
        Path<?> path = root;
        for (String part : parts) 
        {
            path = path.get(part);
        }
        return path;
    }
}