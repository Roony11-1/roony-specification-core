package io.github.roony11_1.specification.core;

public enum FilterOperator 
{
    EQ, NE, LIKE, ILIKE, GT, GTE, LT, LTE, IN, BETWEEN, IS_NULL, IS_NOT_NULL;
    
    public boolean isUnary() 
    {
        return this == IS_NULL || this == IS_NOT_NULL;
    }
    
    public boolean isBinary() 
    {
        return this == BETWEEN;
    }
}