package com.gipsyz.panoramaglandroid;

import android.opengl.GLES10;
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

import com.panoramagl.PLConstants;
import com.panoramagl.PLICamera;
import com.panoramagl.PLIView;
import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.PLViewListener;
import com.panoramagl.computation.PLMath;
import com.panoramagl.computation.PLVector3;
import com.panoramagl.hotspots.PLHotspot;
import com.panoramagl.hotspots.PLIHotspot;
import com.panoramagl.ios.structs.CGPoint;
import com.panoramagl.opengl.matrix.MatrixGrabber;
import com.panoramagl.structs.PLPosition;
import com.panoramagl.structs.PLRotation;
import com.panoramagl.utils.PLUtils;

import javax.microedition.khronos.opengles.GL10;

import static com.panoramagl.computation.PLMath.convertFromSphericalToCartesian;
import static java.lang.Math.PI;
import static java.lang.Math.toDegrees;

public class MainActivity extends AppCompatActivity {

    private PLSphericalPanorama panorama;
    private PLManager plManager;
    private ViewGroup root;
    private int currentIndex = -1;
    private int[] resourceIds = new int[]{ R.raw.car_full_4096, R.raw.sighisoara_sphere_2};
    private boolean isAddingHotspot = false;

    private float[] startRotation = new float[2];
    private float[] endRotation = new float[2];
    private int cameraTimer = 0;
    private int startCameraTime = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        plManager = new PLManager(this);
        root = (ViewGroup)findViewById(R.id.content_view);
        plManager.setContentView(root);
        plManager.onCreate();

        plManager.setZoomEnabled(false);
        plManager.setMinDistanceToEnableScrolling(50);
        plManager.setAccelerometerEnabled(false);
        plManager.setInertiaEnabled(true);
        plManager.setZoomEnabled(false);
        plManager.setListener(new PLViewListener(){

            @Override
            public void onDidOutHotspot(PLIView view, PLIHotspot hotspot, CGPoint screenPoint, PLPosition scene3DPoint) {
                synchronized (MainActivity.class){
                    if(!isAddingHotspot && cameraTimer<=0) {
                        Toast.makeText(getApplicationContext(), "HOTSPOT AT X => "+scene3DPoint.x*10+" 3D Y => "+scene3DPoint.y*10+" 3D Z => "+scene3DPoint.z*10, Toast.LENGTH_LONG).show();
                        //panorama.removeHotspot(hotspot);

                        PLPosition position = hotspot.getPosition();
                        startRotation[0] = panorama.getCamera().getPitch();
                        startRotation[1] = panorama.getCamera().getYaw();
                        endRotation = convert3DtoYawPitch(position);
                        reformatAngles();

                        startCameraTransition(300); //milliseconds
                    }
                    isAddingHotspot = false;
                }
            }

            @Override
            public void onRender(float dt) {

                float t = ((float)cameraTimer)/startCameraTime;
                float deltaT = 0.0166f; //TODO replace with true dt (fixed to 60fps)

                if(cameraTimer>0) {
                    panorama.getCamera().lookAt(
                            startRotation[0] * t + endRotation[0] * (1f - t),
                             startRotation[1] * t + endRotation[1] * (1f - t)
                    );

                    cameraTimer-=deltaT*1000;
                    if(cameraTimer<=0){
                        //onEnd
                        panorama.getCamera().lookAt(
                                endRotation[0],
                                endRotation[1]
                        );
                    }
                }
            }
        });

        changePanorama(0);

        /* Inserimento hotspot in coordinate sull'immagine*/

        //coordinate sull'immagine normalizzata  - punto in alto a sinistra [0,0] - punto in basso a destra [1,1]

        float[] points = {
                            0.15f,0.61f,
                            0.21f,0.8f,
                            0.25f,0.34f,
                            0.5f,0.4f,
                            0.75f,0.7f
                        };

