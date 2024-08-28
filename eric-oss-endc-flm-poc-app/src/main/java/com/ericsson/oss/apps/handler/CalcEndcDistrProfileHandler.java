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
package com.ericsson.oss.apps.handler;

import com.ericsson.oss.apps.execution.ExecutionContext;
import com.ericsson.oss.apps.execution.ExecutionHandler;
import com.ericsson.oss.apps.model.EndcFreqProfileData;
import com.ericsson.oss.apps.service.CalcEndcDistrProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalcEndcDistrProfileHandler implements ExecutionHandler<ExecutionContext> {

    private final CalcEndcDistrProfileService calcEndcDistrProfileService;
    @Value("${rapp-sdk.cellCapacityCalcType.usingCellWeight}")
    private boolean useCellWeight;

    @Override
    public void handle(ExecutionContext context) {

        log.debug("handler that calculate user profile distribution usingCellWeight {}", useCellWeight);
        Map<String, EndcFreqProfileData> eUtranCellToProfileData =
                calcEndcDistrProfileService.processEndcDistrProfile(
                        context.getAllowEutranCells(),
                        context.getEUtranCellToSCGsMap(),
                        useCellWeight,
                        context.getRopTimeStamp());

        context.setEUtranCellToProfileData(eUtranCellToProfileData);
    }

    @Override
    public int getPriority() {
        return 40;
    }
}
