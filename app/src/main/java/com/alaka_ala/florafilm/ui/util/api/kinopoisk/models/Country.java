package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

import java.io.Serializable;

public class Country implements Serializable {

    public Country(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    private final String country;

}
