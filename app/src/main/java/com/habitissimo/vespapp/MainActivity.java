package com.habitissimo.vespapp;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.habitissimo.vespapp.api.VespappApi;
import com.habitissimo.vespapp.appversion.AppVersion;
import com.habitissimo.vespapp.async.Task;
import com.habitissimo.vespapp.async.TaskCallback;
import com.habitissimo.vespapp.database.Database;
import com.habitissimo.vespapp.dialog.EmailDialog;
import com.habitissimo.vespapp.dialog.VersionDialog;
import com.habitissimo.vespapp.info.Info;
import com.habitissimo.vespapp.info.InfoDescriptionActivity;
import com.habitissimo.vespapp.map.Map;
import com.habitissimo.vespapp.menu.ContributorsActivity;
import com.habitissimo.vespapp.menu.AboutUsActivity;
import com.habitissimo.vespapp.menu.ContactActivity;
import com.habitissimo.vespapp.sighting.PicturesActions;
import com.habitissimo.vespapp.sighting.Sighting;
import com.habitissimo.vespapp.sighting.NewSightingDataActivity;
import com.habitissimo.vespapp.sighting.SightingViewActivity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION = 10;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 11;

    private static final int TAKE_CAPTURE_REQUEST = 0;
    private static final int PICK_IMAGE_REQUEST = 1;

    private File photoFile;
    private Map map;
    private Marker marker;
    private HashMap<String, Sighting> relation = new HashMap<>();

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        checkAppVersion();

        initTabs();
        initCamBtn();
        initSelectPicturesBtn();
    }

    private void checkAppVersion() {
        final VespappApi api = Vespapp.get(this).getApi();

        final Callback<List<AppVersion>> callback = new Callback<List<AppVersion>>() {
            @Override
            public void onResponse(Call<List<AppVersion>> call, Response<List<AppVersion>> response) {
                final List<AppVersion> appVersionList = response.body();

                for (int i = 0; i < appVersionList.size(); ++i) {
                    //Si hay nueva version de app, avisa mediante un dialog
                    if (Integer.parseInt(appVersionList.get(i).getVersion()) == BuildConfig.VERSION_CODE
                            && !appVersionList.get(i).getIsLast()) {
                        String message = appVersionList.get(i).getMessage();
                        if (Locale.getDefault().getLanguage().equals("ca")) {//CATALÀ
                            message = appVersionList.get(i).getMessage_ca();
                        }
                        VersionDialog newFragment = VersionDialog.newInstance(message);
                        newFragment.show(getSupportFragmentManager(), "versionDialog");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AppVersion>> call, Throwable t) {
                System.out.println("onFailure " + t);
            }
        };
        Task.doInBackground(new TaskCallback<List<AppVersion>>() {
            @Override
            public List<AppVersion> executeInBackground() {
                Call<List<AppVersion>> call = api.getAppVersion();
                call.enqueue(callback);
                return null;
            }

            @Override
            public void onError(Throwable t) {
                callback.onFailure(null, t);
            }

            @Override
            public void onCompleted(List<AppVersion> locations) {
                callback.onResponse(null, Response.success((List<AppVersion>) null));

            }
        });
    }

//    private AppVersion getNewestAppVersion(List<AppVersion> appVersionList) {
//        AppVersion newest = new AppVersion("0", "Está usando una versión antigua de Vespapp", "Està utilitzant una versió antiga de Vespapp");
//
//        int max_version = 0;
//        for (int i = 0; i < appVersionList.size(); ++i) {
//            if (Integer.parseInt(appVersionList.get(i).getVersion()) > max_version) {
//                max_version = Integer.parseInt(appVersionList.get(i).getVersion());
//                newest = appVersionList.get(i);
//            }
//        }
//        return newest;
//    }

    private void initTabs() {
        final TabHost tabs = (TabHost) findViewById(R.id.tabs_main);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("GuiaTab");
        spec.setContent(R.id.layout_info_tab);
        spec.setIndicator("", ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_dialog_info, null));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("MainTab");
        spec.setContent(R.id.layout_main_tab);
        spec.setIndicator("", ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_camera, null));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("MapTab");
        spec.setContent(R.id.map);
        spec.setIndicator("Mapa");
        spec.setIndicator("", ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_dialog_map, null));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("MenuTab");
        spec.setContent(R.id.layout_menu_tab);
        spec.setIndicator("", ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_dialog_dialer, null));
        tabs.addTab(spec);

        //Add color initial
