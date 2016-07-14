package com.habitissimo.vespapp.questions;

import java.io.Serializable;
import java.util.List;

/**
 * Created by archi on 11/03/16.
 */
public class Question implements Serializable {

    private static final int TYPE_RADIO = 1;
    private static final int TYPE_CHECKBOX = 2;

    private int id;
    private List<Answer> answers;
    private List<Answer> available_answers;
    private String title;
    private String title_ca;
    /* Para futuras actualizaciones de idioma */
//    private String title_en;
//    private String title_de;

    private boolean is_active;

    private int question_type;
    private int sighting_type;

    public Question(int id, List<Answer> answers, List<Answer> available_answers,
                    String title, String title_ca, /*String title_en, String title_de*/
                    boolean is_active, int question_type, int sighting_type) {
        this.id = id;
        this.answers = answers;
        this.available_answers = available_answers;
        this.title = title;
        this.title_ca = title_ca;

        /*Para futuras actualizaciones de idioma */
//        this.title_en = title_en;
//        this.title_de = title_de;

        this.is_active = is_active;
        this.question_type = question_type;
        this.sighting_type = sighting_type;
    }

    public boolean isCheckBox() {
        return question_type == TYPE_CHECKBOX;
    }

    public boolean isRadioButton() {
        return question_type == TYPE_RADIO;
    }

    public int getId() {
        return id;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public List<Answer> getAvailable_answers() {
        return available_answers;
    }

    public String getTitle() {
        return title;
    }

    public String getTitle_ca() {
        return title_ca;
    }

    /* Desecomentar para futuras actualizaciones de idioma */
//    public String getTitle_en() {
//        return title_en;
//    }
//
//    public String getTitle_de() {
//        return title_de;
//    }

    public boolean is_active() {
        return is_active;
    }

    public int getQuestion_type() {
        return question_type;
    }

    public int getSighting_type() {
        return sighting_type;
    }
}
