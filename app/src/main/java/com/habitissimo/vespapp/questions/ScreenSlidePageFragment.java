package com.habitissimo.vespapp.questions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.habitissimo.vespapp.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ScreenSlidePageFragment extends Fragment {

    private static final String ARG_QUESTION = "question";
    private static final String ARG_POSITION = "position";
    private Question question;
    private int position;
    private static Map<String, Answer> answersMap = new HashMap<>();

    public static ScreenSlidePageFragment create(int position, Question question) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_QUESTION, question);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);

        return fragment;
    }


    public ScreenSlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        question = (Question) getArguments().getSerializable(ARG_QUESTION);
        position = getArguments().getInt(ARG_POSITION);
    }

    private String translateQuestionToCatalan(String text) {
        String question = "";
        if (text.startsWith("¿Dónde la has encontrado?")) {
            question = "On l'has trobat?";
        } else if (text.startsWith("¿Estaba")) {
            question = "Estava...?";
        } else if (text.startsWith("¿Qué med")) {
            question = "Què mesurava el niu?";
        } else if (text.startsWith("¿Dónde estaba el nido")) {
            question = "On era el niu?";
        } else if (text.startsWith("¿Había avispas")) {
            question = "Hi havia vespes al seu voltant?";
        } else if (text.startsWith("¿Qué hacía")) {
            question = "Què feia?";
        } return question;
    }

    private String translateAnswerToCatalan(String text) {
        String answer = "";
        if (text.startsWith("Bosque")) {
            answer = "Bosc";
        } else if (text.startsWith("Zona rural")) {
            answer = "Zona rural";
        } else if (text.startsWith("Zona urbana")) {
            answer = "Zona urbana";
        } else if (text.startsWith("Sola")) {
            answer = "Tota sola";
        } else if (text.startsWith("En grupo")) {
            answer = "En grup";
        } else if (text.startsWith("Menos de")) {
            answer = "Menys de 20 cm.";
        } else if (text.startsWith("Más de")) {
            answer = "Més de 20 cm.";
        } else if (text.startsWith("Árbol")) {
            answer = "Arbre";
        } else if (text.startsWith("Edificio")) {
            answer = "Edifici";
        } else if (text.startsWith("Otros")) {
            answer = "Altres";
        } else if (text.startsWith("Sí")) {
            answer = "Sí";
        } else if (text.equals("No")) {
            answer = "No";
        } else if (text.startsWith("Comía fru")) {
            answer = "Menjava fruita/fems";
        } else if (text.startsWith("Capturaba")) {
            answer = "Capturava abelles o altres insectes";
        } else if (text.startsWith("Volaba")) {
            answer = "Volava";
        } else if (text.startsWith("No lo")) {
            answer = "No ho sé";
        }
        return answer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView;

        if (question.isCheckBox()) {
            rootView = (ViewGroup) inflater.inflate(R.layout.fragment_multiple_answer, container, false);
            LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.layout_multiple_answer);

            //Si esta en catala, feim parche a la espera de canviar WS
            String question_text = question.getTitle();
            if (Locale.getDefault().getLanguage().equals("ca")) {
                question_text = translateQuestionToCatalan(question.getTitle());
            }

            TextView text = (TextView) rootView.findViewById(R.id.text_multiple_answer);
            text.setText(question_text);

            for (final Answer answer : question.getAvailable_answers()) {
                CheckBox checkAnswerFirst = new CheckBox(getActivity());

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 20;
                //Add space between button and text
                final float scale = this.getResources().getDisplayMetrics().density;
                checkAnswerFirst.setPadding(checkAnswerFirst.getPaddingLeft() + (int) (10.0f * scale + 0.5f),
                        checkAnswerFirst.getPaddingTop(),
                        checkAnswerFirst.getPaddingRight(),
                        checkAnswerFirst.getPaddingBottom());

                //Si esta en catala, feim parche a la espera de canviar WS
                String answer_text = answer.getValue();
                if (Locale.getDefault().getLanguage().equals("ca")) {
                    answer_text = translateAnswerToCatalan(answer.getValue());
                }

                checkAnswerFirst.setText(answer_text);
                checkAnswerFirst.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean checked = ((CheckBox) v).isChecked();
                        if (checked) {
                            answersMap.put(answer.getValue(), answer);
                        } else {
                            answersMap.remove(answer.getValue());
                        }
                    }
                });

                ll.addView(checkAnswerFirst, params);
            }

            if (position == QuestionsActivity.NUM_PAGES - 1) {
                Button btn_send = new Button(getContext());
                btn_send.setText(R.string.questions_send);
                btn_send.setBackgroundColor(getResources().getColor(R.color.brandSecondary));
                btn_send.setTextColor(getContext().getResources().getColor(R.color.colorTitle));
                btn_send.setGravity(Gravity.CENTER);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 60, 0, 0);
                btn_send.setLayoutParams(params);
                ll.addView(btn_send);

                btn_send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionsActivity.updateSighting(answersMap);
                        answersMap.clear();
                    }
                });
            }


        } else {
            rootView = (ViewGroup) inflater.inflate(R.layout.fragment_one_answer, container, false);
            LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.linear_layout);
            RadioGroup rg = (RadioGroup) rootView.findViewById(R.id.radiogroup_one_answer);

            //Si esta en catala, feim parche a la espera de canviar WS
            String question_text = question.getTitle();
            if (Locale.getDefault().getLanguage().equals("ca")) {
                question_text = translateQuestionToCatalan(question.getTitle());
            }

            TextView text = (TextView) rootView.findViewById(R.id.text_one_answer);
            text.setText(question_text);

            for (final Answer answer : question.getAvailable_answers()) {

                //Si esta en catala, feim parche a la espera de canviar WS
                String answer_text = answer.getValue();
                if (Locale.getDefault().getLanguage().equals("ca")) {
                    answer_text = translateAnswerToCatalan(answer.getValue());
                }

                RadioButton radioAnswerFirst = new RadioButton(getActivity());
                radioAnswerFirst.setText(answer_text);
                radioAnswerFirst.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean checked = ((RadioButton) v).isChecked();
                        if (checked) {
                            answersMap.put(question.getTitle(), answer);
                        }
                    }
                });
                rg.addView(radioAnswerFirst);
            }

            if (position == QuestionsActivity.NUM_PAGES - 1) {
                Button btn_send = new Button(getContext());
                btn_send.setText(R.string.questions_send);
                btn_send.setBackgroundColor(getResources().getColor(R.color.brandSecondary));
                btn_send.setTextColor(getContext().getResources().getColor(R.color.colorTitle));
                btn_send.setGravity(Gravity.CENTER);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 60, 0, 0);
                btn_send.setLayoutParams(params);

                rg.addView(btn_send);
//                ll.addView(btn_send);

                btn_send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionsActivity.updateSighting(answersMap);
                        answersMap.clear();
                    }
                });
            }
        }

        return rootView;
    }

    private void enableSendButton() {
    }


}