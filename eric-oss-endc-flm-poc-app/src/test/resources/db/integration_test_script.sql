--
-- COPYRIGHT Ericsson 2024
--
--
--
-- The copyright to the computer program(s) herein is the property of
--
-- Ericsson Inc. The programs may be used and/or copied only with written
--
-- permission from Ericsson Inc. or in accordance with the terms and
--
-- conditions stipulated in the agreement/contract under which the
--
-- program(s) have been supplied.
--

-- Create gnodeb table
CREATE TABLE GNODEB (
	DTYPE CHARACTER VARYING(50) NOT NULL,
	ME_FDN CHARACTER VARYING(255) NOT NULL,
	REF_ID CHARACTER VARYING(255) NOT NULL,
	PARENT_REF_ID CHARACTER VARYING(255),
	GNBID BIGINT,
	GNBID_LENGTH INTEGER,
	MCC INTEGER,
	MNC INTEGER,
	PRIMARY KEY (ME_FDN,REF_ID)
);
