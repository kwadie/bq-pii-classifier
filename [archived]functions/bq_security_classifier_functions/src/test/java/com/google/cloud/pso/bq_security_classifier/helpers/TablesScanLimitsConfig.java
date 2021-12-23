package com.google.cloud.pso.bq_security_classifier.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.math.BigInteger;
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;

public class TablesScanLimitsConfig {

    @Test
    public void testTableScanLimitBasedOnNumRows(){
        String json = "{\"limitType\": \"NUMBER_OF_ROWS\", \"limits\": {\"5000\": \"500\",\"1000\": \"100\", \"2000\": \"200\"}}";

        TableScanLimitsConfig config = new TableScanLimitsConfig(json);

        // Test lowest bucket
        assertEquals(java.util.Optional.of(100),
                java.util.Optional.of(config.getTableScanLimitBasedOnNumRows(700)));

        // Test edge of bucket
        assertEquals(java.util.Optional.of(100),
                java.util.Optional.of(config.getTableScanLimitBasedOnNumRows(1000)));

        // Test middle buckets
        assertEquals(java.util.Optional.of(200),
                java.util.Optional.of(config.getTableScanLimitBasedOnNumRows(1500)));

        // Test higher than last bucket
        assertEquals(java.util.Optional.of(500),
                java.util.Optional.of(config.getTableScanLimitBasedOnNumRows(7000)));
    }
}