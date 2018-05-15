package com.gipsyz.panoramaglandroid;

import com.panoramagl.PLImage;
import com.panoramagl.hotspots.PLHotspot;
import com.panoramagl.ios.structs.CGPoint;
import com.panoramagl.structs.PLPosition;
import com.panoramagl.utils.PLUtils;

/**
 * Created by fmadaio on 14/05/2018.
 */

public class PanoramaHotspot extends PLHotspot {


    public PanoramaHotspot(PLImage image, CGPoint points) {

        super(1, image, 0, 0, 0.04f, 0.04f);
        initHotspot(points);
    }

    public PanoramaHotspot(PLImage image, CGPoint points,float width,float height) {

        super(1, image, 0, 0, width, height);
        initHotspot(points);
    }



    public void initHotspot(CGPoint points) {

        PLPosition scenePoint = new PLPosition();
        float[] angles = PanoramaManager.convertXYtoYawPitch(points.x, points.y);
        PanoramaManager.convertYawPitchTo3D(angles,scenePoint);

        this.setXAxisEnabled(true);
        this.setYAxisEnabled(true);
        this.setPosition(scenePoint.x, scenePoint.y, scenePoint.z);
    }
}
