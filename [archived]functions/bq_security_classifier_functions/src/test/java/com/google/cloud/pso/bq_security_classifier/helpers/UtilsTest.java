package com.google.cloud.pso.bq_security_classifier.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;
import static org.junit.Assert.*;
import com.google.cloud.pso.bq_security_classifier.helpers.Utils;

import java.util.Map;
import java.util.SortedMap;

public class UtilsTest {

    @Test
    public void extractTaxonomyIdFromPolicyTagId() {

        String input = "projects/<project>/locations/<location>/taxonomies/<taxonomyID>/policyTags/<policyTagID";
        String expected = "projects/<project>/locations/<location>/taxonomies/<taxonomyID>";
        String actual = Utils.extractTaxonomyIdFromPolicyTagId(input);

        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConfigFromEnv_Required() {
        Utils.getConfigFromEnv("NA_VAR", true);
    }

    @Test
    public void getConfigFromEnv_NotRequired() {
        // should not fail because the VAR is not required
        Utils.getConfigFromEnv("NA_VAR", false);
    }
}