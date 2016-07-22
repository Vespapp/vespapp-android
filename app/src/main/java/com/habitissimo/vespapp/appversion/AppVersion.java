package com.habitissimo.vespapp.appversion;

import java.io.Serializable;

/**
 * Created by Simó on 18/07/2016.
 */
public class AppVersion implements Serializable {
    private String version;
    private String message;
    private boolean is_last;

    private String message_ca;

    /* Para futuras actualizaciones de idioma */
//    private String message_en;
//    private String message_de;

    public AppVersion(String version, String message, boolean is_last, String message_ca /*, String message_en, String message_de*/) {
        this.version = version;
        this.message = message;

        this.is_last = is_last;
        this.message_ca = message_ca;
        /* Para futuras actualizaciones de idioma */
//        this.message_en = message_en;
//        this.message_de = message_de;
    }

    public String getVersion() {
        return version;
    }

    public String getMessage() {
        return message;
    }

    public boolean getIsLast() {
        return is_last;
    }

    public String getMessage_ca() {
        return message_ca;
    }

    /*Descomentar para futuras actualizaciones de idioma*/
//    public String getMessage_en() {
//        return message_en;
//    }
//
//    public String getMessage_de() {
//        return message_de;
//    }
}
