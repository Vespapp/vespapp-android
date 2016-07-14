package com.habitissimo.vespapp.sighting;

import java.io.Serializable;

public class Location implements Serializable {

    private int id;
    private String name;
    private String name_ca;

    /* Para futuras actualizaciones */
//    private String name_en;
//    private String name_de;

    private float lat;
    private float lng;
    private String created_at;
    private String updated_at;
    private int province;

    public Location(int id, String name, String name_ca, /*String name_en, String name_de,*/ float lat, float lng,
                    String created_at, String updated_at, int province) {
        this.id = id;
        this.name = name;
        this.name_ca = name_ca;

        /* Para futuras actualizaciones de idioma */
//        this.name_en = name_en;
//        this.name_de = name_de;

        this.lat = lat;
        this.lng = lng;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.province = province;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getName_ca() {
        return name_ca;
    }

    /* Desecomentar para futuras actualizaciones de idioma */
//    public String getName_en() {
//        return name_en;
//    }
//
//    public String getName_de() {
//        return name_de;
//    }

    public void setName(String name) {
        this.name = name;
    }

    public void setName_ca(String name_ca) {
        this.name_ca = name_ca;
    }

    /* Desecomentar para futuras actualizaciones de idioma */
//    public void setName_en(String name_en) {
//        this.name_en = name_en;
//    }
//
//    public void setName_de(String name_de) {
//        this.name_de = name_de;
//    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int getProvince() {
        return province;
    }

    public void setProvince(int province) {
        this.province = province;
    }
}
