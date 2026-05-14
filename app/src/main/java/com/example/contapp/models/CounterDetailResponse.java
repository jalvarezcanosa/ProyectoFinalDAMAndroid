package com.example.contapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CounterDetailResponse {
    private int id;
    private String title;
    private String description;

    @SerializedName("image_url")
    private String imageUrl;

    private String status;

    @SerializedName("closed_at")
    private String closedAt;

    @SerializedName("global_count")
    private int globalCount;

    @SerializedName("individual_count")
    private int individualCount;

    @SerializedName("ranking")
    private List<Participant> ranking;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public int getGlobalCount() {
        return globalCount;
    }

    public int getIndividualCount() {
        return individualCount;
    }

    public List<Participant> getRanking() {
        return ranking;
    }
}
