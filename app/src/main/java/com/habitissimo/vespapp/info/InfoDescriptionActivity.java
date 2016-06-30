package com.habitissimo.vespapp.info;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.Touch;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.habitissimo.vespapp.R;
import com.habitissimo.vespapp.image.TouchImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class InfoDescriptionActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_info_description2);

        initToolbar();

        zoomed = false;

        Intent i = getIntent();
        Info info = (Info) i.getSerializableExtra("infoObject");

        String title = info.getTitle();
        String body = info.getBody();
        String imageUrl = info.getImage();
        Log.d("[InfoDescription]", "ImageURL = "+imageUrl);

        //Si esta en catala, de moment feim parche
        Log.d("[InfoDescription]", "Locale = " + Locale.getDefault().getLanguage());
        if (Locale.getDefault().getLanguage().equals("ca")) {
            title = translateTitleToCatalan(info.getTitle());
            body = translateBodyToCatalan(info.getTitle());
        }

        setNameToolbar(title);

        TextView textBody = (TextView) findViewById(R.id.info_body);
        textBody.setText(body);

        try {/*
            ImageView imageInfo = (ImageView)findViewById(R.id.info_image);
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(imageUrl).getContent());
            imageInfo.setImageBitmap(bitmap);*/

            final ImageView imageInfo = (ImageView) findViewById(R.id.info_image);

            //TouchImageView --> custom ImageView from Mike Ortiz, allows ZoomIn
//            final TouchImageView imageInfo = (TouchImageView) findViewById(R.id.info_image);
            Bitmap bitmap = null;
            if (title.startsWith("Fitxa identificació vespa")) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.morfo_vespa);
            } else if (title.startsWith("Fitxa identificació n")) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nius_cat);
            } else {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(imageUrl).getContent());
            }
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int widthDisplay = size.x;
            int heightDisplay = size.y;
            double heightFinal = widthDisplay*0.6;
            int heightFinal1 =  (int) heightFinal;

            if (bitmap != null) {
                bitmap = resizeBitmap(bitmap, widthDisplay, heightFinal1);
                imageInfo.setImageBitmap(bitmap);

                final Bitmap finalBitmap = bitmap;
                imageInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        zoomImageFromThumb(imageInfo, finalBitmap);
                    }
                });
//                imageInfo.setMaxZoom(5);
            } else {
                Toast.makeText(getApplicationContext(), R.string.bitmap_null_sighting_view, Toast.LENGTH_SHORT).show();
                Log.e("[SightingViewAct]", "Null bitmap, maybe you are not connected to the Internet");
            }

            // Retrieve and cache the system's default "short" animation time.
            mShortAnimationDuration = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

        } catch (MalformedURLException e) {
            e.printStackTrace();
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

    private void initToolbar() {
        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_info_description);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setNameToolbar (String s){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(s);
    }

    private String translateTitleToCatalan(String title_info) {
        String title = title_info;
        if (title_info.startsWith("Ficha identificación avispa asiática")) {
            title = "Fitxa identificació vespa asiàtica";
        } else if (title_info.startsWith("Ficha identificación nido")) {
            title = "Fitxa identificació niu";
        } else if (title_info.startsWith("¡No")) {
            title = "No l'hem de confondre!";
        } else if (title_info.startsWith("Biolo")){
            title = "Biologia";
        } else if (title_info.startsWith("Impactos")) {
            title = "Impactes";
        } else if (title_info.startsWith("Importancia")) {
            title = "Importància de les abelles";
        } return title;
    }

    private String translateBodyToCatalan(String title_info) {
        String body = "";
        if (title_info.startsWith("Ficha identificación avispa asiática")) {
            body = "- Mida: 3 - 3,5 cm.\n" +
                    "- Coloració principalment fosca.\n" +
                    "- Cap: part superior negra, part frontal groga-taronja.\n" +
                    "- Tòrax: castany-negre.\n" +
                    "- Abdomen: castany amb una fina franja groga i amb el quart segment abdominal groc-taronja.\n" +
                    "- Cames: part proximal castanya-negra, part distal groga.\n";
        } else if (title_info.startsWith("Ficha identificación nido")) {
            body = "Niu primari:\n" +
                    "- Mida: 6 - 10 cm .\n" +
                    "- Forma: esfèrica.\n" +
                    "- Entrada per la part inferior.\n" +
                    "\n" +
                    "Niu secundari:\n" +
                    "- Mida: 40 - 70 cm de diàmetre, 60 - 90 cm alçada.\n" +
                    "- Forma: esfèrica-ovalada.\n" +
                    "- Entrada lateral d’1,5 cm aproximadament al terç superior.\n";
        } else if (title_info.startsWith("¡No tenemos")) {
            body = "Hi ha altres vespes autòctones que no s'han de confondre amb la vespa asiàtica.\n" +
                    "Les altres espècies de vespes es distingeixen de la vespa asiàtica perquè tenen " +
                    "un patró de coloració diferent a l'abdomen, i a més, presenten taques grogues al " +
                    "tòrax i al cap (a diferència de la vespa asiàtica, que té la part superior del cap" +
                    " i el tòrax negres).\n";
        } else if (title_info.startsWith("Biolo")){
            body = "És una espècie diürna.\n" +
                    "Les obreres cacen abelles mel·líferes i altres insectes per alimentar les cries. " +
                    "A més, també pot alimentar-se de fruita madura i nèctar.\n" +
                    "Les reines hibernen durant l'hivern i en arribar la primavera les reines fundadores " +
                    "construeixen els nius primaris (que tenen una mida aproximada de 10 cm). Entre abril i " +
                    "maig neixen les obreres i formen els nius secundaris, que són més grans. " +
                    "Al setembre-octubre és quan la colònia presenta el màxim nombre d'individus (1.200-1.800) " +
                    "i neixen els mascles, que fecunden les futures reines. A l'hivern les futures " +
                    "reines abandonen els nius i hibernen i la resta de la colònia mor.\n";
        } else if (title_info.startsWith("Impactos")) {
            body = "És una espècie exòtica invasora, originària del Sud-est asiàtic.\n" +
                    "\n" +
                    "Els impactes que provoca són a tres nivells:\n" +
                    "- Ecològic: pèrdua de pol·linitzadors , fet que suposa un greu desequilibri de l'ecosistema.\n" +
                    "- Econòmic: és una amenaça per a l'apicultura, ja que implica la pèrdua de caseres.\n" +
                    "- Sobre la salut humana: pel comportament i la perillositat és similar a la vespa autòctona.\n";
        } else if (title_info.startsWith("Importancia")) {
            body = "- Són pol·linitzadors essencials de plantes silvestres i conreades, i per això són imprescindibles " +
                    "per a la conservació dels ecosistemes i la producció d'aliments.\n" +
                    "- Elaboren mel i altres productes apícoles.\n";
        } return body;
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

        findViewById(R.id.container_dos).setBackgroundColor(Color.BLACK);
        findViewById(R.id.toolbar_info_description).setVisibility(View.INVISIBLE);

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
        findViewById(R.id.container_dos)
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

        findViewById(R.id.container_dos).setBackgroundResource(R.color.transparent);
        findViewById(R.id.toolbar_info_description).setVisibility(View.VISIBLE);

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
