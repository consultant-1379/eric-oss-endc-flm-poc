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

package com.ericsson.oss.apps.db;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@ActiveProfiles("postgres")
@SpringBootTest
public class PostgresQLIntegrationTest {

    @Container
    private static final JdbcDatabaseContainer postgreSQL = new PostgisContainerProvider()
            .newInstance()
            .withInitScript("db/integration_test_script.sql");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQL::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQL::getPassword);
        registry.add("spring.datasource.username", postgreSQL::getUsername);
    }

    @Test
    @Order(1)
    public void testDatabaseConnection() {
        // Execute a query to test DB connection
        int result = jdbcTemplate.queryForObject("SELECT 42", Integer.class);
        assertEquals(42, result);
    }

    @Test
    @Order(2)
    public void testQuery() {
        // Insert a gnodeb into the database
        jdbcTemplate.update("INSERT INTO GNODEB (DTYPE,ME_FDN,REF_ID,PARENT_REF_ID,GNBID,GNBID_LENGTH,MCC,MNC) VALUES\n" +
                "\t ('ExternalGNBCUCPFunction','SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR122gNodeBRadio00001,ManagedElement=NR122gNodeBRadio00001','GNBCUCPFunction=1,NRNetwork=1,ExternalGNBCUCPFunction=NR115gNodeBRadio00012','GNBCUCPFunction=1,NRNetwork=1',4493,22,128,49);\n");

        // Perform a query
        String query = "SELECT GNBID, MCC, MNC FROM GNODEB WHERE ME_FDN LIKE 'SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR122gNodeBRadio00001,ManagedElement=NR122gNodeBRadio00001'; ";
        jdbcTemplate.query(query, rs -> {
            int gnb_id = rs.getInt(1);
            int mcc = rs.getInt(2);
            int mnc = rs.getInt(3);

            assertEquals(4493, gnb_id);
            assertEquals(128, mcc);
            assertEquals(49, mnc);
        });
    }

}
