package com.habitissimo.vespapp;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
//import com.google.android.gms.gcm.GoogleCloudMessaging;
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
import com.habitissimo.vespapp.database.SightingsDB;
import com.habitissimo.vespapp.dialog.VersionDialog;
import com.habitissimo.vespapp.info.Info;
import com.habitissimo.vespapp.info.InfoDescriptionActivity;
import com.habitissimo.vespapp.map.Map;
import com.habitissimo.vespapp.menu.ContributorsActivity;
import com.habitissimo.vespapp.menu.AboutUsActivity;
import com.habitissimo.vespapp.menu.ContactActivity;
import com.habitissimo.vespapp.menu.HelpUsActivity;
import com.habitissimo.vespapp.menu.LOPDActivity;
import com.habitissimo.vespapp.sighting.PicturesActions;
import com.habitissimo.vespapp.sighting.Sighting;
import com.habitissimo.vespapp.sighting.NewSightingDataActivity;
import com.habitissimo.vespapp.sighting.SightingViewActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.security.AccessController.getContext;


public class MainActivity extends AppCompatActivity {

    private static final int NOTIF_ALERTA_ID = -1;
    private static final int NOTIF_ALERTA_ID_GROUP = -2;

    private static final int CAMERA_PERMISSION = 10;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 11;
    private static final int WRITE_EXTERNAL_STORAGE_AND_CAMERA_PERMISSION = 12;
    private static final int GET_ACCOUNT_PERMISSION = 13;
    private static final int READ_PHONE_PERMISSION = 14;

    private static final int TAKE_CAPTURE_REQUEST = 0;
    private static final int PICK_IMAGE_REQUEST = 1;

    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
    private static final String PROPERTY_USER = "user";
    private static final String SENDER_ID = "357137804481";

    private File photoFile;
    private Map map;
    private Marker marker;
    private HashMap<String, Sighting> relation = new HashMap<>();

    private Activity activity;
//    private GoogleCloudMessaging gcm;
    private String regid;

    private List<Sighting> sightingsList;
    private Toast mToastToShow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        SharedPreferences prefs =
                getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

        String email = prefs.getString("email", getEmailFromAccount());
//        String phone = prefs.getString("phone",getPhoneNumber());

//        setupGCM();
        checkAppVersion();

        fillSightingsListAndNotify();

