package com.google.cloud.pso.bq_security_classifier.helpers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Map;
import java.util.SortedMap;

public class TableScanLimitsConfig {

    private TableScanLimitsType scanLimitsType;
    private SortedMap<Integer, Integer> limitsIntervals;

    /**
     * Expects a String in the format of "{\"limitType\": \"NUMBER_OF_ROWS\", \"limits\": {\"5000\": \"500\",\"1000\": \"100\", \"2000\": \"200\"}}"
     * Where limitType = NUMBER_OF_ROWS | PERCENTAGE_OF_ROWS
     * And limits =  "max table size": "number of rows"
     * @param jsonString
     */
    public TableScanLimitsConfig(String jsonString){

        JsonElement root = JsonParser.parseString(jsonString).getAsJsonObject();
        String limitType = root.getAsJsonObject().get("limitType").getAsString();
        JsonElement limits = root.getAsJsonObject().get("limits").getAsJsonObject();
        Gson gson = new Gson();
        Type mapType = new TypeToken<SortedMap<Integer, Integer>>() {}.getType();

        this.scanLimitsType = TableScanLimitsType.valueOf(limitType);
        this.limitsIntervals = gson.fromJson(limits, mapType);
    }

    public Integer getTableScanLimitBasedOnNumRows (Integer numRows){

        // loop on the sorted intervals and return the value for the right interval bracket
        for(Integer IntervalEnd: limitsIntervals.keySet()){
            if (numRows <= IntervalEnd){
                return  limitsIntervals.get(IntervalEnd);
            }
        }
        // if no interval found return the value of the highest bracket
        return limitsIntervals.get(limitsIntervals.lastKey());
    }

    public TableScanLimitsType getScanLimitsType() {
        return scanLimitsType;
    }

    @Override
    public String toString() {
        return "TableScanLimitsConfig{" +
                "scanLimitsType=" + scanLimitsType +
                ", limitsIntervals=" + limitsIntervals +
                '}';
    }
}
