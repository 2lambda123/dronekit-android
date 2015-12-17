package org.droidplanner.services.android.core.gcs.follow;

import android.os.Handler;

import org.droidplanner.services.android.core.drone.manager.DroneManager;

public class FollowLeft extends FollowHeadingAngle {

    public FollowLeft(DroneManager droneMgr, Handler handler, double radius) {
        super(droneMgr, handler, radius, -90.0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEFT;
    }

}
