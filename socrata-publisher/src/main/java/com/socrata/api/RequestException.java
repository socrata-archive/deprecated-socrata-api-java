package com.socrata.api;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class RequestException extends Exception
{
    String code;
    boolean error;

    public RequestException()
    {
        super();
    }

    public RequestException(String s)
    {
        super(s);
    }

    public RequestException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public RequestException(Throwable throwable)
    {
        super(throwable);
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public boolean isError()
    {
        return error;
    }

    public void setError(boolean error)
    {
        this.error = error;
    }
}
