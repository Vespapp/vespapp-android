package com.habitissimo.vespapp.questions;

import java.io.Serializable;

/**
 * Created by archi on 11/03/16.
 */
public class Answer implements Serializable {

    private int id;
    private String value;

    private String value_ca;

    /* Para futuras actualizaciones de idioma */
//    private String value_en;
//    private String value_de;


    public Answer(int id, String value, String value_ca /*, String value_en, String value_de */) {
        this.id = id;
        this.value = value;

        this.value_ca = value_ca;

        /* Para futuras actualizaciones de idioma */
//        this.value_en = value_en;
//        this.value_de = value_de;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getValue_ca() {
        return value_ca;
    }

    /* Descomentar para futuras actualizaciones de idioma */
//    public String getValue_en() {
//        return value_en;
//    }
//
//    public String getValue_de() {
//        return value_de;
//    }
}
