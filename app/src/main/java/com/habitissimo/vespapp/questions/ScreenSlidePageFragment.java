package com.habitissimo.vespapp.questions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView;

        LinearLayout rl = (LinearLayout) View.inflate(getContext(), R.layout.fragment_buttons_questions, null);

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

        Button btn_next = (Button) rl.findViewById(R.id.but_next);
        btn_next.setText(R.string.questions_next);
        btn_next.setBackgroundColor(getResources().getColor(R.color.brandSecondary));
        btn_next.setTextColor(getContext().getResources().getColor(R.color.colorTitle));
        btn_next.setGravity(Gravity.CENTER);

        Button btn_prev = (Button) rl.findViewById(R.id.but_prev);
        btn_prev.setText(R.string.questions_prev);
        btn_prev.setBackgroundColor(getResources().getColor(R.color.brandSecondary));
        btn_prev.setTextColor(getContext().getResources().getColor(R.color.colorTitle));
        btn_prev.setGravity(Gravity.CENTER);

        if (question.isCheckBox()) {
            rootView = (ViewGroup) inflater.inflate(R.layout.fragment_multiple_answer, container, false);
            LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.layout_multiple_answer);

            String question_text = question.getTitle();
            //Cambiamos según idioma
            if (Locale.getDefault().getLanguage().equals("ca")) {//CATALÀ
                question_text = question.getTitle_ca();
            }
//            else if (Locale.getDefault().getLanguage().equals("en")) {//ENGLISH
//                question_text = question.getTitle_en();
//            } else if (Locale.getDefault().getLanguage().equals("de")) {//DEUTSCH
//                question_text = question.getTitle_de();
//            }


            TextView text = (TextView) rootView.findViewById(R.id.text_multiple_answer);
            text.setText(question_text);

            for (final Answer answer : question.getAvailable_answers()) {
                CheckBox checkAnswerFirst = new CheckBox(getActivity());

                LinearLayout.LayoutParams params_layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params_layout.leftMargin = 20;
                //Add space between button and text
                final float scale = this.getResources().getDisplayMetrics().density;
                checkAnswerFirst.setPadding(checkAnswerFirst.getPaddingLeft() + (int) (10.0f * scale + 0.5f),
                        checkAnswerFirst.getPaddingTop(),
                        checkAnswerFirst.getPaddingRight(),
                        checkAnswerFirst.getPaddingBottom());

                //Cambiamos según idioma
                String answer_text = answer.getValue();
                if (Locale.getDefault().getLanguage().equals("ca")) {//CATALÀ
                    answer_text = answer.getValue_ca();
                }
//                else if (Locale.getDefault().getLanguage().equals("en")) {//ENGLISH
//                    answer_text = answer.getValue_en();
//                } else if (Locale.getDefault().getLanguage().equals("de")) {//DEUTSCH
//                    answer_text = answer.getValue_de();
//                }

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

                ll.addView(checkAnswerFirst, params_layout);
            }

            if (position == QuestionsActivity.NUM_PAGES - 1) {

                ll.addView(btn_send);

                btn_send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionsActivity.updateSighting(answersMap);
                        answersMap.clear();
                    }
                });
            } else if (position == 0) {

                ll.addView(rl);

                btn_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionsActivity.setPage(position+1);
                    }
                });
            } else {

                ll.addView(rl);
                btn_prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionsActivity.setPage(position-1);
                    }
                });

                btn_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionsActivity.setPage(position+1);
                    }
                });
            }


        } else {
            rootView = (ViewGroup) inflater.inflate(R.layout.fragment_one_answer, container, false);
            LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.linear_layout);
            RadioGroup rg = (RadioGroup) rootView.findViewById(R.id.radiogroup_one_answer);

            String question_text = question.getTitle();
            //Cambiamos según idioma
            if (Locale.getDefault().getLanguage().equals("ca")) {//CATALÀ
                question_text = question.getTitle_ca();
            }
//            else if (Locale.getDefault().getLanguage().equals("en")) {//ENGLISH
//                question_text = question.getTitle_en();
//            } else if (Locale.getDefault().getLanguage().equals("de")) {//DEUTSCH
//                question_text = question.getTitle_de();
//            }

            TextView text = (TextView) rootView.findViewById(R.id.text_one_answer);
            text.setText(question_text);

            for (final Answer answer : question.getAvailable_answers()) {

                //Cambiamos según idioma
                String answer_text = answer.getValue();
                if (Locale.getDefault().getLanguage().equals("ca")) {//CATALÀ
                    answer_text = answer.getValue_ca();
                }
//                else if (Locale.getDefault().getLanguage().equals("en")) {//ENGLISH
//                    answer_text = answer.getValue_en();
//                } else if (Locale.getDefault().getLanguage().equals("de")) {//DEUTSCH
//                    answer_text = answer.getValue_de();
//                }

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
                btn_send.setLayoutParams(params);

                rg.addView(btn_send);

                btn_send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionsActivity.updateSighting(answersMap);
                        answersMap.clear();
                    }
                });
            } else if (position == 0) {

                ll.addView(rl);

                btn_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionsActivity.setPage(position+1);
                    }
                });
            } else {
                ll.addView(rl);
                btn_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionsActivity.setPage(position+1);
                    }
                });

                btn_prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionsActivity.setPage(position-1);
                    }
                });
            }
        }

        return rootView;
    }

    private void enableSendButton() {
    }


}