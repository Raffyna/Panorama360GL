package com.lespinside.simplepanorama.sample;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.lespinside.simplepanorama.view.SphericalView;
import com.panoramagl.PLCameraListener;
import com.panoramagl.PLICamera;
import com.panoramagl.computation.PLVector3;
import com.panoramagl.enumerations.PLCameraAnimationType;
import com.panoramagl.utils.PLUtils;

public class MainActivity extends AppCompatActivity {

    private SphericalView sphericalView;
    private Bitmap[] bitmaps = new Bitmap[2];
    private ImageView icon;

    float x = 0;
    float y = 0;
    int width = 0 ;
    int height = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bitmaps[0] = PLUtils.getBitmap(this, R.raw.panorama);

        sphericalView = (SphericalView) findViewById(R.id.spherical_view);
        icon = (ImageView) findViewById(R.id.icon);


        BitmapDrawable drawable = (BitmapDrawable) icon.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        sphericalView.setPanorama(bitmaps[0], true, bitmap);


        DisplayMetrics metrics  = getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        sphericalView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return false;
            }
        });


        sphericalView.getCamera().setListener(new PLCameraListener() {
            @Override
            public void didBeginAnimation(Object sender, PLICamera camera, PLCameraAnimationType type) {


            }

            @Override
            public void didEndAnimation(Object sender, PLICamera camera, PLCameraAnimationType type) {
            }

            @Override
            public void didLookAt(Object sender, PLICamera camera, float pitch, float yaw, boolean animated) {
            }

            @Override
            public void didRotate(Object sender, PLICamera camera, float pitch, float yaw, float roll) {

                Log.e("","pitch => "+pitch+"  yaw = "+yaw);

                if (pitch==1.9356553554879063f && yaw == -2.14077619383343)
                {
                    Toast.makeText(getApplicationContext(), "Ciao", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void didFov(Object sender, PLICamera camera, float fov, boolean animated) {
            }

            @Override
            public void didReset(Object sender, PLICamera camera) {
            }
        });

//        sphericalView.getCamera().setYaw(-10.14077619383343f);
//        sphericalView.getCamera().setPitch(1.9356553554879063f);


    }

    @Override
    protected void onResume() {
        super.onResume();
        sphericalView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sphericalView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sphericalView.onDestroy();
    }

    private void changePanorama(int index) {
//        sphericalView.setPanorama(bitmaps[index], true, bitmap);
    }
}
