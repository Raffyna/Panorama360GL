package com.gipsyz.panoramaglandroid;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import com.panoramagl.PLICamera;
import com.panoramagl.PLIView;
import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.PLViewListener;
import com.panoramagl.computation.PLVector3;
import com.panoramagl.hotspots.PLHotspot;
import com.panoramagl.hotspots.PLIHotspot;
import com.panoramagl.ios.UITouch;
import com.panoramagl.ios.structs.CGPoint;
import com.panoramagl.structs.PLPosition;
import com.panoramagl.utils.PLUtils;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import static java.lang.Math.PI;
import static java.lang.Math.toDegrees;

/**
 * Created by fmadaio on 14/05/2018.
 */

public class PanoramaManager {

    private PLSphericalPanorama mPanorama;
    private PLManager mPlManager;

    private PLIHotspot touchedHotspot = null;
    private boolean doubleTouch = false;

    private float[] mStartRotation = new float[2];
    private float[] mEndRotation = new float[2];
    private int cameraTimer = 0;
    private int startCameraTime = 1;

    private List<PLIHotspot> hotspots = new ArrayList<>();
    private boolean isAddingHotspot = false;

    private int[] resourceIds;
    private int currentResIndex = -1;

    private PanoramaListener panoramaListener = null;
    private MainActivity mActivity;


