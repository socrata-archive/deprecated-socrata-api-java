package com.socrata.data;

import com.socrata.api.*;
import com.sun.jersey.core.impl.provider.entity.Inflector;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public abstract class Model<T>
{
    public abstract <T extends Model> T create(Connection request) throws RequestException;
    public abstract <T extends Model> T update(Connection request) throws RequestException;
    public abstract void delete(Connection request) throws RequestException;

    static final String base = "https://opendata.socrata.com/api";

    <T> T deserialize(String serializedBody, Class<T> type) throws RequestException
    {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            return mapper.readValue(serializedBody, type);
        }
        catch (JsonMappingException e)
        {
            throw new RequestException("Invalid response does not appear to be a " + type.toString() + ".", e);
        }
        catch (JsonParseException e)
        {
            throw new RequestException("Invalid json in response. Unable to parse.", e);
        }
        catch (IOException e)
        {
            throw new RequestException("There was a problem parsing the response.", e);
        }
    }

    <T extends Model> T results(Response response, Class<T> type) throws RequestException
    {
        switch (response.status)
        {
            case 200:
                return deserialize(response.body, type);
            case 401:
            case 403:
                throw deserialize(response.body, UnauthorizedException.class);
            case 404:
                throw deserialize(response.body, NotFoundException.class);
            default:
                throw deserialize(response.body, RequestException.class);
        }
    }

    void validate(Response response) throws RequestException
    {
        switch (response.status)
        {
            case 200:
            case 202:
                break;
            case 401:
            case 403:
                throw deserialize(response.body, UnauthorizedException.class);
            case 404:
                throw deserialize(response.body, NotFoundException.class);
            default:
                throw deserialize(response.body, RequestException.class);
        }
    }

    String path()
    {
        return "/" + Inflector.getInstance().pluralize(this.getClass().getSimpleName().toLowerCase());
    }

    public <T extends Model> T get(String id, Connection request, Class<T> type) throws RequestException
    {
        Response response = request.get(base + path() + "/" + id);
        return results(response, type);
    }

    public void delete(String id, Connection request) throws RequestException
    {
        validate(request.delete(base + path() + "/" + id));
    }

    public <T extends Model> T update(String id, Connection request, Class<T> type) throws RequestException
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            Response response = request.put(base + path() + "/" + id, mapper.writeValueAsString(this));

            return results(response, type);
        }
        catch (IOException e)
        {
            throw new RequestException("Unexpected IOException while requesting.", e);
        }
    }

    public <T extends Model> T create(Connection request, Class<T> type) throws RequestException
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            Response response = request.post(base + path(), mapper.writeValueAsString(this));

            return results(response, type);
        }
        catch (IOException e)
        {
            throw new RequestException("Unexpected IOException while requesting.", e);
        }
    }
}