//        tabs.getTabWidget().getChildAt(1).setBackgroundColor(getResources().getColor(R.color.brandPrimary));

        tabs.setCurrentTab(1);
        tabs.getTabWidget().getChildAt(1).setBackgroundColor(getResources().getColor(R.color.orange));

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                int i = tabs.getCurrentTab();
                if (i == 0) {
                    if (map != null) {
                        map.removeMap();
                    }
                    tabs.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.orange));
                    tabs.getTabWidget().getChildAt(1).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(2).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(3).setBackgroundColor(getResources().getColor(R.color.brandPrimary));

                    getInfo();
                } else if (i == 1) {
                    if (map != null) {
                        map.removeMap();
                    }
                    LinearLayout ll = (LinearLayout) findViewById(R.id.layout_info_tab);
                    ll.removeAllViews();

                    tabs.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.orange));
                    tabs.getTabWidget().getChildAt(0).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(2).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(3).setBackgroundColor(getResources().getColor(R.color.brandPrimary));

                } else if (i == 2) {
                    LinearLayout ll = (LinearLayout) findViewById(R.id.layout_info_tab);
                    ll.removeAllViews();

                    tabs.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.orange));
                    tabs.getTabWidget().getChildAt(0).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(1).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(3).setBackgroundColor(getResources().getColor(R.color.brandPrimary));

                    initMap();
                } else if (i == 3) {
                    if (map != null) {
                        map.removeMap();
                    }
                    LinearLayout ll = (LinearLayout) findViewById(R.id.layout_info_tab);
                    ll.removeAllViews();

                    tabs.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.orange));
                    tabs.getTabWidget().getChildAt(0).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(1).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(2).setBackgroundColor(getResources().getColor(R.color.brandPrimary));

                    initMenuOptions();
                }
            }
        });
    }

    private void initCamBtn() {
        ImageButton btn = (ImageButton) findViewById(R.id.btn_cam);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int permissionCheck_Camera = ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CAMERA);
                    if (permissionCheck_Camera == PackageManager.PERMISSION_GRANTED) {
                        takePhoto();
                    }
                    else {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_PERMISSION);
                    }
                } catch (IOException e) {
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    try {
                        takePhoto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d("[MainActivity]","Permission to use Camera not granted");
                }
                return;
            }
            case WRITE_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        photoFile = PicturesActions.createImageFile();
                        savePicturePathToDatabase(photoFile.getAbsolutePath());
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(intent, TAKE_CAPTURE_REQUEST);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d("[MainActivity]","Permission to write to external storage not granted");
                }
        }
    }

    private void initMenuOptions() {
        ((RelativeLayout) findViewById(R.id.layout_menu_tab_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ContactActivity.class);
                startActivity(i);
            }
        });
        ((RelativeLayout) findViewById(R.id.layout_menu_tab_about_us)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AboutUsActivity.class);
                startActivity(i);
            }
        });
        ((RelativeLayout) findViewById(R.id.layout_menu_tab_contributors)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ContributorsActivity.class);
                startActivity(i);
            }
        });
        ((RelativeLayout) findViewById(R.id.layout_menu_tab_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailDialog newFragment = EmailDialog.newInstance();
                newFragment.show(getSupportFragmentManager(), "emailDialog");
            }
        });
