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

import com.ericsson.oss.apps.common.topology.model.CommonTopologyObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CtsRestClient {

    private final WebClient webClient;
    private final String basePath;

    public Flux<CommonTopologyObject> fetchNodes() {
        String baseUrl = String.format("%s/ctw/enodeb", basePath);
        Map<String, String> parameters = getParameters(0L);

        String uri = extendUriTemplate(baseUrl, parameters);

        return fetchAllTopologyObjects(uri, parameters);
    }

    public Flux<CommonTopologyObject> fetchCells(String type) {
        String baseUrl = String.format("%s/ctw/ltecell", basePath);
        Map<String, String> parameters = getCellParameters(type);

        String uri = extendUriTemplate(baseUrl, parameters);

        return fetchAllCellTopologyObjects(uri, parameters);
    }

    private static String extendUriTemplate(String baseUrl, Map<String, String> parameters) {
        StringBuilder builder = new StringBuilder();
        parameters.forEach((k, v) -> {
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(k).append("={")
                    .append(k).append("}");
        });
        return !builder.isEmpty() ? (baseUrl + "?" + builder) : baseUrl;
    }

    private Flux<CommonTopologyObject> fetchAllTopologyObjects(String uri, Map<String, String> parameters) {
        return fetchTopologyObjects(uri, parameters)
                .collectList()
                .flatMapMany(list -> {
                    if (!list.isEmpty()) {
                        CommonTopologyObject last = list.get(list.size() - 1);
                        return Flux.concat(Flux.fromIterable(list), fetchAllTopologyObjects(uri, getParameters(last.getId())));
                    }
                    return Flux.fromIterable(list);
                });
    }

    private Flux<CommonTopologyObject> fetchAllCellTopologyObjects(String uri, Map<String, String> parameters) {
        return fetchTopologyObjects(uri, parameters)
                .collectList()
                .flatMapMany(Flux::fromIterable);
    }

    private static Map<String, String> getParameters(Long id) {
        return Map.of(
                "fs", "attrs",
                "sort", "objectInstId",
                "criteria", String.format("(objectInstId > %dL)", id)
        );
    }

    private static Map<String, String> getCellParameters(String cellType) {
        return Map.of("type", cellType);
    }

    private Flux<CommonTopologyObject> fetchTopologyObjects(String uri, Map<String, ?> parameters) {
        return webClient.get()
                .uri(uri, parameters)
                .retrieve()
                .bodyToFlux(CommonTopologyObject.class);
    }
}
