package io.github.roony11_1.specification.core;

public class FilterException extends RuntimeException 
{
    public FilterException(String detail) 
    {
        super("Error en filtro: " + detail);
    }

    public FilterException(String detail, Throwable cause) 
    {
        super("Error en filtro: " + detail, cause);
    }
}