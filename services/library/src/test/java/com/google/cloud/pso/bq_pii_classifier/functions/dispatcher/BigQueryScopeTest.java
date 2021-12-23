package com.google.cloud.pso.bq_pii_classifier.functions.dispatcher;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;
import static org.junit.Assert.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;

public class BigQueryScopeTest {

        @Test
        public void fromJson() {

            String input = "{\n" +
                    "\"datasetExcludeList\":[],\n" +
                    "\"datasetIncludeList\":[],\n" +
                    "\"projectIncludeList\":[\"Project1\", \"Project2\"],\n" +
                    "\"tableExcludeList\":[],\n" +
                    "\"tableIncludeList\":[]\n" +
                    "}";

            BigQueryScope expected = new BigQueryScope(
                    new ArrayList<>(Arrays.asList("Project1", "Project2")),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>()
            );

            Gson gson = new Gson();
            BigQueryScope actual = gson.fromJson(input, BigQueryScope.class);

            assertEquals(expected, actual);
        }
}
