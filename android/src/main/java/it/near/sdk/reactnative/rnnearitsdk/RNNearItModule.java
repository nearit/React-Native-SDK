/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 * Last changes by Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.nearit.ui_bindings.utils.PermissionsUtils;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.NearItManager;
import it.near.sdk.communication.OptOutNotifier;
import it.near.sdk.operation.NearItUserProfile;
import it.near.sdk.operation.values.NearMultipleChoiceDataPoint;
import it.near.sdk.reactions.couponplugin.CouponListener;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.feedbackplugin.FeedbackEvent;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.recipes.NearITEventHandler;
import it.near.sdk.recipes.foreground.ProximityListener;
import it.near.sdk.recipes.inbox.NotificationHistoryManager;
import it.near.sdk.recipes.inbox.model.HistoryItem;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.NearUtils;

public class RNNearItModule extends ReactContextBaseJavaModule implements LifecycleEventListener,
        ActivityEventListener,
        ProximityListener {

    // Module name
    static final String MODULE_NAME = "RNNearIt";

    // Event topic (used by JS to subscribe to generated events)
    static final String NATIVE_EVENTS_TOPIC = "RNNearItEvent";
    private static final String NATIVE_PERMISSIONS_TOPIC = "RNNearItPermissions";
    private static final String NATIVE_NOTIFICATION_HISTORY_TOPIC = "RNNearItNotificationHistory";

    @SuppressWarnings("WeakerAccess")
    // Local Events topic (used by LocalBroadcastReceiver to handle foreground notifications)
    static final String LOCAL_EVENTS_TOPIC = "RNNearItLocalEvents";

    // Module Constants
    // Events type
    private static final String EVENT_TYPE_PERMISSIONS = "NearIt.Events.PermissionStatus";
    static final String EVENT_TYPE_SIMPLE = "NearIt.Events.SimpleNotification";
    static final String EVENT_TYPE_CUSTOM_JSON = "NearIt.Events.CustomJSON";
    static final String EVENT_TYPE_COUPON = "NearIt.Events.Coupon";
    static final String EVENT_TYPE_CONTENT = "NearIt.Events.Content";
    static final String EVENT_TYPE_FEEDBACK = "NearIt.Events.Feedback";

    // Events content
    static final String EVENT_TYPE = "type";
    static final String EVENT_TRACKING_INFO = "trackingInfo";
    static final String EVENT_CONTENT = "content";
    static final String EVENT_CONTENT_MESSAGE = "message";
    static final String EVENT_CONTENT_DATA = "data";
    static final String EVENT_CONTENT_COUPON = "coupon";
    static final String EVENT_CONTENT_TEXT = "text";
    static final String EVENT_CONTENT_TITLE = "title";
    static final String EVENT_CONTENT_IMAGE = "image";
    static final String EVENT_CONTENT_CTA = "cta";
    static final String EVENT_CONTENT_FEEDBACK = "feedbackId";
    static final String EVENT_CONTENT_QUESTION = "feedbackQuestion";
    static final String EVENT_FROM_USER_ACTION = "fromUserAction";
    private static final String EVENT_STATUS = "status";

    // Location permission status
    private static final String PERMISSION_LOCATION_GRANTED = "NearIt.Permissions.Location.Granted";
    private static final String PERMISSION_LOCATION_DENIED = "NearIt.Permissions.Location.Denied";

    // Error codes
    private static final String E_SEND_FEEDBACK_ERROR = "E_SEND_FEEDBACK_ERROR";
    private static final String E_PROFILE_ID_GET_ERROR = "E_PROFILE_ID_GET_ERROR";
    private static final String E_PROFILE_ID_RESET_ERROR = "E_PROFILE_ID_RESET_ERROR";
    private static final String E_PROFILE_GET_USER_DATA_ERROR = "E_PROFILE_GET_USER_DATA_ERROR";
    private static final String E_COUPONS_PARSING_ERROR = "E_COUPONS_PARSING_ERROR";
    private static final String E_COUPONS_RETRIEVAL_ERROR = "E_COUPONS_RETRIEVAL_ERROR";
    private static final String E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR = "E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR";
    private static final String E_OPT_OUT_ERROR = "E_OPT_OUT_ERROR";

    @SuppressWarnings("WeakerAccess")
    public RNNearItModule(ReactApplicationContext reactContext) {
        super(reactContext);

        // Listen for Resume, Pause, Destroy events
        reactContext.addLifecycleEventListener(this);
        reactContext.addActivityEventListener(this);

        // Register LocalBroadcastReceiver to be used for Foreground notification handling
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(reactContext);
        localBroadcastManager.registerReceiver(new LocalBroadcastReceiver(), new IntentFilter(LOCAL_EVENTS_TOPIC));
    }

    // Module definition
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("NativeEventsTopic", NATIVE_EVENTS_TOPIC);
                put("NativePermissionsTopic", NATIVE_PERMISSIONS_TOPIC);
                put("NativeNotificationHistoryTopic", NATIVE_NOTIFICATION_HISTORY_TOPIC);
                put("Events", getEventsConstants());
                put("EventContent", getEventContentConstants());
                put("Statuses", getStatusConstants());
                put("Permissions", getPermissionStatusConstants());
            }

            private Map<String, Object> getEventsConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("PermissionStatus", EVENT_TYPE_PERMISSIONS);
                        put("SimpleNotification", EVENT_TYPE_SIMPLE);
                        put("CustomJson", EVENT_TYPE_CUSTOM_JSON);
                        put("Coupon", EVENT_TYPE_COUPON);
                        put("Content", EVENT_TYPE_CONTENT);
                        put("Feedback", EVENT_TYPE_FEEDBACK);
                    }
                });
            }

            private Map<String, Object> getEventContentConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("type", EVENT_TYPE);
                        put("trackingInfo", EVENT_TRACKING_INFO);
                        put("content", EVENT_CONTENT);
                        put("message", EVENT_CONTENT_MESSAGE);
                        put("data", EVENT_CONTENT_DATA);
                        put("coupon", EVENT_CONTENT_COUPON);
                        put("title", EVENT_CONTENT_TITLE);
                        put("text", EVENT_CONTENT_TEXT);
                        put("image", EVENT_CONTENT_IMAGE);
                        put("cta", EVENT_CONTENT_CTA);
                        put("feedbackId", EVENT_CONTENT_FEEDBACK);
                        put("question", EVENT_CONTENT_QUESTION);
                        put("fromUserAction", EVENT_FROM_USER_ACTION);
                        put("status", EVENT_STATUS);
                    }
                });
            }

            private Map<String, Object> getStatusConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("received", Recipe.RECEIVED);
                        put("opened", Recipe.OPENED);
                    }
                });
            }

            private Map<String, Object> getPermissionStatusConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("LocationGranted", PERMISSION_LOCATION_GRANTED);
                        put("LocationDenied", PERMISSION_LOCATION_DENIED);
                    }
                });
            }
        });
    }

    public static void onPostCreate(Context context, Intent intent) {
        if (NearUtils.carriesNearItContent(intent)) {
            NearUtils.parseContents(intent, new RNNearItCoreContentsListener(context, null, true));
        }
    }

    // ReactApp Lifecycle methods
    @Override
    public void onHostResume() {
        NearItManager.getInstance().addProximityListener(this);
        this.dispatchNotificationQueue();
    }

    @Override
    public void onHostPause() {
        NearItManager.getInstance().removeProximityListener(this);
    }

    @Override
    public void onHostDestroy() {
        RNNearItPersistedQueue.defaultQueue().resetListeners();
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        // Nothing to do here
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (NearUtils.carriesNearItContent(intent)) {
            NearUtils.parseContents(intent, new RNNearItCoreContentsListener(getReactApplicationContext(), getRCTDeviceEventEmitter(), true));
        }
    }

    // ReactNative listeners management
    @ReactMethod
    public void listenerRegistered(final Promise promise) {
        final int listenersCount = RNNearItPersistedQueue.defaultQueue().registerListener();
        Log.i(MODULE_NAME, String.format("listenerRegistered (Registered listeners: %d)", listenersCount));
        this.dispatchNotificationQueue();
        promise.resolve(true);
    }

    @ReactMethod
    public void listenerUnregistered(final Promise promise) {
        final int listenersCount = RNNearItPersistedQueue.defaultQueue().unregisterListener();
        Log.d(MODULE_NAME, String.format("listenerUnregistered (Registered listeners: %d)", listenersCount));
        promise.resolve(true);
    }

    @ReactMethod
    public void notificationHistoryListenerRegistered(final Promise promise) {
        // TODO:
    }

    @ReactMethod
    public void notificationHistoryListenerUnRegistered(final Promise promise) {
        // TODO:
    }

    // NearIT SDK Listeners
    @Override
    public void foregroundEvent(Parcelable parcelable, TrackingInfo trackingInfo) {
        NearUtils.parseContents(parcelable, trackingInfo, new RNNearItCoreContentsListener(getReactApplicationContext(), getRCTDeviceEventEmitter(), false));
    }

    // Radar related methods

    @SuppressLint("MissingPermission")
    @ReactMethod
    public void startRadar() {
        NearItManager.getInstance().startRadar();
    }

    @ReactMethod
    public void stopRadar() {
        NearItManager.getInstance().stopRadar();
    }

    // Trackings related methods

    @ReactMethod
    public void sendTracking(final String trackinInfoData, final String status) {
        NearItManager.getInstance().sendTracking(RNNearItUtils.unbundleTrackingInfo(trackinInfoData), status);
    }

    // Feedback related methods

    @ReactMethod
    public void sendFeedback(final String feedbackB64, final int rating, final String comment, final Promise promise) {
        final Feedback feedback;
        try {
            feedback = RNNearItUtils.unbundleFeedback(feedbackB64);

            NearItManager.getInstance().sendEvent(new FeedbackEvent(feedback, rating, comment), new NearITEventHandler() {
                @Override
                public void onSuccess() {
                    promise.resolve(null);
                }

                @Override
                public void onFail(int errorCode, String errorMessage) {
                    promise.reject(E_SEND_FEEDBACK_ERROR, "Failed to send feedback to NearIT", new Throwable(errorMessage));
                }
            });
        } catch (Exception e) {
            promise.reject(E_SEND_FEEDBACK_ERROR, "Failed to encode feedback to be sent", e);
        }
    }

    // ProfileId related methods

    @ReactMethod
    public void getProfileId(final Promise promise) {
        NearItManager.getInstance().getProfileId(new NearItUserProfile.ProfileFetchListener() {
            @Override
            public void onProfileId(String profileId) {
                promise.resolve(profileId);
            }

            @Override
            public void onError(String error) {
                promise.reject(E_PROFILE_ID_GET_ERROR, error);
            }
        });
    }

    @ReactMethod
    public void setProfileId(final String profileId) {
        NearItManager.getInstance().setProfileId(profileId);
    }

    @ReactMethod
    public void resetProfileId(final Promise promise) {
        NearItManager.getInstance().resetProfileId(new NearItUserProfile.ProfileFetchListener() {
            @Override
            public void onProfileId(String profileId) {
                promise.resolve(profileId);
            }

            @Override
            public void onError(String error) {
                promise.reject(E_PROFILE_ID_RESET_ERROR, error);
            }
        });
    }

    // User data related methods

    @ReactMethod
    public void setUserData(final String key, final String value) {
        NearItManager.getInstance().setUserData(key, value);
    }

    @ReactMethod
    public void setMultiChoiceUserData(final String key, final ReadableMap userData) {
        try {
            NearMultipleChoiceDataPoint multiChoiceData = null;
            HashMap<String, Boolean> data = new HashMap<String, Boolean>();
            if (userData != null) {
                for (Map.Entry<String, Object> entry : userData.toHashMap().entrySet()) {
                    if(entry.getValue() instanceof Boolean){
                        data.put(entry.getKey(), (Boolean) entry.getValue());
                    }
                }
            }
            if (!data.isEmpty()) {
                multiChoiceData = new NearMultipleChoiceDataPoint(data);
            }

            Log.d(MODULE_NAME, "setting user data: "+key+", "+data);
            NearItManager.getInstance().setUserData(key, multiChoiceData);
        } catch (Exception e) {
            Log.e(MODULE_NAME, "Error while setting userData");
        }
    }

    @ReactMethod
    public void getUserData(final Promise promise) {
        NearItManager.getInstance().getUserData(new NearItUserProfile.ProfileUserDataListener() {
            @Override
            public void onUserData(Map<String, Object> userData) {
                JSONObject result = new JSONObject(userData);
                promise.resolve(result);
            }

            @Override
            public void onError(String error) {
                promise.reject(E_PROFILE_GET_USER_DATA_ERROR, "Could not get user profile Id", new Throwable(error));
            }
        });
    }

    // Opt-out related methods

    @ReactMethod
    public void optOut(final Promise promise) {
        NearItManager.getInstance().optOut(new OptOutNotifier() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFailure(String error) {
                promise.reject(E_OPT_OUT_ERROR, error);
            }
        });
    }

    // NearIT Permissions request

    @ReactMethod
    public void isNotificationGranted(final Promise promise) {
        boolean granted = PermissionsUtils.areNotificationsEnabled(getReactApplicationContext());
        promise.resolve(granted);
    }

    @ReactMethod
    public void isLocationGranted(final Promise promise) {
        boolean granted = PermissionsUtils.checkLocationPermission(getReactApplicationContext());
        promise.resolve(granted);
    }

    @ReactMethod
    public void areLocationServicesOn(final Promise promise) {
        boolean areOn = PermissionsUtils.checkLocationServices(getReactApplicationContext());
        promise.resolve(areOn);
    }

    @ReactMethod
    public void isBluetoothEnabled(final Promise promise) {
        boolean enabled = PermissionsUtils.checkBluetooth(getReactApplicationContext());
        promise.resolve(enabled);
    }

    // In-app events related methods

    @ReactMethod
    public void triggerInAppEvent(final String eventKey) {
        NearItManager.getInstance().triggerInAppEvent(eventKey);
    }

    // Coupon related methods

    @ReactMethod
    public void getCoupons(final Promise promise) {
        NearItManager.getInstance().getCoupons(new CouponListener() {
            @Override
            public void onCouponsDownloaded(List<Coupon> list) {
                try {
                    final WritableArray coupons = new WritableNativeArray();
                    for (Coupon c : list) {
                        coupons.pushMap(RNNearItUtils.bundleCoupon(c));
                    }
                    promise.resolve(coupons);
                } catch (Exception e) {
                    promise.reject(E_COUPONS_PARSING_ERROR, e);
                }
            }

            @Override
            public void onCouponDownloadError(String errorMessage) {
                promise.reject(E_COUPONS_RETRIEVAL_ERROR, errorMessage);
            }
        });
    }

    // Notification History related methods

    @ReactMethod
    public void getNotificationHistory(final Promise promise) {
        NearItManager.getInstance().getHistory(new NotificationHistoryManager.OnNotificationHistoryListener() {
            @Override
            public void onNotifications(@NonNull List<HistoryItem> historyItemList) {
                final WritableArray items = new WritableNativeArray();
                for (HistoryItem item : historyItemList) {
                    items.pushMap(RNNearItUtils.bundleHistoryItem(item));
                }
                promise.resolve(items);
            }

            @Override
            public void onError(String error) {
                promise.reject(E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR, error);
            }
        });
    }

    @ReactMethod
    public void markNotificationHistoryAsOld() {
        NearItManager.getInstance().markNotificationHistoryAsOld();
    }

    @ReactMethod
    public void setNotificationHistoryUpdateListener() {
        // TODO:
    }

    private void dispatchNotificationQueue() {
        // Try to flush background notifications when a listener is added
        RNNearItPersistedQueue.dispatchNotificationsQueue(getReactApplicationContext(), new RNNearItPersistedQueue.NotificationDispatcher() {
            @Override
            public void onNotification(WritableMap notification) {
                Log.i(MODULE_NAME, "Dispatching background notification");
                try {
                    getRCTDeviceEventEmitter().emit(NATIVE_EVENTS_TOPIC, notification);
                } catch (Exception e) {
                    Log.i(MODULE_NAME, "Error dispatching backgrounded notification");
                }
            }
        });
    }

    private DeviceEventManagerModule.RCTDeviceEventEmitter getRCTDeviceEventEmitter() {
        return this.getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

    private void sendEventWithLocationPermissionStatus(final String permissionStatus) {
        try {
            // Create Event map to send to JS
            final WritableMap eventMap = new WritableNativeMap();
            eventMap.putString(EVENT_TYPE, EVENT_TYPE_PERMISSIONS);
            eventMap.putString(EVENT_STATUS, permissionStatus);

            // Send event to JS
            getRCTDeviceEventEmitter().emit(NATIVE_PERMISSIONS_TOPIC, eventMap);
        } catch (Exception e) {
            Log.e(MODULE_NAME, "Error while sending permissions to JS");
        }
    }

    // LocalBroadcastReceiver
    public class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && NearUtils.carriesNearItContent(intent)) {
                NearUtils.parseContents(intent, new RNNearItCoreContentsListener(getReactApplicationContext(), getRCTDeviceEventEmitter(), false));
            }
        }
    }
}
