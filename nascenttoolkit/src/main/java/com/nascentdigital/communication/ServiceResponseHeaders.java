package com.nascentdigital.communication;

import java.util.List;
import java.util.Map;

/**
 * Created by andyrohan on 2016-07-12.
 */
public class ServiceResponseHeaders {
    public Map<String, List<String>> headers;

    public ServiceResponseHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }
}
