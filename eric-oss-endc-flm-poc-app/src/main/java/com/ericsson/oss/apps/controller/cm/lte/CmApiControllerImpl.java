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
package com.ericsson.oss.apps.controller.cm.lte;

import com.ericsson.oss.apps.api.controller.CmApi;
import com.ericsson.oss.apps.api.model.AllowList;
import com.ericsson.oss.apps.service.CmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CmApiControllerImpl implements CmApi {

    private final CmService cmService;
    private final Pattern cellFDNPattern = Pattern.compile("^(SubNetwork=[\\-\\w]+,)+(MeContext=[\\-\\w]+,)?ManagedElement=[\\-\\w]+,ENodeBFunction=[\\-\\w]+,((EUtranCellFDD=[\\-\\w]+)|(EUtranCellTDD=[\\-\\w]+))$");
    private final Pattern nodeFDNPattern = Pattern.compile("^(SubNetwork=[\\-\\w]+,)+(MeContext=[\\-\\w]+,)?ManagedElement=[\\-\\w]+,ENodeBFunction=[\\-\\w]+$");

    @Override
    public ResponseEntity<AllowList> getLTEAllowList() {
        log.info("Retrieving LTE Allow List");
        return new ResponseEntity<>(cmService.getAllowList(), HttpStatusCode.valueOf(200));
    }

    @Override
    public ResponseEntity<AllowList> putLTEAllowList(AllowList allowList) {
        log.info("Updating LTE Allow List");
        AllowList invalidFDNs = new AllowList();

        invalidFDNs.setEutranCells(allowList.getEutranCells().stream()
                .filter(eutranCell -> !cellFDNPattern.matcher(eutranCell.getFdn()).matches())
                .collect(Collectors.toList()));

        invalidFDNs.setEnodebs(allowList.getEnodebs().stream()
                .filter(eNodeb -> !nodeFDNPattern.matcher(eNodeb.getFdn()).matches())
                .collect(Collectors.toList()));

        if (invalidFDNs.getEnodebs().isEmpty() && invalidFDNs.getEutranCells().isEmpty()) {
            return new ResponseEntity<>(cmService.saveAllowList(allowList), HttpStatusCode.valueOf(200));
        }
        return new ResponseEntity<>(invalidFDNs, HttpStatusCode.valueOf(400));
    }
}
