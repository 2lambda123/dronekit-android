package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.ConnectionActions.ACTION_CONNECT;
import static com.o3dr.services.android.lib.drone.action.ConnectionActions.ACTION_DISCONNECT;
import static com.o3dr.services.android.lib.drone.action.ConnectionActions.EXTRA_CONNECT_PARAMETER;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_DO_GUIDED_TAKEOFF;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SEND_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.ControlActions.ACTION_SET_GUIDED_ALTITUDE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_ALTITUDE;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_FORCE_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.ControlActions.EXTRA_GUIDED_POINT;
import static com.o3dr.services.android.lib.drone.action.ParameterActions.ACTION_REFRESH_PARAMETERS;
import static com.o3dr.services.android.lib.drone.action.ParameterActions.ACTION_WRITE_PARAMETERS;
import static com.o3dr.services.android.lib.drone.action.ParameterActions.EXTRA_PARAMETERS;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_ARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.ACTION_SET_VEHICLE_MODE;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_ARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_EMERGENCY_DISARM;
import static com.o3dr.services.android.lib.drone.action.StateActions.EXTRA_VEHICLE_MODE;

/**
 * Provides access to the vehicle specific functionality.
 */
public class VehicleApi extends Api {

    private static final ConcurrentHashMap<Drone, VehicleApi> vehicleApiCache = new ConcurrentHashMap<>();
    private static final Builder<VehicleApi> apiBuilder = new Builder<VehicleApi>() {
        @Override
        public VehicleApi build(Drone drone) {
            return new VehicleApi(drone);
        }
    };

    /**
     * Retrieves a vehicle api instance.
     * @param drone target vehicle
     * @return a VehicleApi instance.
     */
    public static VehicleApi getApi(final Drone drone) {
        return getApi(drone, vehicleApiCache, apiBuilder);
    }

    private final Drone drone;

    private VehicleApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Establish connection with the vehicle.
     *
     * @param parameter parameter for the connection.
     */
    public void connect(ConnectionParameter parameter){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_CONNECT_PARAMETER, parameter);
        Action connectAction = new Action(ACTION_CONNECT, params);
        drone.performAsyncAction(connectAction);
    }

    /**
     * Break connection with the vehicle.
     */
    public void disconnect(){
        drone.performAsyncAction(new Action(ACTION_DISCONNECT));
    }

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm true to arm, false to disarm.
     */
    public void arm(boolean arm) {
        arm(arm, null);
    }

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm             true to arm, false to disarm.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void arm(boolean arm, AbstractCommandListener listener){
        arm(arm, false, listener);
    }

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm             true to arm, false to disarm.
     * @param emergencyDisarm true to skip landing check and disarm immediately,
     *                        false to disarm only if it is safe to do so.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void arm(boolean arm, boolean emergencyDisarm, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_ARM, arm);
        params.putBoolean(EXTRA_EMERGENCY_DISARM, emergencyDisarm);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_ARM, params), listener);
    }

    /**
     * Change the vehicle mode for the connected drone.
     *
     * @param newMode new vehicle mode.
     */
    public void setVehicleMode(VehicleMode newMode) {
        setVehicleMode(newMode, null);
    }

    /**
     * Change the vehicle mode for the connected drone.
     *
     * @param newMode  new vehicle mode.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void setVehicleMode(VehicleMode newMode, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_VEHICLE_MODE, newMode);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_VEHICLE_MODE, params), listener);
    }

    /**
     * Generate action used to refresh the parameters for the connected drone.
     */
    public void refreshParameters(){
        drone.performAsyncAction(new Action(ACTION_REFRESH_PARAMETERS));
    }

    /**
     * Generate action used to write the given parameters to the connected drone.
     * @param parameters parameters to write to the drone.
     * @return
     */
    public void writeParameters(Parameters parameters){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_PARAMETERS, parameters);
        drone.performAsyncAction(new Action(ACTION_WRITE_PARAMETERS, params));
    }
}
