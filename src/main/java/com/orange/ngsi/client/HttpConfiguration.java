/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.ngsi.client;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.AsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;

/**
 * Configure HTTP connections
 */
@Configuration
public class HttpConfiguration {

    @Value("${httpConfiguration.defaultMaxTotalConnections}")
    private int defaultMaxTotalConnections;

    @Value("${httpConfiguration.defaultMaxConnectionsPerRoute}")
    private int defaultMaxConnectionsPerRoute;

    @Value("${httpConfiguration.defaultReadTimeoutMilliseconds}")
    private int defaultReadTimeoutMilliseconds;

    PoolingNHttpClientConnectionManager connectionManager;

    @Bean
    public AsyncRestTemplate asyncRestTemplate() throws IOReactorException {
        AsyncRestTemplate restTemplate = new AsyncRestTemplate(
                clientHttpRequestFactory());
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        //TODO replace by authent interceptor
        //restTemplate.setInterceptors(Collections.<ClientHttpRequestInterceptor>singletonList(new ServiceNamePathInterceptor()));

        return restTemplate;
    }

    @Bean
    public AsyncClientHttpRequestFactory clientHttpRequestFactory() throws IOReactorException {
        return new HttpComponentsAsyncClientHttpRequestFactory(asyncHttpClient());
    }

    @Bean
    public CloseableHttpAsyncClient asyncHttpClient() throws IOReactorException {

            connectionManager = new PoolingNHttpClientConnectionManager(
                    new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT));
            connectionManager.setMaxTotal(defaultMaxTotalConnections);
            connectionManager.setDefaultMaxPerRoute(defaultMaxConnectionsPerRoute);

            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(defaultReadTimeoutMilliseconds)
                    .setSocketTimeout(defaultReadTimeoutMilliseconds)
                    .setConnectionRequestTimeout(defaultReadTimeoutMilliseconds)
                    .build();

            CloseableHttpAsyncClient httpclient = HttpAsyncClientBuilder
                    .create().setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(config).build();
            return httpclient;
    }
}
