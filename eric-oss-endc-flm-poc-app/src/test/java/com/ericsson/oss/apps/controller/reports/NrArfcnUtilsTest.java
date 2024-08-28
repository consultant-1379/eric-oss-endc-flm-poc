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
package com.ericsson.oss.apps.controller.reports;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


class NrArfcnUtilsTest {
    @ParameterizedTest
    @MethodSource("getPossibleBandsFeeder")
    void getPossibleBands(Integer arfcn, List<Integer> expectedBands) {
        Assertions.assertEquals(expectedBands, NrArfcnUtils.getPossibleBands(arfcn));
    }

    @Test
    void getFirstBand() {
        Assertions.assertEquals(77, NrArfcnUtils.getFirstBand(620000));
        Assertions.assertThrows(IllegalArgumentException.class, () -> NrArfcnUtils.getFirstBand(3279166));
    }

    @ParameterizedTest
    @MethodSource("nrArfcnToFreqFeeder")
    void nrArfcnToFreq(Integer arfcn, double expected) {
        Assertions.assertEquals(expected, NrArfcnUtils.nrArfcnToFreq(arfcn));
    }

    @Test
    void nrArfcnToFreqException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> NrArfcnUtils.nrArfcnToFreq(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> NrArfcnUtils.nrArfcnToFreq(3279166));
    }

    @ParameterizedTest
    @MethodSource("nrArfcnFreqTypeFeeder")
    void nrArfcnFreqType(Integer arfcn, String expected) {
        Assertions.assertEquals(expected, NrArfcnUtils.nrArfcnFreqType(arfcn));
    }

    @Test
    void nrArfcnFreqTypeException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> NrArfcnUtils.nrArfcnFreqType(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> NrArfcnUtils.nrArfcnFreqType(3279166));
    }

    private static Stream<Arguments> getPossibleBandsFeeder() {
        return Stream.of(
                Arguments.of(143400, List.of(29)),
                Arguments.of(422000, List.of(1, 65, 66)),
                Arguments.of(620000, List.of(77, 78)),
                Arguments.of(2270932, List.of(259, 260)),
                Arguments.of(500, List.of())
        );
    }

    private static Stream<Arguments> nrArfcnToFreqFeeder() {
        return Stream.of(
                Arguments.of(500, 2.5),
                Arguments.of(620000, 3300),
                Arguments.of(2070833, 27500.04)
        );
    }

    private static Stream<Arguments> nrArfcnFreqTypeFeeder() {
        return Stream.of(
                Arguments.of(500, "FR1"),
                Arguments.of(620000, "FR1"),
                Arguments.of(2070833, "FR2")
        );
    }
}
