package org.droidplanner.services.android.core.drone.autopilot.apm;

import android.content.Context;
import android.os.Handler;

import com.MAVLink.enums.MAV_TYPE;

import org.droidplanner.services.android.communication.model.DataStreams;
import org.droidplanner.services.android.core.drone.DroneInterfaces;
import org.droidplanner.services.android.core.drone.LogMessageListener;
import org.droidplanner.services.android.core.firmware.FirmwareType;
import org.droidplanner.services.android.core.model.AutopilotWarningParser;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public class ArduRover extends ArduPilot {

    public ArduRover(Context context, DataStreams.DataOutputStream mavClient, Handler handler, AutopilotWarningParser warningParser, LogMessageListener logListener) {
        super(context, mavClient, handler, warningParser, logListener);
    }

    @Override
    public int getType(){
        return MAV_TYPE.MAV_TYPE_GROUND_ROVER;
    }

    @Override
    public void setType(int type){}

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.ARDU_ROVER;
    }
}
