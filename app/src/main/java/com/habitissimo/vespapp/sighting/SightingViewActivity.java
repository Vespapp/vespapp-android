package com.habitissimo.vespapp.sighting;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.habitissimo.vespapp.R;
import com.habitissimo.vespapp.Vespapp;
import com.habitissimo.vespapp.api.VespappApi;
import com.habitissimo.vespapp.async.Task;
import com.habitissimo.vespapp.async.TaskCallback;
import com.habitissimo.vespapp.image.TouchImageView;
import com.habitissimo.vespapp.map.Map;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SightingViewActivity extends AppCompatActivity {

    private static Sighting sighting;

    private Map map;
    private Marker marker;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;

    private boolean zoomed;

    private TouchImageView expandedImageView;
    private float startScaleFinal;
    private Rect startBounds;
    private View _thumbView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sighting_view);

        initToolbar();

        Intent i = getIntent();
        sighting = (Sighting) i.getSerializableExtra("sightingObject");

        initTabs();

        getInfo();
        getPictures();
        initMap();

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    private void initToolbar() {
        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_sigthing_view);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorTitle));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.confirm_cap_map_sight);
    }

    private void initTabs() {
        final TabHost tabs = (TabHost) findViewById(R.id.tabs_sighting_view);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("PicsTab");
        spec.setContent(R.id.layout_pictures_sighting_tab);
        spec.setIndicator(getString(R.string.sightingview_tabs_photo));

        tabs.addTab(spec);

        spec = tabs.newTabSpec("MainTab");
        spec.setContent(R.id.layout_info_sighting_tab);
        spec.setIndicator(getString(R.string.sightingview_tabs_info));

        tabs.addTab(spec);

        spec = tabs.newTabSpec("MapTab");
        spec.setContent(R.id.map);
        spec.setIndicator(getString(R.string.sightingview_tabs_map));

        tabs.addTab(spec);

        //Add color initial
        tabs.getTabWidget().getChildAt(1).setBackgroundColor(getResources().getColor(R.color.brandPrimary));

        //Set color text
        int totalTabs = tabs.getTabWidget().getTabCount();

        for (int i=0; i<totalTabs; i++){
            TextView tv = (TextView) tabs.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.colorTitle));
        }

        tabs.setCurrentTab(1);
        tabs.getTabWidget().getChildAt(1).setBackgroundColor(getResources().getColor(R.color.orange));

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                int i = tabs.getCurrentTab();
                if (i == 0) {
                    tabs.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.orange));
                    tabs.getTabWidget().getChildAt(1).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(2).setBackgroundColor(getResources().getColor(R.color.brandPrimary));

                } else if (i == 1) {
                    tabs.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.orange));
                    tabs.getTabWidget().getChildAt(0).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(2).setBackgroundColor(getResources().getColor(R.color.brandPrimary));

                } else if (i == 2) {
                    tabs.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.orange));
                    tabs.getTabWidget().getChildAt(0).setBackgroundColor(getResources().getColor(R.color.brandPrimary));
                    tabs.getTabWidget().getChildAt(1).setBackgroundColor(getResources().getColor(R.color.brandPrimary));

                }
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void getInfo() {
        TextView lSource = (TextView) findViewById(R.id.sighting_source_label);
        lSource.setText(R.string.sightingview_notified);
        TextView tSource = (TextView) findViewById(R.id.sighting_source);
        String source = sighting.getSource();
        tSource.setText(source);

        if (source.equals("web")) {
            ImageView myImage = (ImageView) findViewById(R.id.sighting_source_image);
            myImage.setImageResource(R.mipmap.ic_computer);
        } else if (source.equals("app")) {
            ImageView myImage = (ImageView) findViewById(R.id.sighting_source_image);
            myImage.setImageResource(R.mipmap.ic_movile);
        } else { //Twitter
            ImageView myImage = (ImageView) findViewById(R.id.sighting_source_image);
            myImage.setImageResource(R.mipmap.ic_twitter);
        }

        TextView lLat = (TextView) findViewById(R.id.sighting_lat_label);
        lLat.setText(R.string.sightingview_lat);
        TextView tLat = (TextView) findViewById(R.id.sighting_lat);
        tLat.setText(String.valueOf(sighting.getLat()));

        TextView lLng = (TextView) findViewById(R.id.sighting_lng_label);
        lLng.setText(R.string.sightingview_lon);
        TextView tLng = (TextView) findViewById(R.id.sighting_lng);
        tLng.setText(String.valueOf(sighting.getLng()));

        TextView lType = (TextView) findViewById(R.id.sighting_type_label);
        lType.setText(R.string.sightingview_type);
        TextView tType = (TextView) findViewById(R.id.sighting_type);
        int type = sighting.getType();
        if (type == 1) {
            tType.setText(R.string.sightingview_wasp);
        } else if(type == 2){
            tType.setText(R.string.sightingview_nest);
        }else {
            tType.setText("-");
        }


        TextView lStatus = (TextView) findViewById(R.id.sighting_status_label);
        lStatus.setText(R.string.sightingview_status);
        TextView tStatus = (TextView) findViewById(R.id.sighting_status);
        int status = sighting.getStatus();
        if (status == 0) {
            tStatus.setText(R.string.sightingview_status_pending);
            tStatus.setBackgroundColor(getResources().getColor(R.color.statusPending));
        } else if (status == 1) {
            tStatus.setText(R.string.sightingview_status_processing);
            tStatus.setBackgroundColor(getResources().getColor(R.color.statusProcessing));
        } else if (status == 2) {
            tStatus.setText(R.string.sightingview_status_validated);
            tStatus.setBackgroundColor(getResources().getColor(R.color.statusValidated));
        }

        TextView lResult = (TextView) findViewById(R.id.sighting_result_label);
        lResult.setText(R.string.sightingview_result);
        TextView tResult = (TextView) findViewById(R.id.sighting_result);
        Boolean result = sighting.is_valid();
        if (result == null) {
            tResult.setText(R.string.sightingview_result_unknown);
            tResult.setBackgroundColor(getResources().getColor(R.color.resultUnknown));
        } else if (result == false) {
            tResult.setText(R.string.sightingview_result_negative);
            tResult.setBackgroundColor(getResources().getColor(R.color.resultNo));
        } else if (result == true) {
            tResult.setText(R.string.sightingview_result_positive);
            tResult.setBackgroundColor(getResources().getColor(R.color.resultYes));
        }

/*        TextView lData = (TextView) findViewById(R.id.sighting_data_label);
        lData.setText("Fecha:");
        TextView tData = (TextView) findViewById(R.id.sighting_data);

        tData.setText(sighting.getCreated_at());*/


        TextView lDescription = (TextView) findViewById(R.id.sighting_description_label);
        lDescription.setText(R.string.sightingview_description);
        TextView tDescription = (TextView) findViewById(R.id.sighting_description);
        if(!sighting.getFree_text().isEmpty()){
            tDescription.setText(sighting.getFree_text());
        }else{
            tDescription.setText("-");
        }


    }


    private void getPictures() {
        final VespappApi api = Vespapp.get(this).getApi();

        final Callback<List<Picture>> callback = new Callback<List<Picture>>() {
            @Override
            public void onResponse(Call<List<Picture>> call, Response<List<Picture>> response) {
                List<Picture> pictureList = response.body();
                sighting.setPictures(pictureList);
                printPictures(pictureList);
            }

            @Override
            public void onFailure(Call<List<Picture>> call, Throwable t) {
                System.out.println("onFailure " + t);
            }
        };
        Task.doInBackground(new TaskCallback<List<Picture>>() {
            @Override
            public List<Picture> executeInBackground() {
                Call<List<Picture>> call = api.getPhotos(String.valueOf(sighting.getId()));
                call.enqueue(callback);
                return null;
            }

            @Override
            public void onError(Throwable t) {
                callback.onFailure(null, t);
            }

            @Override
            public void onCompleted(List<Picture> locations) {
                callback.onResponse(null, Response.success((List<Picture>) null));

            }
        });
    }

    public String parseDateToddMMyyyy(String time) {
        System.out.println(time);
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        String outputPattern = "dd-MMM-yyyy h:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    private void printPictures(List<Picture> picturesList) {
        for (Picture picture : picturesList) {
            addItemList(picture);
        }
    }

    private void addItemList(final Picture picture) {
        Bitmap bitmap = null;
        ImageView imageInfo = null;
        try {
            LinearLayout ll = (LinearLayout) findViewById(R.id.layout_pictures_sighting_tab);

            imageInfo = new ImageView(this);
            LinearLayout.LayoutParams vp =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
            imageInfo.setLayoutParams(vp);

            imageInfo.setAdjustViewBounds(true); //Adjust the height to size photo
            imageInfo.setCropToPadding(true);
            vp.setMargins(0, 35, 0, 0); //(left, top, right, bottom);

            Log.d("[SightingViewAct]", "URL = "+picture.getFile());
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(picture.getFile()).getContent());
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int widthDisplay = size.x;
            int heightDisplay = size.y;
            double heightFinal = widthDisplay*0.6;
            int i =  (int) heightFinal;

            if (bitmap != null) {//ANR
                bitmap = resizeBitmap(bitmap, widthDisplay, i);
                imageInfo.setImageBitmap(bitmap);

                final ImageView finalImageInfo = imageInfo;
                final Bitmap finalBitmap = bitmap;
                imageInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        zoomImageFromThumb(finalImageInfo, finalBitmap);
                    }
                });

                ll.addView(imageInfo);
            } else {
                Toast.makeText(getApplicationContext(), R.string.bitmap_null_sighting_view, Toast.LENGTH_SHORT).show();
                Log.e("[SightingViewAct]", "Null bitmap, MAYbe you are not connected to the Internet");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
        }
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    private void initMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap Gmap) {
                int permissionCheck_Coarse_Location = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION);
                int permissionCheck_Fine_Location = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if (permissionCheck_Coarse_Location == PackageManager.PERMISSION_GRANTED &&
                        permissionCheck_Fine_Location == PackageManager.PERMISSION_GRANTED)
                    Gmap.setMyLocationEnabled(false);

                map = new Map(Gmap);

                double lat = sighting.getLat();
                double lng = sighting.getLng();
                int zoom = 15;

                LatLng myLocation = new LatLng(lat, lng);
                marker = Gmap.addMarker(new MarkerOptions().position(myLocation));
                map.moveCamera(lat, lng, zoom);
            }
        });

    }

    private void zoomImageFromThumb(final View thumbView, Bitmap bitmap) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
