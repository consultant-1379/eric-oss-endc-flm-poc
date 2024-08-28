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
package com.ericsson.oss.apps.common.topology;

import com.ericsson.oss.apps.ncmp.util.CmHandleResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnProperty(prefix = "rapp-sdk.topology", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CommonTopologyConfiguration {

    @Value("${rapp-sdk.topology.base-path:${BASE_URL:http://localhost}/oss-core-ws/rest}")
    private String basePath;

    @Bean
    public CtsRestClient ctsRestClient(WebClient webClient) {
        return new CtsRestClient(webClient, basePath);
    }

    @Bean
    public CommonTopologyService networkTopologyService(CtsRestClient ctsRestClient, CmHandleResolver cmHandleResolver) {
        return new CommonTopologyService(ctsRestClient, cmHandleResolver);
    }
}
