/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.apps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Core Application, the starting point of the application.
 */
@SpringBootApplication
@EnableScheduling
public class CoreApplication {
    /**
     * Main entry point of the application.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

    /**
     * Configuration bean for Web MVC.
     *
     * @return WebMvcConfigurer
     */
    @Bean
    public WebMvcConfigurer webConfigurer() {
        return new WebMvcConfigurer() {
        };
    }

    /**
     * Making a WebClient, using the WebClient.Builder, to use for consumption of RESTful interfaces.
     *
     * @return WebClient
     */
    @Bean
    @ConditionalOnMissingBean(WebClient.class)
    public WebClient webClient(final WebClient.Builder webClientBuilder,
                               ClientHttpConnector clientHttpConnector,
                               List<ExchangeFilterFunction> filters
    ) {
        return webClientBuilder
                .clientConnector(clientHttpConnector)
                .filters(webClientFilters -> webClientFilters.addAll(filters))
                .build();
    }
}
