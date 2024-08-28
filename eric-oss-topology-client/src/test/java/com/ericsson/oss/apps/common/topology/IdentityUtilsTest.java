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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IdentityUtilsTest {

    private static final String SUBNETWORK = "SubNetwork";
    private static final String MECONTEXT = "MeContext";

    private static Stream<Arguments> combinationResults() {
        return Stream.of(
                Arguments.of(1, List.of(
                        List.of(SUBNETWORK), List.of(MECONTEXT)
                )),
                Arguments.of(2, List.of(
                        List.of(SUBNETWORK, SUBNETWORK),
                        List.of(SUBNETWORK, MECONTEXT),
                        List.of(MECONTEXT, MECONTEXT)
                )),
                Arguments.of(3, List.of(
                        List.of(SUBNETWORK, SUBNETWORK, SUBNETWORK),
                        List.of(SUBNETWORK, SUBNETWORK, MECONTEXT),
                        List.of(SUBNETWORK, MECONTEXT, MECONTEXT),
                        List.of(MECONTEXT, MECONTEXT, MECONTEXT)
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("combinationResults")
    void generateDnPrefixKeyCombinations(int size, List<List<String>> expected) {
        Assertions.assertEquals(expected, IdentityUtils.generateDnPrefixKeyCombinations(size).collect(Collectors.toList()));
    }
}
