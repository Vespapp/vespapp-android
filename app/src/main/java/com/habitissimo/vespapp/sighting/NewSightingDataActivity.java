package com.habitissimo.vespapp.sighting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.habitissimo.vespapp.Constants;
import com.habitissimo.vespapp.R;
import com.habitissimo.vespapp.database.Database;
import com.habitissimo.vespapp.dialog.EmailDialog;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewSightingDataActivity extends AppCompatActivity {

    private static final int TAKE_CAPTURE_REQUEST = 0;
    private static final int PICK_IMAGE_REQUEST = 1;
    private File photoFile;

    RecyclerViewAdapter rcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sighting_data);
        ButterKnife.bind(this);

        checkEmailsAndWarn();

        initToolbar();
        initAlbum();
        initAddBtn();
        initSelectBtn();
    }

    private void checkEmailsAndWarn() {
        if (!checkIfEmailRegistered()) {
            Log.d("[NewSightingData]", "Email not registered, account = " + getEmailFromAccount() +
                    ", preferences = "+getEmailFromPreferences());
            SharedPreferences prefs =
                    getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

            boolean already_checked = prefs.getBoolean("checked", false);

            if (!already_checked) {
                EmailDialog newFragment = EmailDialog.newInstance();
                newFragment.show(getSupportFragmentManager(), "emailDialog2");

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("checked", true);
                editor.commit();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.checking_email_toast),
                        Toast.LENGTH_LONG).show();
            }

        } else {
            Log.d("[NewSightingData]", "Email already registered");
        }
    }
    private boolean checkIfEmailRegistered() {

        if (!getEmailFromPreferences().equals(""))
            return true;

        if (!getEmailFromAccount().equals(""))
            return true;

        return false;
    }

    private void initToolbar() {
        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_sigthing_data);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.confirm_cap_titulo);
    }

    private void initAlbum() {
        PicturesActions picturesActions = getPicturesList();

        GridLayoutManager gridLayout = new GridLayoutManager(this, 2);

        RecyclerView rView = (RecyclerView) findViewById(R.id.recycle_pictures);
        rView.setHasFixedSize(true);
        rView.setLayoutManager(gridLayout);

        rcAdapter = new RecyclerViewAdapter(this, picturesActions.getList());
        rView.setAdapter(rcAdapter);
    }

    private void initAddBtn() {
        Button btn = (Button) findViewById(R.id.btn_add_picture);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    takePhoto();
                } catch (IOException e) {
                }
            }
        });
    }

    private void initSelectBtn() {
        Button btn = (Button) findViewById(R.id.btn_select_picture);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });
    }

    private void takePhoto() throws IOException {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = PicturesActions.createImageFile();
        savePicturePathToDatabase(photoFile.getAbsolutePath());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(intent, TAKE_CAPTURE_REQUEST);
    }

    private void selectPicture() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE_REQUEST);
    }

    @OnClick(R.id.btn_nido)
    void onNestPressed() {
        onTypeOfSightPressed(Sighting.TYPE_NEST);
    }

    @OnClick(R.id.btn_avispa)
    void onWaspPressed() {
        onTypeOfSightPressed(Sighting.TYPE_WASP);
    }



    private void onTypeOfSightPressed(int type) {
        Sighting sighting = new Sighting();
        sighting.setType(type);
        String userEmail = getEmailFromPreferences();
        //Si no hay un email guardado voluntariamente, se coge la informacion de la cuenta
        if (userEmail.equals("")) {
            userEmail = getEmailFromAccount();
        } else {
            userEmail += " / "+getEmailFromAccount();
        }
        sighting.setContact(userEmail);

       // sighting.set_valid(null);

        Intent i = new Intent(this, NewSightingLocationsActivity.class);
        i.putExtra("sightingObject", sighting);
        startActivity(i);
    }

    private String getEmailFromAccount() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return "";
    }

    private String getEmailFromPreferences() {
        SharedPreferences prefs =
                getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

        return prefs.getString("email", "");
    }


    private PicturesActions getPicturesList() {
        return Database.get(this).load(Constants.PICTURES_LIST, PicturesActions.class);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            String picturePath = null;

            switch (requestCode) {
                case TAKE_CAPTURE_REQUEST:
                    picturePath = getPicturePathFromDatabase();
                    //photoFile = new File(picturePath);
                    //picturesActions.resize(photoFile, 640, 480);
                    break;
                case PICK_IMAGE_REQUEST:
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    break;
            }

            savePictureToDatabase(picturePath);
        }
    }

    private void savePicturePathToDatabase(String picturePath) {
        Database.get(this).save(Constants.KEY_CAPTURE, picturePath);
    }

    private String getPicturePathFromDatabase() {
        return Database.get(this).load(Constants.KEY_CAPTURE);
    }

    private void savePictureToDatabase(String picturePath) {
        PicturesActions picturesActions = Database.get(this).load(Constants.PICTURES_LIST, PicturesActions.class);

        if (picturesActions == null) {
            picturesActions = new PicturesActions();
        }

        if (picturesActions.getList().size() == 5) {
            Toast.makeText(this, R.string.newsightingdata_cantUpload, Toast.LENGTH_LONG).show();
        } else {
            picturesActions.getList().add(picturePath);
            Database.get(this).save(Constants.PICTURES_LIST, picturesActions);
            rcAdapter.addPhoto(picturePath);
        }
    }
}