        initTabs();
        initCamBtn();
        initSelectPicturesBtn();
    }

    private void fillSightingsListAndNotify() {
        final VespappApi api = Vespapp.get(this).getApi();

        final Callback<List<Sighting>> callback = new Callback<List<Sighting>>() {
            @Override
            public void onResponse(Call<List<Sighting>> call, Response<List<Sighting>> response) {
                sightingsList = response.body();
                notifySightings();
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

    private void notifySightings() {
        SightingsDB sdb = new SightingsDB(getApplicationContext());

        ArrayList<String> sightsNotChecked = sdb.getSightsNotChecked();
        ArrayList<Sighting> sightings = new ArrayList<>();

        for (int i = 0; i < sightsNotChecked.size(); ++i) {
            for (int j = 0; j < sightingsList.size(); ++j) {
                if (sightsNotChecked.get(i).equals(String.valueOf(sightingsList.get(j).getId()))) {
                    if (sightingsList.get(j).getStatus() == Sighting.STATUS_PROCESSED) {
                        sightings.add(sightingsList.get(j));
                        sdb.setChecked(sightsNotChecked.get(i));
                    }
                }
            }
        }

        if (ifUserRegistered())
            showNotifications(sightings);
    }

    private void showNotifications(ArrayList<Sighting> sights) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.vespaicon);

        if (sights.size() > 1) {

            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
            for (int i = 0; i < sights.size() && i < 7; ++i) {

                String created_at = sights.get(0).getCreated_at();
                String month = created_at.substring(5, 7);
                String day = created_at.substring(8, 10);

                String date = day + "/"+ month;

                String notif_msg = "";
                if (sights.get(i).getType() == Sighting.TYPE_NEST)
                    notif_msg += date + getString(R.string.notification_line_nest);
                else if (sights.get(i).getType() == Sighting.TYPE_WASP)
                    notif_msg += date + getString(R.string.notification_line_wasp);

                if (sights.get(i).is_valid() != null && sights.get(i).is_valid())
                    notif_msg += getString(R.string.notification_result_positive);
                else if (sights.get(i).is_valid() != null && !sights.get(i).is_valid())
                    notif_msg += getString(R.string.notification_result_negative);
                else {//if (is_valid == null)
                    notif_msg += getString(R.string.notification_result_unknown);
                }

                style.addLine(notif_msg);
                style.setBigContentTitle(sights.size() + getString(R.string.notification_processed_sightings));
                style.setSummaryText(getString(R.string.notification_processed_wasp_nest));

            }

            NotificationCompat.Builder summaryNotification = new NotificationCompat.Builder(this)
                    .setContentTitle(sights.size() + getString(R.string.notification_processed_sightings))
                    .setSmallIcon(R.drawable.vespaicon)
                    .setLargeIcon(largeIcon)
                    .setStyle(style)
                    .setGroup("summary")
                    .setGroupSummary(true)
                    .setAutoCancel(true);

            Intent notIntent =  new Intent(this, MainActivity.class);
            PendingIntent contIntent = PendingIntent.getActivity(
                    this, 0, notIntent, 0);

            summaryNotification.setContentIntent(contIntent);

            mNotificationManager.notify(NOTIF_ALERTA_ID_GROUP, summaryNotification.build());

        } else if (sights.size() == 1) {

            int icon;
            if (sights.get(0).getType() == Sighting.TYPE_NEST)
                icon = R.drawable.nidoicon;
            else
                icon = R.drawable.vespaicon;

            String created_at = sights.get(0).getCreated_at();
            String month = created_at.substring(5, 7);
            String day = created_at.substring(8, 10);
            String date = day + "/"+ month;

            String msg = date + getString(R.string.notification_line);
            if (sights.get(0).is_valid() != null && sights.get(0).is_valid()) {
                msg += getString(R.string.notification_result_positive);
            } else if (sights.get(0).is_valid() != null && !sights.get(0).is_valid()) {
                msg += getString(R.string.notification_result_negative);
            } else {//if (is_valid == null)
                msg += getString(R.string.notification_result_unknown);
            }

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(icon)
                            .setLargeIcon(largeIcon)
                            .setContentTitle(getString(R.string.notification_processed_sighting))
                            .setContentText(msg)
                            .setAutoCancel(true);

            Intent notIntent =  new Intent(this, SightingViewActivity.class);
            notIntent.putExtra("sightingObject", sights.get(0));

            PendingIntent contIntent = PendingIntent.getActivity(
                    this, 0, notIntent, 0);

            mBuilder.setContentIntent(contIntent);

            mNotificationManager.notify(NOTIF_ALERTA_ID, mBuilder.build());

        }

    }

//    private void setupGCM() {
//
//        if(isGooglePlayServicesAvailable(this)) {
//            gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
//
//            //Obtenemos el Registration ID guardado
//            regid = getRegistrationId(getApplicationContext());
//
//            //Si no disponemos de Registration ID comenzamos el registro
//            if (regid.equals("")) {
//                GCMRegister task = new GCMRegister();
//                String email = getEmailFromPreferences();
//                if (email.equals(""))
//                    email = getEmailFromAccount();
//                task.execute(email);
//            }
//        }
//        else {
//            Log.i("[GCM MainActivity]", "No se ha encontrado Google Play Services.");
//        }
//    }

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

                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Log.d("[MainActivity]","Permission to write to external storage not granted");
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Show permission explanation dialog...
                        showToast(getString(R.string.permission_warning_storage_1));
                    } else {
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                        showToast(getString(R.string.permission_warning_storage_2));
                    }
                }
                return;
            case WRITE_EXTERNAL_STORAGE_AND_CAMERA_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectPicture();

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Log.d("[MainActivity]","Permission to write to external storage not granted");
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Show permission explanation dialog...
                        showToast(getString(R.string.permission_warning_storage_3));
                    } else {
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                        showToast(getString(R.string.permission_warning_storage_2));
                    }
                }
                return;
            case GET_ACCOUNT_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    SharedPreferences prefs =
                            getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

                    String email = prefs.getString("email", getEmailFromAccount());
                    if (!email.equals("")) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("email", email);
                        editor.commit();
                    }

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Log.d("[MainActivity]","Permission to get user information not granted");
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.GET_ACCOUNTS)) {
                        //Show permission explanation dialog...
                        showToast(getString(R.string.permission_warning_account_1));
                    } else {
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                        showToast(getString(R.string.permission_warning_storage_2));
                    }

                }
                return;
            case READ_PHONE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("[MainActivity]"," READ_PHONE_Phone permission granted");

                    TelephonyManager tMgr = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
                    String mPhoneNumber = tMgr.getLine1Number();
                    if (mPhoneNumber == null)
                        mPhoneNumber = "";
                    else {
                        SharedPreferences prefs =
                                getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("phone", mPhoneNumber);
                        editor.commit();
                    }

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Log.d("[MainActivity]","Permission to write to external storage not granted");
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                        //Show permission explanation dialog...
                        Toast.makeText(getApplicationContext(), getString(R.string.permission_warning_storage_1), Toast.LENGTH_LONG);
                    } else {
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                        Toast.makeText(getApplicationContext(), getString(R.string.permission_warning_storage_2), Toast.LENGTH_LONG);
                    }
                }
                return;

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
                Intent i = new Intent(getApplicationContext(), HelpUsActivity.class);
                startActivity(i);
            }
        });
        ((RelativeLayout) findViewById(R.id.layout_menu_tab_lopd)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LOPDActivity.class);
                startActivity(i);
            }
        });
    }

    private boolean ifUserRegistered() {

        SharedPreferences prefs =
                getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

        String email = prefs.getString("email", "");
        String name = prefs.getString("name","");
        String phone = prefs.getString("phone", "");

        if (!email.equals("") && !name.equals("") && !phone.equals("")) {
            return true;
        }

        return false;
    }

    private String getEmailFromAccount() {
        Log.d("[MainActivity]","gettinEmailFromAccount");
        int permissionCheck_Accounts = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.GET_ACCOUNTS);
        if (permissionCheck_Accounts == PackageManager.PERMISSION_GRANTED) {
            Log.d("[MainActivity]","Email Permission Granted");
            Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
            Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    Log.e("[MAIN]","Email = "+account.name);
                    return account.name;
                }
            }
        }
        else {
            Log.d("[MainActivity]","Email Permission Not Granted");
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.GET_ACCOUNTS)) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        GET_ACCOUNT_PERMISSION);

                Log.d("[MainActivity]","Email Permission ASKED");
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        GET_ACCOUNT_PERMISSION);
            }
        }

        return "";
    }

    private String getPhoneNumber() {
        Log.d("[MainActivity]","getPhoneNumber");
        int permissionCheck_Phone = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_PHONE_STATE);

        String mPhoneNumber = "";
        if (permissionCheck_Phone == PackageManager.PERMISSION_GRANTED) {
            Log.d("[MainActivity]","Phone permission granted");
            TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneNumber = tMgr.getLine1Number();
            Log.d("[MainActivity]","MyPhone = " + mPhoneNumber);
            if (mPhoneNumber == null)
                return "";
        } else {
            Log.d("[MainActivity]","Phone permission NOT granted");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    READ_PHONE_PERMISSION);
        }
        return mPhoneNumber;
    }

    private void initSelectPicturesBtn() {
        Button btn = (Button) findViewById(R.id.btn_selFotos);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck_WriteExternalStorage = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int permissionCheck_Camera = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.CAMERA);
                if (permissionCheck_WriteExternalStorage == PackageManager.PERMISSION_GRANTED &&
                        permissionCheck_Camera == PackageManager.PERMISSION_GRANTED) {
                    selectPicture();
                }
                else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_EXTERNAL_STORAGE_AND_CAMERA_PERMISSION);

                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.CAMERA},
                                WRITE_EXTERNAL_STORAGE_AND_CAMERA_PERMISSION);

                    } else {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_EXTERNAL_STORAGE_AND_CAMERA_PERMISSION);

                    }
                }

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

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_PERMISSION);

            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_PERMISSION);
            }
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

        final GoogleMap Gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        //Comprobacion permisos para Android 6.0
        int permissionCheck_Coarse_Location = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine_Location = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse_Location == PackageManager.PERMISSION_GRANTED &&
                permissionCheck_Fine_Location == PackageManager.PERMISSION_GRANTED)
            Gmap.setMyLocationEnabled(true);


        map = new Map(Gmap);


        Gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Sighting s = getSightingByMarker(marker);
                changeActivityToSightingView(s);
                return false;
            }
        });

        SightingsDB sdb = new SightingsDB(getApplicationContext());
        ArrayList<String> localSights = sdb.getLocalSightings();

        if (sightingsList != null) {
            for (Sighting sighting : sightingsList) {
                if (sighting.is_public()) {
                    LatLng myLocation = new LatLng(sighting.getLat(), sighting.getLng());
                    if (sighting.is_valid()) {
                        marker = Gmap.addMarker(new MarkerOptions()
                                .position(myLocation)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    } else {
                        marker = Gmap.addMarker(new MarkerOptions().position(myLocation));
                    }
                    relation.put(marker.getId(), sighting);
                }
                if (ifUserRegistered()) {
                    for (int i = 0; i < localSights.size(); ++i) {
                        if (String.valueOf(sighting.getId()).equals(localSights.get(i))) {
                            LatLng myLocation = new LatLng(sighting.getLat(), sighting.getLng());
                            marker = Gmap.addMarker(new MarkerOptions()
                                    .position(myLocation)
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                            relation.put(marker.getId(), sighting);
                        }
                    }
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.getting_sightings_list),Toast.LENGTH_LONG).show();
            fillSightingsListAndNotify();
        }
        double lat = 39.56;
        double lng = 2.62;
        int zoom = 7;
        map.moveCamera(lat, lng, zoom);


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
                    Log.d("[MainActivity]","URI = "+selectedImage.toString());
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    picturePath = cursor.getString(columnIndex);
                    if (picturePath == null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.loading_photo), Toast.LENGTH_LONG).show();
                        InputStream is = null;
                        try {
                            is = getContentResolver().openInputStream(selectedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        picturePath = saveToSD(bitmap);
                    }
                    Log.d("[MainActivity]","picture path = "+picturePath);
                    cursor.close();
                    break;
            }

            savePictureToDatabase(picturePath);
            Intent i = new Intent(this, NewSightingDataActivity.class);
            startActivity(i);
        }
    }

    public String saveToSD(Bitmap selectedBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Vespapp/TempFiles");
        myDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String fname = timeStamp + ".jpg";

        String realPath = root + "/Vespapp/TempFiles/" + timeStamp + ".jpg";

        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return realPath;
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

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("[MainActivity]","onResume");

        if (sightingsList == null) {
            fillSightingsListAndNotify();
        }
//        if (isGooglePlayServicesAvailable(this)) {
//            Log.d("[MainActivity]", "Google Play Services available");
//        } else {
//            Log.d("[MainActivity]", "Google Play Services NOT available");
//        }

    }

    //Para Google Cloud Message
    private String getRegistrationId(Context context) {
        SharedPreferences prefs = getSharedPreferences(
               Constants.PREFERENCES,
                Context.MODE_PRIVATE);

        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.length() == 0) {
            Log.d("[MainActivity]", "Registro GCM no encontrado.");
            return "";
        }

        String registeredUser =
                prefs.getString(PROPERTY_USER, "user");

        int registeredVersion =
                prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);

        long expirationTime =
                prefs.getLong(PROPERTY_EXPIRATION_TIME, -1);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String expirationDate = sdf.format(new Date(expirationTime));

        Log.d("[MainActivity]", "Registro GCM encontrado (usuario=" + registeredUser +
                ", version=" + registeredVersion +
                ", expira=" + expirationDate + ")");

        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion) {
            Log.d("[MainActivity]", "Nueva versión de la aplicación.");
            return "";
        } else if (System.currentTimeMillis() > expirationTime) {
            Log.d("[MainActivity]", "Registro GCM expirado.");
            return "";
        }
        else if (!getEmailFromAccount().equals(registeredUser) ) {
//                || !getEmailFromPreferences().equals(registeredUser)) {
            Log.d("[MainActivity]", "Nuevo nombre de usuario.");
            return "";
        }

        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try
        {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            throw new RuntimeException("Error al obtener versión: " + e);
        }
    }

    //Para Google Cloud Message
    //http://www.sgoliver.net/blog/notificaciones-push-android-google-cloud-messaging-gcm-implementacion-cliente-nueva-version/

