package com.gipsyz.panoramaglandroid;

import com.panoramagl.hotspots.PLIHotspot;

/**
 * Created by fmadaio on 14/05/2018.
 */

public interface PanoramaListener {
    void onCameraTransitionEnd(PLIHotspot touchedHotspot);
    void onDoubleTouchTransitionEnd(PLIHotspot touchedHotspot);
}
