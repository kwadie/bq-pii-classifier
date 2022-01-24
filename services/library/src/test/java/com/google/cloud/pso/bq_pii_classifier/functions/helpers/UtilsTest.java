package com.google.cloud.pso.bq_pii_classifier.functions.helpers;

import com.google.cloud.pso.bq_pii_classifier.helpers.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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