package com.gipsyz.panoramaglandroid;

import android.opengl.GLU;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.panoramagl.PLICamera;
import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.computation.PLMath;
import com.panoramagl.computation.PLVector3;
import com.panoramagl.hotspots.PLHotspot;
import com.panoramagl.ios.structs.CGPoint;
import com.panoramagl.opengl.matrix.MatrixGrabber;
import com.panoramagl.structs.PLPosition;
import com.panoramagl.utils.PLUtils;

import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private PLSphericalPanorama panorama;
    private PLManager plManager;
    private ViewGroup root;
    private int currentIndex = -1;
    private int[] resourceIds = new int[]{ R.raw.spherical, R.raw.sighisoara_sphere_2};
    private int[] pos = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        plManager = new PLManager(this);
        root = (ViewGroup)findViewById(R.id.content_view);
        plManager.setContentView(root);
        plManager.onCreate();

        plManager.setAccelerometerEnabled(false);
        plManager.setInertiaEnabled(true);
        plManager.setZoomEnabled(false);

        changePanorama(0);

        /* Inserimento hotspot in coordinate sull'immagine*/

        //coordinate sull'immagine normalizzata  - punto in alto a sinistra [0,0] - punto in basso a destra [1,1]
        float[] angles = convertXYtoYawPitch(0.0f,0.5f);
        PLPosition scenePoint = new PLPosition();
        PLMath.convertFromSphericalToCartesian(1,angles[0],angles[1],scenePoint);

        PLHotspot hotspot = new PLHotspot(1,new PLImage(PLUtils.getBitmap(getApplicationContext(),R.raw.fabricio),false),0,0,0.06f,0.06f);
        hotspot.setXAxisEnabled(true);
        hotspot.setYAxisEnabled(true);
        hotspot.setPosition(scenePoint.x,scenePoint.y,scenePoint.z);

        panorama.addHotspot(hotspot);

        Toast.makeText(getApplicationContext(), "3D X => "+scenePoint.x+" 3D Y => "+scenePoint.y*10+" 3D Z => "+scenePoint.z*10, Toast.LENGTH_LONG).show();


        /* Inserimento hotspot on touch*/
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                PLPosition nearScenePoint = new PLPosition();
                PLPosition farScenePoint = new PLPosition();

                GL10 gl = plManager.getGLContext();

                panorama.convertPointTo3DPoint(gl,new CGPoint(event.getX(),event.getY()),0,nearScenePoint);
                panorama.convertPointTo3DPoint(gl,new CGPoint(event.getX(),event.getY()),1f,farScenePoint);

                PLVector3 point = new PLVector3(farScenePoint.x - nearScenePoint.x ,farScenePoint.y - nearScenePoint.y,farScenePoint.z - nearScenePoint.z);

                point.normalize();


                PLHotspot hotspot = new PLHotspot(1,new PLImage(PLUtils.getBitmap(getApplicationContext(),R.raw.fabricio),false),0,0,0.06f,0.06f);
                hotspot.setXAxisEnabled(true);
                hotspot.setYAxisEnabled(true);
                hotspot.setPosition(point.x,point.y,point.z);

                panorama.addHotspot(hotspot);
                //Toast.makeText(getApplicationContext(), "Touch Screen X => "+event.getX()+" Y => "+event.getY(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "3D X => "+point.x*10+" 3D Y => "+point.y*10+" 3D Z => "+point.z*10, Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    //converts XY points in image to yaw and pitch angles
    float[] convertXYtoYawPitch(float x, float y) {
        float[] rot = new float[2];
        rot[0] = (x + 0.25f) * 360 ;//0.25 initial offset
        rot[1] = (y - 0.5f) * -180f;
        return rot;
    }

    @Override
    protected void onResume() {
        super.onResume();
        plManager.onResume();
    }

    @Override
    protected void onPause() {
        plManager.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        plManager.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return plManager.onTouchEvent(event);
    }

    private void changePanorama(int index) {
        if(currentIndex == index) return;

        panorama = new PLSphericalPanorama();

        panorama.getCamera().setRotationSensitivity(1000f);
        panorama.setImage(new PLImage(PLUtils.getBitmap(this, resourceIds[index]), false));
        float pitch = 0f;
        float yaw = 180f;
        float zoomFactor = 0.8f;

        if(currentIndex != -1) {
            PLICamera camera = plManager.getPanorama().getCamera();
            pitch = camera.getPitch();
            yaw = camera.getYaw();
            zoomFactor = camera.getZoomFactor();
        }

        panorama.getCamera().lookAtAndZoomFactor(pitch, yaw, zoomFactor, false);
        plManager.setPanorama(panorama);
        currentIndex = index;
    }
}
