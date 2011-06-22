package com.socrata.api;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;

/**
 * The connection is the interface through which basic REST operations happen
 * against the socrata API.
 */
public interface Connection
{
    Response post(String url, String body);

    Response post(String url, MultivaluedMap<String, String> params);

    Response post(String url, MultivaluedMap<String, String> params, String body);

    Response post(String url, MultivaluedMap<String, String> params, File file);

    Response get(String url);

    Response get(String url, MultivaluedMap<String, String> params);

    Response put(String url, String body);

    Response put(String url, MultivaluedMap<String, String> params, String body);

    Response delete(String url);
}
