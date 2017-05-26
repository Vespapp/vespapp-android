package com.habitissimo.vespapp.menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.habitissimo.vespapp.Constants;
import com.habitissimo.vespapp.R;

/**
 * Created by Simo on 20/05/2017.
 */

public class HelpUsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_help_us);

        initToolbar();
        initActivity();
        saveUserInfo();
    }

    private void initToolbar() {
        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_menu_help_us);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorTitle));
        toolbar.setBackgroundColor(getResources().getColor(R.color.brandPrimary));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_menu_help_us);
    }

    private void initActivity() {
        SharedPreferences prefs =
                getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

        String email = prefs.getString("email", "");
        String name = prefs.getString("name", "");
        String phone = prefs.getString("phone", "");

        EditText new_email = (EditText) findViewById(R.id.editText_mail);
        EditText new_name = (EditText) findViewById(R.id.editText_name);
        EditText new_phone = (EditText) findViewById(R.id.editText_phone);

        new_email.setText(email);
        new_name.setText(name);
        new_phone.setText(phone);
    }

    private void saveUserInfo() {
        ((Button) findViewById(R.id.save_info_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText new_email = (EditText) findViewById(R.id.editText_mail);
                EditText new_name = (EditText) findViewById(R.id.editText_name);
                EditText new_phone = (EditText) findViewById(R.id.editText_phone);

                SharedPreferences prefs =
                        getSharedPreferences(Constants.PREFERENCES,Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = prefs.edit();
                if (!new_email.getText().toString().equals(""))
                    editor.putString("email", new_email.getText().toString());
                if (!new_name.getText().toString().equals(""))
                    editor.putString("name", new_name.getText().toString());
                if (!new_phone.getText().toString().equals(""))
                    editor.putString("phone", new_phone.getText().toString());
                editor.putString("permission", "yes");
                editor.commit();

                if (new_email.getText().toString().equals("") &&
                        new_name.getText().toString().equals("") &&
                        new_phone.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.dialog_email_toast_empty), Toast.LENGTH_SHORT).show();
                }
                else if (!new_email.getText().toString().equals("") ||
                        !new_name.getText().toString().equals("") ||
                        !new_phone.getText().toString().equals("")) {

                    Toast.makeText(getApplicationContext(), getString(R.string.dialog_email_toast), Toast.LENGTH_SHORT).show();
                    finish();
                }


            }
        });
    }
}