//        final TouchImageView expandedImageView = (TouchImageView) findViewById(
//                R.id.expanded_image);

        expandedImageView = (TouchImageView) findViewById(
                R.id.expanded_image);
//        expandedImageView.setImageResource(imageResId);
        expandedImageView.setImageBitmap(bitmap);
        expandedImageView.setMaxZoom(4);

        findViewById(R.id.container_tres).setBackgroundColor(Color.BLACK);
        findViewById(R.id.tabs_sighting_view).setVisibility(View.INVISIBLE);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.

        startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container_tres)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        zoomed = true;

        _thumbView = thumbView;
        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomOut(expandedImageView, startScaleFinal, startBounds, thumbView);
            }
        });
    }

    private void zoomOut(final TouchImageView expandedImageView, float startScaleFinal, Rect startBounds, final View thumbView) {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(expandedImageView, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.Y,startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_X, startScaleFinal))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_Y, startScaleFinal));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                thumbView.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                thumbView.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        findViewById(R.id.container_tres).setBackgroundResource(R.color.transparent);
        findViewById(R.id.tabs_sighting_view).setVisibility(View.VISIBLE);

        zoomed = false;

        expandedImageView.resetZoom();
    }

    @Override
    public void onBackPressed() {
        if (zoomed) {
            zoomOut(expandedImageView, startScaleFinal, startBounds, _thumbView);
        } else {
            super.onBackPressed();
        }
    }
}