//    public class GCMRegister extends AsyncTask<String,Integer,String> {
//        @Override
//        protected String doInBackground(String... params) {
//            String msg = "";
//
//            try
//            {
//                if (gcm == null) {
//                    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
//                }
//
//                //Nos registramos en los servidores de GCM
//                regid = gcm.register(SENDER_ID);
//
//                Log.d("[MainActivity]", "Registrado en GCM: registration_id=" + regid);
//
//                //Nos registramos en nuestro servidor
//                sendGCMRegisterInfo(params[0], regid);
//
//                //Guardamos los datos del registro
//                setRegistrationId(getApplicationContext(), params[0], regid);
//            }
//            catch (IOException ex)
//            {
//                Log.d("[MainActivity]", "Error registro en GCM:" + ex.getMessage());
//            }
//
//            return msg;
//        }
//
//        private void setRegistrationId(Context context, String user, String regId) {
//            SharedPreferences prefs = getSharedPreferences(
//                    Constants.PREFERENCES,
//                    Context.MODE_PRIVATE);
//
//            int appVersion = getAppVersion(context);
//
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putString(PROPERTY_USER, user);
//            editor.putString(PROPERTY_REG_ID, regId);
//            editor.putInt(PROPERTY_APP_VERSION, appVersion);
//            editor.putLong(PROPERTY_EXPIRATION_TIME,
//                    System.currentTimeMillis() + 1000 * 3600 * 24 * 7);
//
//            editor.commit();
//        }
//
//        private void sendGCMRegisterInfo(String user, String regId) {
//            String version = String.valueOf(getAppVersion(getApplicationContext()));
//            String expiration_time = String.valueOf(System.currentTimeMillis() + 1000 * 3600 * 24 * 7);
//
//            GCM gcm = new GCM(user, regId, version, expiration_time);
//            new SendGCMRegisterInfo(getApplicationContext()).sendRegInfo(gcm);
//        }
//    }

    public void showToast(String text) {
        // Set the toast and duration
        int toastDurationInMilliSeconds = 12000;
        mToastToShow = Toast.makeText(this, text, Toast.LENGTH_LONG);

        // Set the countdown to display the toast
        CountDownTimer toastCountDown;
        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000 /*Tick duration*/) {
            public void onTick(long millisUntilFinished) {
                mToastToShow.show();
            }
            public void onFinish() {
                mToastToShow.cancel();
            }
        };

        // Show the toast and starts the countdown
        mToastToShow.show();
        toastCountDown.start();
    }
}
