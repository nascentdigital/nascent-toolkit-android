package com.nascentdigital.communication;

import javax.net.ssl.SSLContext;


public interface SSLContextFactory {
    SSLContext makeContext(String allowedHost) throws Exception;
}
