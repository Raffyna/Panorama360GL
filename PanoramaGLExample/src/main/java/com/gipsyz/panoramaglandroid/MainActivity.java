package com.gipsyz.panoramaglandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.panoramagl.PLImage;
import com.panoramagl.hotspots.PLIHotspot;
import com.panoramagl.ios.structs.CGPoint;
import com.panoramagl.utils.PLUtils;


public class MainActivity extends AppCompatActivity {

    private ViewGroup root;
    private int[] resourceIds = new int[]{ R.raw.car_full_4096, R.raw.sighisoara_sphere_2};

    private PanoramaManager panoramaManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = (ViewGroup)findViewById(R.id.content_view);

        panoramaManager = new PanoramaManager(this,root);


        panoramaManager.setPanoramas(resourceIds);
        panoramaManager.changePanorama(0,this);

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

            PanoramaHotspot hotspot = new PanoramaHotspot(
                    new PLImage(PLUtils.getBitmap(getApplicationContext(), R.raw.map_pin),false),
                    new CGPoint(points[i],points[i+1]));

            panoramaManager.addHotspot(hotspot);
        }

        //call always after loading the array of hotspots
        panoramaManager.updateHotspots();

        panoramaManager.setPanoramaListener(new PanoramaListener() {
            @Override
            public void onCameraTransitionEnd(PLIHotspot touchedHotspot) {
                Toast.makeText(getApplicationContext(), "Transition End", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDoubleTouchTransitionEnd(PLIHotspot touchedHotspot) {
                Toast.makeText(getApplicationContext(), "Double Touch", Toast.LENGTH_SHORT).show();
            }
        });

        root.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        panoramaManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        panoramaManager.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        panoramaManager.onDestroy();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return panoramaManager.onTouchEvent(event);
    }


}
