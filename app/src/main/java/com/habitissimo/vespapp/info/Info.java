package com.habitissimo.vespapp.info;

import java.io.Serializable;

/**
 * Created by joan on 4/05/16.
 */
public class Info implements Serializable {

    private String title;
    private String body;
    private String image;

    private String title_ca;
    private String body_ca;
    private String image_ca;

    /* Para futuras actualizaciones de idioma */
//    private String title_en;
//    private String body_en;
//    private String image_en;
//
//    private String title_de;
//    private String body_de;
//    private String image_de;

    public Info(String title, String body, String image, String title_ca, String body_ca, String image_ca
            /*, String title_en, String body_en, String image_en, String title_de, String body_de, String image_de*/) {
        this.title = title;
        this.body = body;
        this.image = image;

        this.title_ca = title_ca;
        this.body_ca = body_ca;
        this.image_ca = image_ca;

        /* Para futuras actualizaciones de idioma */
//        this.title_en = title_en;
//        this.body_en = body_en;
//        this.image_en = image_en;
//
//        this.title_de = title_de;
//        this.body_de = body_de;
//        this.image_de = image_de;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getImage() {
        return image;
    }

    public String getTitle_ca() {
        return title_ca;
    }

    public String getBody_ca() {
        return body_ca;
    }

    public String getImage_ca() {
        return image_ca;
    }

    /* Descomentar para futuras actualizaciones de idioma */
//    public String getTitle_en() {
//        return title_en;
//    }
//
//    public String getBody_en() {
//        return body_en;
//    }
//
//    public String getImage_en() {
//        return image_en;
//    }
//    public String getTitle_de() {
//        return title_de;
//    }
//
//    public String getBody_de() {
//        return body_de;
//    }
//
//    public String getImage_de() {
//        return image_de;
//    }
}
