package com.alaka_ala.florafilm.ui.util.api.collapse.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class ApiResponse {
    public int getTotal() {
        return total;
    }

    public String getPrevPage() {
        return prevPage;
    }

    public String getNextPage() {
        return nextPage;
    }

    public List<Result> getResults() {
        return results;
    }

    private int total;

    @SerializedName("prev_page")
    private String prevPage;

    @SerializedName("next_page")
    private String nextPage;

    private List<Result> results;

}







