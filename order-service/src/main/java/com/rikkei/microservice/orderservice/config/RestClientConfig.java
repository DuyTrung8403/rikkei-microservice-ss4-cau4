package com.rikkei.microservice.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    URI uri = request.getURI();
                    if ("ProductService".equalsIgnoreCase(uri.getHost())) {
                        try {
                            URI newUri = new URI(
                                    uri.getScheme(),
                                    uri.getUserInfo(),
                                    "product-service",
                                    uri.getPort(),
                                    uri.getPath(),
                                    uri.getQuery(),
                                    uri.getFragment()
                            );
                            HttpRequest modifiedRequest = new HttpRequestWrapper(request) {
                                @Override
                                public URI getURI() {
                                    return newUri;
                                }
                            };
                            return execution.execute(modifiedRequest, body);
                        } catch (URISyntaxException e) {
                            return execution.execute(request, body);
                        }
                    }
                    return execution.execute(request, body);
                });
    }

    @Bean
    public RestClient restClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.build();
    }
}
