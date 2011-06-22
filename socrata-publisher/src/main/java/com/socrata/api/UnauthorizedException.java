package com.socrata.api;

/**
 * Resources that return either a 403 or 401 HTTP response code raise this
 * exception.
 */
public class UnauthorizedException extends RequestException
{
    public UnauthorizedException(String s)
    {
        super(s);
    }
}
