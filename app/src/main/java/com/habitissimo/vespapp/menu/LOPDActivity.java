package com.habitissimo.vespapp.menu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.habitissimo.vespapp.R;

/**
 * Created by Simo on 20/05/2017.
 */

public class LOPDActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_lopd);

        initToolbar();
    }

    private void initToolbar() {
        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_menu_lopd);
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
        getSupportActionBar().setTitle(R.string.title_activity_menu_lopd);
    }
}
