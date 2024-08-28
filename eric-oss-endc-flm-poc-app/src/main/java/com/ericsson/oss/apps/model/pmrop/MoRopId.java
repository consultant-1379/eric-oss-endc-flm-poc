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
package com.ericsson.oss.apps.model.pmrop;

import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class MoRopId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1677624110721750769L;

    @Embedded
    private ManagedObjectId objectId;
    private long ropTime;
}
