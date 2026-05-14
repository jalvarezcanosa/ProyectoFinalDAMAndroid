package com.example.contapp.models;

import com.google.gson.annotations.SerializedName;
public class Participant {
    private String username;

    @SerializedName("total_clicks")
    private int totalClicks;

    public String getUsername() {
        return username;
    }

    public int getTotalClicks() {
        return totalClicks;
    }
}