//        if (!checkIfEmailRegistered())
//                ((RelativeLayout) findViewById(R.id.layout_menu_tab_login)).setBackgroundColor(getResources().getColor(R.color.red));
    }

    private boolean checkIfEmailRegistered() {

        if (!getEmailFromPreferences().equals(""))
            return true;

        if (!getEmailFromAccount().equals(""))
            return true;

        return false;
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

    private void initSelectPicturesBtn() {
        Button btn = (Button) findViewById(R.id.btn_selFotos);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });
    }


    public void takePhoto() throws IOException {

        int permissionCheck_WriteExternalStorage = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck_WriteExternalStorage == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoFile = PicturesActions.createImageFile();
            savePicturePathToDatabase(photoFile.getAbsolutePath());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(intent, TAKE_CAPTURE_REQUEST);
        }
        else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_PERMISSION);
        }

    }

    public void selectPicture() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE_REQUEST);
    }

    private void getInfo() {
        final VespappApi api = Vespapp.get(this).getApi();

        final Callback<List<Info>> callback = new Callback<List<Info>>() {
            @Override
            public void onResponse(Call<List<Info>> call, Response<List<Info>> response) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.layout_info_tab);
                final List<Info> infoList = response.body();
                for (final Info info : infoList) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                    params.setMargins(15, 50, 20, 20);

                    String title_info = info.getTitle();
                    //Cambiamos según idioma
                    if (Locale.getDefault().getLanguage().equals("ca")) {//CATALÀ
                        title_info = info.getTitle_ca();
                    }
//                    else if (Locale.getDefault().getLanguage().equals("en")) {//ENGLISH
//                        title_info = info.getTitle_en();
//                    } else if (Locale.getDefault().getLanguage().equals("de")) {//DEUTSCH
//                        title_info = info.getTitle_de();
//                    }

                    Button btn = new Button(getApplicationContext());
                    btn.setText(title_info);
                    btn.setBackgroundResource(R.drawable.button_selector);
                    btn.setTextSize((int) getResources().getDimension(R.dimen.text_info_button));
                    btn.setLayoutParams(params);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getApplicationContext(), InfoDescriptionActivity.class);
                            i.putExtra("infoObject", info);
                            startActivity(i);
                        }
                    });
                    ll.addView(btn);
                }
            }

            @Override
            public void onFailure(Call<List<Info>> call, Throwable t) {
                System.out.println("onFailure " + t);
            }
        };
        Task.doInBackground(new TaskCallback<List<Info>>() {
            @Override
            public List<Info> executeInBackground() {
                Call<List<Info>> call = api.getInfo();
                call.enqueue(callback);
                return null;
            }

            @Override
            public void onError(Throwable t) {
                callback.onFailure(null, t);
            }

            @Override
            public void onCompleted(List<Info> infos) {
                callback.onResponse(null, Response.success((List<Info>) null));

            }
        });
    }

    private void initMap() {
        final VespappApi api = Vespapp.get(this).getApi();

        final GoogleMap Gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        //Comprobacion permisos para Android 6.0
        int permissionCheck_Coarse_Location = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine_Location = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse_Location == PackageManager.PERMISSION_GRANTED &&
                permissionCheck_Fine_Location == PackageManager.PERMISSION_GRANTED)
            Gmap.setMyLocationEnabled(false);


        map = new Map(Gmap);


        Gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Sighting s = getSightingByMarker(marker);
                changeActivityToSightingView(s);
                return false;
            }
        });


        final Callback<List<Sighting>> callback = new Callback<List<Sighting>>() {
            @Override
            public void onResponse(Call<List<Sighting>> call, Response<List<Sighting>> response) {
                List<Sighting> sightingList = response.body();
                for (Sighting sighting : sightingList) {
                    if (sighting.is_public()) {
                        LatLng myLocation = new LatLng(sighting.getLat(), sighting.getLng());
                        if (sighting.is_valid())
                            marker = Gmap.addMarker(new MarkerOptions()
                                    .position(myLocation)
                                    .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        else
                            marker = Gmap.addMarker(new MarkerOptions().position(myLocation));
                        relation.put(marker.getId(), sighting);
                    }
                }
                double lat = 39.56;
                double lng = 2.62;
                int zoom = 8;
                map.moveCamera(lat, lng, zoom);
            }

            @Override
            public void onFailure(Call<List<Sighting>> call, Throwable t) {
                System.out.println("onFailure " + t);
            }
        };
        Task.doInBackground(new TaskCallback<List<Sighting>>() {
            @Override
            public List<Sighting> executeInBackground() {
                Call<List<Sighting>> call = api.getSightings();
                call.enqueue(callback);
                return null;
            }

            @Override
            public void onError(Throwable t) {
                callback.onFailure(null, t);
            }

            @Override
            public void onCompleted(List<Sighting> sightings) {
                callback.onResponse(null, Response.success((List<Sighting>) null));

            }
        });
    }

    private void changeActivityToSightingView(Sighting sighting) {
        Intent i = new Intent(this, SightingViewActivity.class);
        i.putExtra("sightingObject", sighting);
        startActivity(i);
    }

    private Sighting getSightingByMarker(Marker m) {
        Sighting sighting = relation.get(m.getId());
        return sighting;
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
            Intent i = new Intent(this, NewSightingDataActivity.class);
            startActivity(i);
        }
    }

    private void savePicturePathToDatabase(String picturePath) {
        Database.get(this).save(Constants.KEY_CAPTURE, picturePath);
    }

    private String getPicturePathFromDatabase() {
        return Database.get(this).load(Constants.KEY_CAPTURE);
    }

    private void savePictureToDatabase(String picturePath) {
        PicturesActions picturesActions = new PicturesActions();
        picturesActions.getList().add(picturePath);
        Database.get(this).save(Constants.PICTURES_LIST, picturesActions);
    }
}
