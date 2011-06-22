package com.socrata.api;

/**
 * Resources that return a 404 throw this exception.
 */
public class NotFoundException extends RequestException
{
    public NotFoundException(String s)
    {
        super(s);
    }
}
