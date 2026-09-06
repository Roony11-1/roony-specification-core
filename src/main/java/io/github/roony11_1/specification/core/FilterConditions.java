package io.github.roony11_1.specification.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FilterConditions
{
    private final List<FilterCondition> conditions;

    public FilterConditions()
    {
        this.conditions = new ArrayList<>();
    }

    public FilterConditions(List<FilterCondition> conditions)
    {
        this.conditions = new ArrayList<>(Objects.requireNonNullElseGet(conditions, ArrayList::new));
    }

    public FilterConditions add(FilterCondition condition)
    {
        Objects.requireNonNull(condition, "condition must not be null");
        conditions.add(condition);
        return this;
    }

    public FilterConditions addAll(List<FilterCondition> conditions)
    {
        if (conditions != null)
        {
            this.conditions.addAll(conditions);
        }
        return this;
    }

    public List<FilterCondition> getConditions()
    {
        return Collections.unmodifiableList(conditions);
    }

    public boolean isEmpty()
    {
        return conditions.isEmpty();
    }

    public int size()
    {
        return conditions.size();
    }
}
