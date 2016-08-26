package com.nascentdigital.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A wrapper for a broadcast receiver that provides network connectivity
 * state information, independent of network type (mobile, Wi-Fi, etc.).
 * {@hide}
 */
public class NetworkConnectivityListener {
    public static final String CONNECTIVITY_STATE_MESSAGE_KEY = "CONNECTIVITY_STATE_MESSAGE_KEY";

    private static final String TAG = "ConnectivityListener";
    private static final boolean DBG = false;

    private Context mContext;
    private final HashMap<Handler, Integer> mHandlers = new HashMap<>();
    private State mPreviousState;
    private State mCurrentState;
    private boolean mListening;
    private String mReason;
    private boolean mIsFailover;

    /**
     * Network connectivity information
     */
    private NetworkInfo mNetworkInfo;

    /**
     * In case of a Disconnect, the connectivity manager may have
     * already established, or may be attempting to establish, connectivity
     * with another network. If so, {@code mOtherNetworkInfo} will be non-null.
     */
    private NetworkInfo mOtherNetworkInfo;

    private final ConnectivityBroadcastReceiver mReceiver;

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ||
                    !mListening) {
                Log.w(TAG, "onReceived() called with " + mCurrentState.toString() + " and " + intent);
                return;
            }

            boolean noConnectivity = false;
            if (intent.getExtras().containsKey(ConnectivityManager.EXTRA_NO_CONNECTIVITY)) {
                noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            }
            // To support older versions of the Android OS, we can pull the EXTRA_NETWORK_INFO since
            // EXTRA_NO_CONNECTIVITY will not be provided
            else if (intent.getExtras().containsKey(ConnectivityManager.EXTRA_NETWORK_INFO)) {
                mNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                noConnectivity = !mNetworkInfo.isConnected();
            }

            mPreviousState = mCurrentState;
            if (noConnectivity) {
                mCurrentState = State.NOT_CONNECTED;
            } else {
                mCurrentState = State.CONNECTED;
            }
            mOtherNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

            mReason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            mIsFailover =
                    intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            if (DBG) {
                Log.d(TAG, "onReceive(): mNetworkInfo=" + mNetworkInfo + " mOtherNetworkInfo = "
                        + (mOtherNetworkInfo == null ? "[none]" : mOtherNetworkInfo +
                        " noConn=" + noConnectivity) + " mCurrentState=" + mCurrentState.toString());
            }

            // Notifiy any handlers.
            Iterator<Handler> it = mHandlers.keySet().iterator();
            while (it.hasNext()) {
                Handler target = it.next();
                Message message = Message.obtain(target, mHandlers.get(target));
                target.sendMessage(message);
            }
        }
    }

    public enum State {
        UNKNOWN,

        /**
         * This state is returned if there is connectivity to any network
         **/
        CONNECTED,
        /**
         * This state is returned if there is no connectivity to any network. This is set
         * to true under two circumstances:
         * <ul>
         * <li>When connectivity is lost to one network, and there is no other available
         * network to attempt to switch to.</li>
         * <li>When connectivity is lost to one network, and the attempt to switch to
         * another network fails.</li>
         */
        NOT_CONNECTED
    }

    /**
     * Create a new NetworkConnectivityListener.
     */
    public NetworkConnectivityListener() {
        mCurrentState = State.UNKNOWN;
        mReceiver = new ConnectivityBroadcastReceiver();
    }

    /**
     * This method starts listening for network connectivity state changes.
     *
     * @param context
     */
    public synchronized void startListening(Context context) {
        if (!mListening) {
            mContext = context;

            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(mReceiver, filter);
            mListening = true;
        }
    }

    /**
     * This method stops this class from listening for network changes.
     */
    public synchronized void stopListening() {
        if (mListening) {
            mContext.unregisterReceiver(mReceiver);
            mContext = null;
            mNetworkInfo = null;
            mOtherNetworkInfo = null;
            mIsFailover = false;
            mReason = null;
            mListening = false;
        }
    }

    /**
     * This methods registers a Handler to be called back onto with the specified what code when
     * the network connectivity state changes.
     *
     * @param target The target handler.
     * @param what   The what code to be used when posting a message to the handler.
     */
    public void registerHandler(Handler target, int what) {
        mHandlers.put(target, what);
    }

    /**
     * This methods unregisters the specified Handler.
     *
     * @param target
     */
    public void unregisterHandler(Handler target) {
        mHandlers.remove(target);
    }

    /**
     * Provides the previous state of the network before the most recent connectivity update.
     *
     * @return previous network connectivity state.
     */
    public State getPreviousState() {
        return mPreviousState;
    }

    /**
     * Provides the current state of the network provided by the most recent connectivity update.
     *
     * @return current network connectivity state.
     */
    public State getState() {
        return mCurrentState;
    }

    /**
     * Return the NetworkInfo associated with the most recent connectivity event.
     *
     * @return {@code NetworkInfo} for the network that had the most recent connectivity event.
     */
    public NetworkInfo getNetworkInfo() {
        return mNetworkInfo;
    }

    /**
     * If the most recent connectivity event was a DISCONNECT, return
     * any information supplied in the broadcast about an alternate
     * network that might be available. If this returns a non-null
     * value, then another broadcast should follow shortly indicating
     * whether connection to the other network succeeded.
     *
     * @return NetworkInfo
     */
    public NetworkInfo getOtherNetworkInfo() {
        return mOtherNetworkInfo;
    }

    /**
     * Returns true if the most recent event was for an attempt to switch over to
     * a new network following loss of connectivity on another network.
     *
     * @return {@code true} if this was a failover attempt, {@code false} otherwise.
     */
    public boolean isFailover() {
        return mIsFailover;
    }

    /**
     * An optional reason for the connectivity state change may have been supplied.
     * This returns it.
     *
     * @return the reason for the state change, if available, or {@code null}
     * otherwise.
     */
    public String getReason() {
        return mReason;
    }
}