        for(int i=0;i<points.length;i+=2) {

            PLPosition scenePoint = new PLPosition();
            float[] angles = convertXYtoYawPitch(points[i], points[i+1]);

            double lat = angles[0];
            double lon = angles[1];

            scenePoint.x = (float) (Math.sin(lon) * Math.sin(lat));
            scenePoint.z = (float) (Math.sin(lon) * Math.cos(lat));
            scenePoint.y = (float) Math.cos(lon);


            PLHotspot hotspot = new PLHotspot(1, new PLImage(PLUtils.getBitmap(getApplicationContext(), R.raw.map_pin),false), 0, 0, 0.04f, 0.04f);
            hotspot.setXAxisEnabled(true);
            hotspot.setYAxisEnabled(true);
            hotspot.setPosition(scenePoint.x, scenePoint.y, scenePoint.z);

            panorama.addHotspot(hotspot);
        }


        /* Inserimento hotspot on touch*/
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                synchronized (MainActivity.class) {

                    //CODICE PER INSERIRE UN HOTSPOT

                    /*GL10 gl = plManager.getGLContext();

                    PLPosition nearScenePoint = new PLPosition();
                    PLPosition farScenePoint = new PLPosition();

                    panorama.convertPointTo3DPoint(gl, new CGPoint(event.getX(), event.getY()), 0, nearScenePoint);
                    panorama.convertPointTo3DPoint(gl, new CGPoint(event.getX(), event.getY()), 1f, farScenePoint);

                    PLVector3 point = new PLVector3(farScenePoint.x - nearScenePoint.x, farScenePoint.y - nearScenePoint.y, farScenePoint.z - nearScenePoint.z);


                    float pitch = (float) Math.atan2(point.z,1);
                    float yaw = (float) Math.atan2(point.y,point.x);
                    panorama.getCamera().lookAt(pitch,yaw);*/

                    /*
                    PLVector3[] ray = new PLVector3[]{ new PLVector3(), new PLVector3() };
                    ray[0].setValues(nearScenePoint.x,nearScenePoint.y,nearScenePoint.z);
                    ray[1].setValues(farScenePoint.x,farScenePoint.y,farScenePoint.z);

                    //picking by ray
                    if(panorama.checkCollisionsWithRay(gl, ray, new CGPoint(event.getX(), event.getY()), true)==0) {
                        isAddingHotspot = true;

                        point.normalize();


                        PLHotspot hotspot = new PLHotspot(1, new PLImage(PLUtils.getBitmap(getApplicationContext(), R.raw.fabricio), false), 0, 0, 0.06f, 0.06f);
                        hotspot.setXAxisEnabled(true);
                        hotspot.setYAxisEnabled(true);
                        hotspot.setPosition(point.x, point.y, point.z);

                        panorama.addHotspot(hotspot);

                        //Toast.makeText(getApplicationContext(), "Touch Screen X => "+event.getX()+" Y => "+event.getY(), Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "3D X => " + point.x * 10 + " 3D Y => " + point.y * 10 + " 3D Z => " + point.z * 10, Toast.LENGTH_LONG).show();
                    }*/
                }
                return false;
            }
        });
    }

    //camera transition starting point
    void startCameraTransition(int time){
        startCameraTime = time;
        cameraTimer = time;
    }

    //conversion of angles to minimize movement
    void reformatAngles(){

        float deltaAngle = angleDifference(startRotation[0],endRotation[0]);
        endRotation[0] = startRotation[0]+deltaAngle;

        deltaAngle = angleDifference(startRotation[1],endRotation[1]);
        endRotation[1] = startRotation[1]+deltaAngle;
    }

    float angleDifference(float sa,float ea){
        float a = ea-sa;
        a = a + ((a>180) ? -360 : (a<-180) ? 360 : 0);
        return a;
    }

    float[] convert3DtoYawPitch(PLPosition position){
        float[] rot = new float[2];
        rot[0] = (float) toDegrees(Math.acos(-position.y)) - 90;
        rot[1] = (float) toDegrees(Math.atan2(position.z,position.x)) - 90;
        return rot;
    }

    //converts XY points in image to yaw and pitch angles
    float[] convertXYtoYawPitch(float x, float y) {
        float[] rot = new float[2];
        rot[0] = (float) (2*PI*(-x));
        rot[1] = (float) (PI *(2.0f-y));
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
