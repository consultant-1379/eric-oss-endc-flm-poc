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

import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class IdentityUtils {

    private static final List<String> DN_PREFIX_KEYS = List.of("SubNetwork", "MeContext");

    public static String buildFdn(List<String> keys, List<String> values) {
        Iterator<String> valueIterator = values.iterator();
        return keys.stream()
                .map(key -> String.format("%s=%s", key, valueIterator.next()))
                .collect(Collectors.joining(","));
    }

    public static Stream<List<String>> generateDnPrefixKeyCombinations(int count) {
        return generateCombinations(DN_PREFIX_KEYS, count, new LinkedList<>());
    }

    private static Stream<List<String>> generateCombinations(List<String> elements, int count, LinkedList<String> previousCombination) {
        if (previousCombination.size() >= count) {
            return Stream.of(previousCombination);
        }
        return elements.stream()
                .filter(element -> previousCombination.isEmpty() || elements.indexOf(previousCombination.getLast()) <= elements.indexOf(element))
                .flatMap(element -> {
                    LinkedList<String> currentCombination = new LinkedList<>(previousCombination);
                    currentCombination.add(element);
                    return generateCombinations(elements, count, currentCombination);
                });
    }
}
