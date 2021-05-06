package com.example.myapplication.database;

import java.io.Serializable;

public class EquipmentBean implements Serializable {

    private String name;
    private String id;
    private String picture;
    private String region;

    public EquipmentBean() {
    }

    public EquipmentBean(String name, String id, String picture, String region) {
        this.name = name;
        this.id = id;
        this.picture = picture;
        this.region = region;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
