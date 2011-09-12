package com.socrata.data;

import com.socrata.api.*;
import com.sun.jersey.core.impl.provider.entity.Inflector;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public abstract class Model<T>
{
    public abstract <T extends Model> T create(Connection request) throws RequestException;
    public abstract <T extends Model> T update(Connection request) throws RequestException;
    public abstract void delete(Connection request) throws RequestException;

    static final String base = "https://opendata.socrata.com/api";
    static final long ticketCheck = 10000L;

    <T> T deserialize(String serializedBody, boolean intoSelf, Class<T> type) throws RequestException
    {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            if (intoSelf)
            {
                mapper.updatingReader(this).readValue(serializedBody);
                return (T) this;
            }
            else
            {
                return mapper.readValue(serializedBody, type);
            }
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

    <T> List<T> deserializeList(String serializedBody, final Class<T> type) throws RequestException
    {
        ObjectMapper mapper = new ObjectMapper();

        final Type listType = new ParameterizedType()
        {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { type };
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };

        try
        {
            // HACK: note that the TypeReference here doesn't actually care what type
            // you give it; we override it in getType() immediately to deal with Javaness.
            return mapper.readValue(serializedBody, new TypeReference<Object>()
            {
                @Override
                public Type getType()
                {
                    return listType;
                }
            });
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

    <T extends Model> T result(Response response, boolean intoSelf, Class<T> type) throws RequestException
    {
        switch (response.status)
        {
            case 200:
                return deserialize(response.body, intoSelf, type);
            case 401:
            case 403:
                throw deserialize(response.body, false, UnauthorizedException.class);
            case 404:
                throw deserialize(response.body, false, NotFoundException.class);
            default:
                throw deserialize(response.body, false, RequestException.class);
        }
    }

    <T extends Model> List<T> results (Response response, Class<T> type) throws RequestException
    {
        switch (response.status)
        {
            case 200:
                return deserializeList(response.body, type);
            case 401:
            case 403:
                throw deserialize(response.body, false, UnauthorizedException.class);
            case 404:
                throw deserialize(response.body, false, NotFoundException.class);
            default:
                throw deserialize(response.body, false, RequestException.class);
        }
    }

    void verifyResponseCode(Response response) throws RequestException
    {
        switch (response.status)
        {
            case 200:
            case 202:
                break;
            case 401:
            case 403:
                throw deserialize(response.body, false, UnauthorizedException.class);
            case 404:
                throw deserialize(response.body, false, NotFoundException.class);
            default:
                throw deserialize(response.body, false, RequestException.class);
        }
    }

    String path()
    {
        return "/" + Inflector.getInstance().pluralize(this.getClass().getSimpleName().toLowerCase());
    }

    public <T extends Model> T get(String id, Connection request, Class<T> type) throws RequestException
    {
        Response response = request.get(base + path() + "/" + id);
        return result(response, false, type);
    }

    public void delete(String id, Connection request) throws RequestException
    {
        verifyResponseCode(request.delete(base + path() + "/" + id));
    }

    public <T extends Model> T update(String id, Connection request, Class<T> type) throws RequestException
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            Response response = request.put(base + path() + "/" + id, mapper.writeValueAsString(this));

            return result(response, false, type);
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

            return result(response, false, type);
        }
        catch (IOException e)
        {
            throw new RequestException("Unexpected IOException while requesting.", e);
        }
    }
}