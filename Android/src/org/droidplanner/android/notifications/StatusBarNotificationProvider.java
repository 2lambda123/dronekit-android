package org.droidplanner.android.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.droidplanner.R;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.utils.DroidplannerPrefs;
import org.droidplanner.android.utils.TextUtils;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

/**
 * Implements DroidPlanner's status bar notifications.
 */
public class StatusBarNotificationProvider implements NotificationHandler.NotificationProvider {

    private static final String LOG_TAG = StatusBarNotificationProvider.class.getSimpleName();

    /**
     * Android status bar's notification id.
     */
    private static final int NOTIFICATION_ID = 1;

    /**
     * Application context.
     */
    private final Context mContext;

    /**
     * Builder for the app notification.
     */
    private final NotificationCompat.Builder mNotificationBuilder;

    /**
     * Uses to generate the inbox style use to populate the notification.
     */
    private final InboxStyleBuilder mInboxBuilder;

    /**
     * Handle to the app preferences.
     */
    private final DroidplannerPrefs mAppPrefs;

    StatusBarNotificationProvider(Context context) {
        mContext = context;
        mAppPrefs = new DroidplannerPrefs(context);

        final PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, FlightActivity.class), 0);

        mNotificationBuilder = new NotificationCompat.Builder(context)
                .setContentIntent(contentIntent);

        mInboxBuilder = new InboxStyleBuilder();
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case DISCONNECTED:
                mNotificationBuilder.setContentTitle(mContext.getString(R.string.disconnected))
                        .setOngoing(false)
                        .setContentText("")
                        .setSmallIcon(R.drawable.ic_launcher_bw);
                mInboxBuilder.reset();
                break;

            case CONNECTED:
                mNotificationBuilder.setContentTitle(mContext.getString(R.string.connected))
                        .setOngoing(mAppPrefs.isNotificationPermanent())
                        .setSmallIcon(R.drawable.ic_launcher);
                updateFlightMode(drone);
                updateDroneState(drone);
                updateBattery(drone);
                updateGps(drone);
                updateHome(drone);
                updateRadio(drone);
                break;

            case BATTERY:
                updateBattery(drone);
                break;

            case GPS_FIX:
            case GPS_COUNT:
                updateGps(drone);
                break;

            case HOME:
                updateHome(drone);
                break;

            case RADIO:
                updateRadio(drone);
                break;

            case STATE:
                updateDroneState(drone);
                break;

            case MODE:
            case TYPE:
                updateFlightMode(drone);
                break;
        }

        showNotification();
    }

    private void updateRadio(Drone drone){
        mInboxBuilder.setLine(4, TextUtils.normal("Signal:   ",
                TextUtils.bold(String.format("%d%%", drone.radio.getSignalStrength()))));
    }

    private void updateHome(Drone drone){
        mInboxBuilder.setLine(0, TextUtils.normal("Home:   ", TextUtils.bold(drone.home
                .getDroneDistanceToHome().toString())));
    }

    private void updateGps(Drone drone){
        mInboxBuilder.setLine(1, TextUtils.normal("Satellite:   ",
                TextUtils.bold(String.format("%d, %s", drone.GPS.getSatCount(),
                        drone.GPS.getFixType()))
        ));
    }

    private void updateBattery(Drone drone){
        mInboxBuilder.setLine(3, TextUtils.normal("Battery:   ",
                TextUtils.bold(String.format("%2.1fv (%2.0f%%)",
                        drone.battery.getBattVolt(),
                        drone.battery.getBattRemain()))
        ));
    }

    private void updateDroneState(Drone drone){
        long timeInSeconds = drone.state.getFlightTime();
        long minutes = timeInSeconds / 60;
        long seconds = timeInSeconds % 60;

        mInboxBuilder.setLine(2, TextUtils.normal("Air Time:   ",
                TextUtils.bold(String.format("%02d:%02d", minutes, seconds))));
    }

    private void updateFlightMode(Drone drone){
        final String flightMode = drone.state.getMode().getName();
        final CharSequence modeSummary = TextUtils.normal("Mode:   ",
                TextUtils.bold(flightMode));
        mInboxBuilder.setSummary(modeSummary);
        mNotificationBuilder.setContentText(modeSummary);
    }

    /**
     * Build a notification from the notification builder, and display it.
     */
    private void showNotification() {
        mNotificationBuilder.setStyle(mInboxBuilder.generateInboxStyle());
        NotificationManager notMgr = (NotificationManager) mContext.getSystemService(Context
                .NOTIFICATION_SERVICE);
        notMgr.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    /**
     * Dismiss the app status bar notification.
     */
    private void dismissNotification() {
        NotificationManager notMgr = (NotificationManager) mContext.getSystemService(Context
                .NOTIFICATION_SERVICE);
        notMgr.cancelAll();
    }

    @Override
    public void quickNotify(String feedback) {
        Toast.makeText(mContext, feedback, Toast.LENGTH_LONG).show();
    }

    private static class InboxStyleBuilder {
        private static final int MAX_LINES_COUNT = 5;

        private final CharSequence[] mLines = new CharSequence[MAX_LINES_COUNT];

        private CharSequence mSummary;

        private boolean mHasContent = false;

        public void setLine(int index, CharSequence content) {
            if (index >= mLines.length || index < 0) {
                Log.w(LOG_TAG, "Invalid index (" + index + ") for inbox content.");
                return;
            }

            mLines[index] = content;
            mHasContent = true;
        }

        public void setSummary(CharSequence summary) {
            mSummary = summary;
            mHasContent = true;
        }

        public void reset() {
            mSummary = null;
            for (int i = 0; i < MAX_LINES_COUNT; i++) {
                mLines[i] = null;
            }

            mHasContent = false;
        }

        public NotificationCompat.InboxStyle generateInboxStyle() {
            if (!mHasContent) { return null; }

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            if (mSummary != null) { inboxStyle.setSummaryText(mSummary); }

            for (CharSequence line : mLines) {
                if (line != null) { inboxStyle.addLine(line); }
            }

            return inboxStyle;
        }
    }
}
