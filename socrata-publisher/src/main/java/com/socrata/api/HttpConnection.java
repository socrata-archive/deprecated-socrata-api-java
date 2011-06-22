package com.socrata.api;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import sun.misc.BASE64Encoder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;

/**
 * This is the standard connection through which the socrata API is accessed.
 * If you're only interested in using the provided models, you can simply create
 * a single connection and pass it around to the business logic methods.
 *
 * If you're interested in extending or implementing some other models or
 * business logic, you should be able to use this connection on RESTful socrata
 * resources without worrying about authentication or setting your domain or
 * response type, etc.
 */
public class HttpConnection implements Connection
{
    String domain;
    String user;
    String password;
    String apptoken;

    /**
     *
     * @param domain The CNAME of the domain
     * @param user The user name (email) of the user you're authenticating as
     * @param password The password for the user.
     * @param apptoken The apptoken for your application.
     */
    public HttpConnection(String domain, String user, String password, String apptoken)
    {
        this.domain = domain;
        this.user = user;
        this.password = password;
        this.apptoken = apptoken;
    }

    public WebResource.Builder resource(String url, MultivaluedMap<String, String> params)
    {
        Client client = Client.create();
        WebResource r = client.resource(url).queryParams(params);
        return r.
                accept("application/json").
                header("X-Socrata-Host", domain).
                header("Authorization", "Basic " + new BASE64Encoder().encode((user + ":" + password).getBytes())).
                header("X-App-Token", apptoken);
    }

    @Override
    public Response post(String url, String body)
    {
        return post(url, new MultivaluedMapImpl(), body);
    }

    @Override
    public Response post(String url, MultivaluedMap<String, String> params)
    {
        ClientResponse response = resource(url, new MultivaluedMapImpl()).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept("text/plain").post(ClientResponse.class, params);
        return new Response(response.getStatus(), response.getEntity(String.class));
    }

    @Override
    public Response post(String url, MultivaluedMap<String, String> params, String body)
    {
        ClientResponse response = resource(url, params).entity(body).post(ClientResponse.class);
        return new Response(response.getStatus(), response.getEntity(String.class));
    }

    @Override
    public Response post(String url, MultivaluedMap<String, String> params, File file)
    {
        FormDataMultiPart form = new FormDataMultiPart();
        form.bodyPart(new FileDataBodyPart(file.getName(), file));
        ClientResponse response = resource(url, params).type(MediaType.MULTIPART_FORM_DATA_TYPE).accept("text/plain").post(ClientResponse.class, form);
        return new Response(response.getStatus(), response.getEntity(String.class));
    }

    @Override
    public Response get(String url)
    {
        return get(url, new MultivaluedMapImpl());
    }

    @Override
    public Response get(String url, MultivaluedMap<String, String> params)
    {
        ClientResponse response = resource(url, params).get(ClientResponse.class);
        return new Response(response.getStatus(), response.getEntity(String.class));
    }

    @Override
    public Response put(String url, String body)
    {
        return put(url, new MultivaluedMapImpl(), body);
    }

    @Override
    public Response put(String url, MultivaluedMap<String, String> params, String body)
    {
        ClientResponse response = resource(url, params).entity(body).put(ClientResponse.class);
        return new Response(response.getStatus(), response.getEntity(String.class));
    }

    @Override
    public Response delete(String url)
    {
        ClientResponse response = resource(url, new MultivaluedMapImpl()).delete(ClientResponse.class);
        return new Response(response.getStatus(), response.getEntity(String.class));
    }
}