    public PanoramaManager(MainActivity activity, ViewGroup root){

        mActivity = activity;
        mPlManager = new PLManager(activity.getApplicationContext());
        mPlManager.setContentView(root);
        mPlManager.onCreate();

        mPlManager.setMinDistanceToEnableScrolling(50);
        mPlManager.setAccelerometerEnabled(false);
        mPlManager.setInertiaEnabled(true);
        mPlManager.setZoomEnabled(false);

        mPlManager.setListener(new PLViewListener(){
            @Override
            public void onTouchesMoved(PLIView view, List<UITouch> touches, MotionEvent event) {

                /*
                GL10 gl = mPlManager.getGLContext();
                CGPoint screenPoint = new CGPoint();
                for(PLIHotspot hotspot :hotspots) {

                    boolean visible = mPanorama.point3DToPosition2D(hotspot.getPosition(),screenPoint, gl);
                    Log.e("screen Point visible",""+visible);
                    Log.e("screen Point",screenPoint.x+" "+screenPoint.y);
                }*/
            }

            @Override
            public void onDidOutHotspot(PLIView view, PLIHotspot hotspot, CGPoint screenPoint, PLPosition scene3DPoint) {
                    if(!isAddingHotspot && cameraTimer<=0) {
                        //Toast.makeText(getApplicationContext(), "HOTSPOT AT X => "+scene3DPoint.x*10+" 3D Y => "+scene3DPoint.y*10+" 3D Z => "+scene3DPoint.z*10, Toast.LENGTH_LONG).show();
                        //panorama.removeHotspot(hotspot);

                        doubleTouch = touchedHotspot == hotspot && touchedHotspot!=null;
                        touchedHotspot = hotspot;

                        PLPosition position = hotspot.getPosition();
                        mStartRotation[0] = mPanorama.getCamera().getPitch();
                        mStartRotation[1] = mPanorama.getCamera().getYaw();
                        mEndRotation = convert3DtoYawPitch(position);

                        //calculating shortest distance between angles
                        float deltaAngle = angleDifference(mStartRotation[0],mEndRotation[0]);
                        mEndRotation[0] = mStartRotation[0]+deltaAngle;

                        deltaAngle = angleDifference(mStartRotation[1],mEndRotation[1]);
                        mEndRotation[1] = mStartRotation[1]+deltaAngle;

                        startCameraTransition(300); //milliseconds
                    }
                    isAddingHotspot = false;
            }

            @Override
            public void onRender(float dt) {

                float t = ((float)cameraTimer)/startCameraTime;
                float deltaT = 0.0166f; //TODO replace with true dt (fixed to 60fps)

                if(cameraTimer>0) {
                    mPanorama.getCamera().lookAt(
                            mStartRotation[0] * t + mEndRotation[0] * (1f - t),
                             mStartRotation[1] * t + mEndRotation[1] * (1f - t)
                    );

                    cameraTimer-=deltaT*1000;
                    if(cameraTimer<=0){

                        //onEnd
                        if(panoramaListener!=null) {
                            //calling methods on the main thread
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    panoramaListener.onCameraTransitionEnd(touchedHotspot);

                                    if(doubleTouch) { //same element touched two times
                                        panoramaListener.onDoubleTouchTransitionEnd(touchedHotspot);
                                    }else {
                                        doubleTouch = false;
                                    }
                                }
                            });
                        }

                        mPanorama.getCamera().lookAt(
                                mEndRotation[0],
                                mEndRotation[1]
                        );
                    }
                }
            }
        });
    }

    public void setPanoramaListener(PanoramaListener panoramaListener) {
        this.panoramaListener = panoramaListener;
    }

    public void addHotspot(PanoramaHotspot hotspot){
        mPanorama.addHotspot(hotspot);
    }


    public void updateHotspots() {
        mPanorama.getHotspots(hotspots);
    }

    //camera transition starting point
    public void startCameraTransition(int time){
        startCameraTime = time;
        cameraTimer = time;
    }

    //sets the panorma image
    public void changePanorama(int index,Context context) {
        if(currentResIndex == index) return;

        mPanorama = new PLSphericalPanorama();

        mPanorama.getCamera().setRotationSensitivity(1000f);
        mPanorama.setImage(new PLImage(PLUtils.getBitmap(context, resourceIds[index]), false));
        float pitch = 0f;
        float yaw = 180f;
        float zoomFactor = 0.8f;

        if(currentResIndex != -1) {
            PLICamera camera = mPlManager.getPanorama().getCamera();
            pitch = camera.getPitch();
            yaw = camera.getYaw();
            zoomFactor = camera.getZoomFactor();
        }

        mPanorama.getCamera().lookAtAndZoomFactor(pitch, yaw, zoomFactor, false);
        mPlManager.setPanorama(mPanorama);
        currentResIndex = index;
    }

    public void onResume() {
        mPlManager.onResume();
    }

    protected void onPause() {
        mPlManager.onPause();
    }

    protected void onDestroy() {
        mPlManager.onDestroy();
    }

    public boolean onTouchEvent(MotionEvent event) {
        return mPlManager.onTouchEvent(event);
    }

    public void setPanoramas(int[] resources) {
        resourceIds = resources;
    }

    //converts XY points in image to yaw and pitch angles
    public static float[] convertXYtoYawPitch(float x, float y) {
        float[] rot = new float[2];
        rot[0] = (float) (2*PI*(-x));
        rot[1] = (float) (PI *(2.0f-y));
        return rot;
    }

    //return minimum distance between two angles
    public static float angleDifference(float sa,float ea){
        float a = ea-sa;
        a = a + ((a>180) ? -360 : (a<-180) ? 360 : 0);
        return a;
    }

    public static float[] convert3DtoYawPitch(PLPosition position){
        float[] rot = new float[2];
        rot[0] = (float) toDegrees(Math.acos(-position.y)) - 90;
        rot[1] = (float) toDegrees(Math.atan2(position.z,position.x)) - 90;
        return rot;
    }

    public static void convertYawPitchTo3D(float[] angles, PLPosition result){

        result.x = (float) (Math.sin(angles[1]) * Math.sin(angles[0]));
        result.z = (float) (Math.sin(angles[1]) * Math.cos(angles[0]));
        result.y = (float) Math.cos(angles[1]);
    }

    public void deleteByTouchPoint(GL10 gl,Context context,CGPoint touch){

        PLVector3 point = new PLVector3();
        PLPosition nearScenePoint = new PLPosition();
        PLPosition farScenePoint = new PLPosition();

        PLVector3[] ray = new PLVector3[]{ new PLVector3(), new PLVector3() };
        ray[0].setValues(nearScenePoint.x,nearScenePoint.y,nearScenePoint.z);
        ray[1].setValues(farScenePoint.x,farScenePoint.y,farScenePoint.z);

        //picking by ray
        if(mPanorama.checkCollisionsWithRay(gl, ray, touch, true)==0) {
            isAddingHotspot = true;

            point.normalize();

            PLHotspot hotspot = new PLHotspot(1, new PLImage(PLUtils.getBitmap(context, R.raw.fabricio), false), 0, 0, 0.06f, 0.06f);
            hotspot.setXAxisEnabled(true);
            hotspot.setYAxisEnabled(true);
            hotspot.setPosition(point.x, point.y, point.z);

            mPanorama.addHotspot(hotspot);

            //Toast.makeText(getApplicationContext(), "Touch Screen X => "+event.getX()+" Y => "+event.getY(), Toast.LENGTH_LONG).show();
            Toast.makeText(context, "3D X => " + point.x * 10 + " 3D Y => " + point.y * 10 + " 3D Z => " + point.z * 10, Toast.LENGTH_LONG).show();
        }
    }
}
