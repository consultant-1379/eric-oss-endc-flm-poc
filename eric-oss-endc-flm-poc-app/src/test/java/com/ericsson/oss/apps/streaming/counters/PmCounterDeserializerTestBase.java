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
package com.ericsson.oss.apps.streaming.counters;

public abstract class PmCounterDeserializerTestBase {
    static final String FDN = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR03gNodeBRadio00002,ManagedElement=NR03gNodeBRadio00002";
    final long ROP_BEGIN_TIME_IN_EPOC = 1674816660000L;
    final long ROP_END_TIME_IN_EPOC = 1674817560000L;
    final String ROP_BEGIN_TIME = "2023-01-27T10:51:00Z";
    final String ROP_END_TIME = "2023-01-27T11:06:00Z";
}
