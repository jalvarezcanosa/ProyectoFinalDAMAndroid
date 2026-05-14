package com.example.contapp.models;

import com.google.gson.annotations.SerializedName;
public class Counter {
    private int id;
    private String title;
    private String description;

    @SerializedName("image_url")
    private String imageUrl;

    private String status;

    @SerializedName("closed_at")
    private String closedAt;

    @SerializedName("individual_count")
    private int individualCount;

    @SerializedName("global_count")
    private int globalCount;

    @SerializedName("invite_code")
    private String inviteCode;

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

    public int getIndividualCount() {
        return individualCount;
    }

    public int getGlobalCount() {
        return globalCount;
    }

    public String getInviteCode() {
        return inviteCode;
    }
}
