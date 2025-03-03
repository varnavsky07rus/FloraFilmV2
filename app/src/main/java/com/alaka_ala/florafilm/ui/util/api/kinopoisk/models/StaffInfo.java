package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

import java.io.Serializable;
import java.util.ArrayList;

/** Подробная информация об актёре */
public class StaffInfo implements Serializable {
    private final int personId;
    private final String webUrl;
    private final String nameRu;
    private final String nameEn;
    private final String sex;
    private final String posterUrl;
    private final int growth;
    private final String birthday;
    private final String death;
    private final int age;
    private final String birthplace;
    private final String deathplace;
    private final ArrayList<StaffSpouseItem> spouses;
    private final int hasAwards;
    private final String profession;
    private final ArrayList<String> facts;
    private final ArrayList<StaffFilmsItem> films;



    public ArrayList<StaffFilmsItem> getFilms() {
        return films;
    }

    public ArrayList<String> getFacts() {
        return facts;
    }

    public String getProfession() {
        return profession;
    }

    public int getHasAwards() {
        return hasAwards;
    }

    public ArrayList<StaffSpouseItem> getSpouses() {
        return spouses;
    }

    public String getDeathplace() {
        return deathplace;
    }

    public String getBirthplace() {
        return birthplace;
    }

    public int getAge() {
        return age;
    }

    public String getDeath() {
        return death;
    }

    public String getBirthday() {
        return birthday;
    }

    public int getGrowth() {
        return growth;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getSex() {
        return sex;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameRu() {
        return nameRu;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public int getPersonId() {
        return personId;
    }



    public StaffInfo(int personId, String webUrl, String nameRu, String nameEn, String sex, String posterUrl, int growth, String birthday, String death, int age, String birthplace, String deathplace, ArrayList<StaffSpouseItem> spouses, int hasAwards, String profession, ArrayList<String> facts, ArrayList<StaffFilmsItem> films) {
        this.personId = personId;
        this.webUrl = webUrl;
        this.nameRu = nameRu;
        this.nameEn = nameEn;
        this.sex = sex;
        this.posterUrl = posterUrl;
        this.growth = growth;
        this.birthday = birthday;
        this.death = death;
        this.age = age;
        this.birthplace = birthplace;
        this.deathplace = deathplace;
        this.spouses = spouses;
        this.hasAwards = hasAwards;
        this.profession = profession;
        this.facts = facts;
        this.films = films;
    }



}
