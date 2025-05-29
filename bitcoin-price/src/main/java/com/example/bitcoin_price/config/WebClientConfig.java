package com.example.bitcoin_price.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Configuration
public class WebClientConfig {
    
    
@Value("${coindesk.base-url}")
    private String coindeskBaseUrl;
                                                                                              
    @Bean
    public WebClient coindeskWebClient() {
        return WebClient.builder()
                .baseUrl(coindeskBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    } 
}
