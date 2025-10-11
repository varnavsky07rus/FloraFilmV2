package com.alaka_ala.florafilm.ui.fragments.changelog;

public class ChangelogItem {
    private String version;
    private String date;
    private String description;

    public ChangelogItem(String version, String date, String description) {
        this.version = version;
        this.date = date;
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
