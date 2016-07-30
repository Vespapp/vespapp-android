package com.habitissimo.vespapp.gcm;

import java.io.Serializable;

/**
 * Created by Simó on 25/07/2016.
 */
public class GCM implements Serializable {

//    private int id;
    private String user;
    private String reg_id;
    private String version;
    private String expiration_time;

    public GCM() {

    }

    public GCM(String user, String reg_id, String version, String expiration_time) {
        this.user = user;
        this.reg_id = reg_id;
        this.version = version;
        this.expiration_time = expiration_time;
    }

    public String getUser() {
        return user;
    }

    public String getReg_id() {
        return reg_id;
    }

    public String getVersion() {
        return version;
    }

    public String getExpiration_time() {
        return expiration_time;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setReg_id(String reg_id) {
        this.reg_id = reg_id;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setExpiration_time(String expiration_time) {
        this.expiration_time = expiration_time;
    }
}
