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
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.nearit.ui_bindings.utils.PermissionsUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.NearItManager;
import it.near.sdk.communication.OptOutNotifier;
import it.near.sdk.operation.ProfileFetchListener;
import it.near.sdk.operation.ProfileUserDataListener;
import it.near.sdk.operation.values.NearMultipleChoiceDataPoint;
import it.near.sdk.reactions.couponplugin.CouponListener;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.feedbackplugin.FeedbackEvent;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.recipes.NearITEventHandler;
import it.near.sdk.recipes.foreground.ProximityListener;
import it.near.sdk.recipes.inbox.NotificationHistoryManager;
import it.near.sdk.recipes.inbox.model.HistoryItem;
import it.near.sdk.recipes.inbox.update.NotificationHistoryUpdateListener;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.NearUtils;

import static it.near.sdk.reactnative.rnnearitsdk.RNNearItConstants.*;

public class RNNearItModule extends ReactContextBaseJavaModule
        implements LifecycleEventListener, ProximityListener, NotificationHistoryUpdateListener {

    // Module name
    static final String MODULE_NAME = "RNNearIt";

    private RNNearItContentsListener proximityListener;

    @SuppressWarnings("WeakerAccess")
    public RNNearItModule(ReactApplicationContext reactContext) {
        super(reactContext);

        // Listen for Resume, Pause, Destroy events
        reactContext.addLifecycleEventListener(this);
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
                put("NativeNotificationHistoryTopic", NATIVE_NOTIFICATION_HISTORY_TOPIC);
                put("Events", getEventsConstants());
                put("EventContent", getEventContentConstants());
                put("Statuses", getStatusConstants());
                put("Permissions", getPermissionsConstants());
            }

            private Map<String, Object> getEventsConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("SimpleNotification", EVENT_TYPE_SIMPLE);
                        put("Content", EVENT_TYPE_CONTENT);
                        put("Feedback", EVENT_TYPE_FEEDBACK);
                        put("Coupon", EVENT_TYPE_COUPON);
                        put("CustomJson", EVENT_TYPE_CUSTOM_JSON);
                    }
                });
            }

            private Map<String, Object> getEventContentConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        // JS Event
                        put("type", EVENT_TYPE);
                        put("trackingInfo", EVENT_TRACKING_INFO);
                        put("message", EVENT_CONTENT_MESSAGE);
                        put("content", EVENT_CONTENT);
                        put("fromUserAction", EVENT_FROM_USER_ACTION);
                        // TrackingInfo
                        put("status", EVENT_STATUS);
                        // Common
                        put("title", EVENT_CONTENT_TITLE);
                        put("image", EVENT_IMAGE);
                        put("fullSize", EVENT_IMAGE_FULL_SIZE);
                        put("squareSize", EVENT_IMAGE_SQUARE_SIZE);
                        // Content
                        put("text", EVENT_CONTENT_TEXT);
                        put("cta", EVENT_CONTENT_CTA);
                        put("label", EVENT_CONTENT_CTA_LABEL);
                        put("url", EVENT_CONTENT_CTA_URL);
                        // Coupon
                        put("description", EVENT_COUPON_DESCRIPTION);
                        put("value", EVENT_COUPON_VALUE);
                        put("expiresAt", EVENT_COUPON_EXPIRES_AT);
                        put("redeemableFrom", EVENT_COUPON_REDEEMABLE_FROM);
                        put("serial", EVENT_COUPON_SERIAL);
                        put("claimedAt", EVENT_COUPON_CLAIMED_AT);
                        put("redeemedAt", EVENT_COUPON_REDEEMED_AT);
                        // Feedback
                        put("question", EVENT_FEEDBACK_QUESTION);
                        put("feedbackId", EVENT_FEEDBACK_ID);
                        // Notification History Item
                        put("read", NOTIFICATION_HISTORY_READ);
                        put("timestamp", NOTIFICATION_HISTORY_TIMESTAMP);
                        put("isNew", NOTIFICATION_HISTORY_IS_NEW);
                        put("notificationContent", NOTIFICATION_HISTORY_CONTENT);
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

            private Map<String, Object> getPermissionsConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("location", PERMISSIONS_LOCATION_PERMISSION);
                        put("notifications", PERMISSIONS_NOTIFICATIONS_PERMISSION);
                        put("bluetooth", PERMISSIONS_BLUETOOTH);
                        put("locationServices", PERMISSIONS_LOCATION_SERVICES);
                        put("always", PERMISSIONS_LOCATION_ALWAYS);
                        put("whenInUse", PERMISSIONS_LOCATION_WHEN_IN_USE);
                        put("denied", PERMISSIONS_LOCATION_DENIED);
                    }
                });
            }

        });
    }



    // ReactApp Lifecycle methods
    @Override
    public void onHostResume() {
        // TODO: remove this? -> this.dispatchNotificationQueue();
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        // Clear listeners
        RNNearItPersistedQueue.defaultQueue().resetListeners();
        NearItManager.getInstance().removeNotificationHistoryUpdateListener(this);
        NearItManager.getInstance().removeProximityListener(this);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void onDeviceReady() {
        RNNearItContentsListener contentsListener = new RNNearItContentsListener(getReactApplicationContext(), getRCTDeviceEventEmitter(), true);
        proximityListener = new RNNearItContentsListener(getReactApplicationContext(), getRCTDeviceEventEmitter(), false);
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Intent intent = activity.getIntent();
            if (intent != null) {
                if (NearUtils.carriesNearItContent(intent)) {
                    NearUtils.parseContents(intent, contentsListener);
                }
            }
        } else {
            Log.d(MODULE_NAME, "onDeviceReady::getCurrentActivity() returned null");
        }
        this.dispatchNotificationQueue();
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void disableDefaultRangingNotifications() {
        NearItManager.getInstance().disableDefaultRangingNotifications();
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void addProximityListener() {
        NearItManager.getInstance().addProximityListener(this);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void removeProximityListener() {
        NearItManager.getInstance().removeProximityListener(this);
    }

    @Override
    public void foregroundEvent(Parcelable parcelable, TrackingInfo trackingInfo) {
        /*
         * ProximityListener implementation:
         * this will be called when default ranging notification are disabled
         * and `addProximityListener` has been explicitly called
         */
        NearUtils.parseContents(parcelable, trackingInfo, proximityListener);
    }

    // ReactNative listeners management
    @SuppressWarnings("unused")
    @ReactMethod
    public void listenerRegistered(final Promise promise) {
        final int listenersCount = RNNearItPersistedQueue.defaultQueue().registerListener();
        Log.i(MODULE_NAME, String.format("listenerRegistered (Registered listeners: %d)", listenersCount));
        this.dispatchNotificationQueue();
        promise.resolve(true);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void listenerUnregistered(final Promise promise) {
        final int listenersCount = RNNearItPersistedQueue.defaultQueue().unregisterListener();
        Log.i(MODULE_NAME, String.format("listenerUnregistered (Registered listeners: %d)", listenersCount));
        promise.resolve(true);
    }

    // Radar related methods

    @SuppressWarnings("unused")
    @SuppressLint("MissingPermission")
    @ReactMethod
    public void startRadar() {
        NearItManager.getInstance().startRadar();
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void stopRadar() {
        NearItManager.getInstance().stopRadar();
    }

    // Trackings related methods

    @SuppressWarnings("unused")
    @ReactMethod
    public void sendTracking(final String trackingInfoData, final String status) {
        NearItManager.getInstance().sendTracking(RNNearItUtils.unbundleTrackingInfo(trackingInfoData), status);
    }

    // Feedback related methods

    @SuppressWarnings("unused")
    @ReactMethod
    public void sendFeedback(final ReadableMap bundledFeedback, final int rating, final String comment, final Promise promise) {
        final Feedback feedback = RNNearItUtils.unbundleFeedback(bundledFeedback);

        if (feedback != null) {
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
        } else {
            promise.reject(E_SEND_FEEDBACK_ERROR, "Failed to encode feedback to be sent");
        }
    }

    // ProfileId related methods

    @SuppressWarnings("unused")
    @ReactMethod
    public void getProfileId(final Promise promise) {
        NearItManager.getInstance().getProfileId(new ProfileFetchListener() {
            @Override
            public void onProfileId(String profileId) {
                promise.resolve(profileId);
            }

            @Override
            public void onProfileError(String error) {
                promise.reject(E_PROFILE_ID_GET_ERROR, error);
            }
        });
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void setProfileId(final String profileId) {
        NearItManager.getInstance().setProfileId(profileId);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void resetProfileId(final Promise promise) {
        NearItManager.getInstance().resetProfileId(new ProfileFetchListener() {
            @Override
            public void onProfileId(String profileId) {
                promise.resolve(profileId);
            }

            @Override
            public void onProfileError(String error) {
                promise.reject(E_PROFILE_ID_RESET_ERROR, error);
            }
        });
    }

    // User data related methods

    @SuppressWarnings("unused")
    @ReactMethod
    public void setUserData(final String key, final String value) {
        NearItManager.getInstance().setUserData(key, value);
    }

    @SuppressWarnings({"unused", "Convert2Diamond"})
    @ReactMethod
    public void setMultiChoiceUserData(final String key, final ReadableMap userData) {
        try {
            NearMultipleChoiceDataPoint multiChoiceData = null;
            HashMap<String, Boolean> data = new HashMap<String, Boolean>();
            if (userData != null) {
                for (Map.Entry<String, Object> entry : RNNearItUtils.toMap(userData).entrySet()) {
                    if(entry.getValue() instanceof Boolean){
                        data.put(entry.getKey(), (Boolean) entry.getValue());
                    }
                }
            }
            if (!data.isEmpty()) {
                multiChoiceData = new NearMultipleChoiceDataPoint(data);
            }

            Log.i(MODULE_NAME, "setting user data: "+key+", "+data);
            NearItManager.getInstance().setUserData(key, multiChoiceData);
        } catch (Exception e) {
            Log.e(MODULE_NAME, "Error while setting userData");
        }
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void getUserData(final Promise promise) {
        NearItManager.getInstance().getUserData(new ProfileUserDataListener() {
            @Override
            public void onUserData(Map<String, Object> userData) {
                promise.resolve(RNNearItUtils.toWritableMap(userData));
            }

            @Override
            public void onUserDataError(String error) {
                promise.reject(E_PROFILE_GET_USER_DATA_ERROR, "Could not get user profile Id", new Throwable(error));
            }
        });
    }

    // Opt-out related methods

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    @ReactMethod
    public void isNotificationGranted(final Promise promise) {
        boolean granted = PermissionsUtils.areNotificationsEnabled(getReactApplicationContext());
        promise.resolve(granted);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void isLocationGranted(final Promise promise) {
        boolean granted = PermissionsUtils.checkLocationPermission(getReactApplicationContext());
        promise.resolve(granted);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void areLocationServicesOn(final Promise promise) {
        boolean areOn = PermissionsUtils.checkLocationServices(getReactApplicationContext());
        promise.resolve(areOn);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void isBluetoothEnabled(final Promise promise) {
        boolean enabled = PermissionsUtils.checkBluetooth(getReactApplicationContext());
        promise.resolve(enabled);
    }

    // In-app events related methods

    @SuppressWarnings("unused")
    @ReactMethod
    public void triggerInAppEvent(final String eventKey) {
        NearItManager.getInstance().triggerInAppEvent(eventKey);
    }

    // Coupon related methods

    @SuppressWarnings("unused")
    @ReactMethod
    public void getCoupons(final Promise promise) {
        NearItManager.getInstance().getCoupons(new CouponListener() {
            @Override
            public void onCouponsDownloaded(List<Coupon> list) {
                try {
                    final WritableArray coupons = new WritableNativeArray();
                    for (Coupon coupon : list) {
                        coupons.pushMap(RNNearItUtils.bundleCoupon(coupon));
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

    @SuppressWarnings("unused")
    @ReactMethod
    public void getNotificationHistory(final Promise promise) {
        NearItManager.getInstance().getHistory(new NotificationHistoryManager.OnNotificationHistoryListener() {
            @Override
            public void onNotifications(@NonNull List<HistoryItem> historyItemList) {
                final WritableArray history = RNNearItUtils.bundleNotificationHistory(historyItemList);
                promise.resolve(history);
            }

            @Override
            public void onError(String error) {
                promise.reject(E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR, error);
            }
        });
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void markNotificationHistoryAsOld() {
        NearItManager.getInstance().markNotificationHistoryAsOld();
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void notificationHistoryListenerRegistered(final Promise promise) {
        NearItManager.getInstance().addNotificationHistoryUpdateListener(this);
        promise.resolve(true);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void notificationHistoryListenerUnregistered(final Promise promise) {
        NearItManager.getInstance().removeNotificationHistoryUpdateListener(this);
        promise.resolve(true);
    }

    @Override
    public void onNotificationHistoryUpdated(List<HistoryItem> items) {
        Log.i(MODULE_NAME, "Dispatching notification history update");
        if (getRCTDeviceEventEmitter() != null) {
            WritableArray history = RNNearItUtils.bundleNotificationHistory(items);
            getRCTDeviceEventEmitter().emit(NATIVE_NOTIFICATION_HISTORY_TOPIC, history);
        } else {
            Log.e(MODULE_NAME, "Error dispatching notification history update");
        }
    }

    private void dispatchNotificationQueue() {
        // Try to flush background notifications when a listener is added
        RNNearItPersistedQueue.dispatchNotificationsQueue(
                getReactApplicationContext(),
                new RNNearItPersistedQueue.NotificationDispatcher() {
                    @Override
                    public void onNotification(WritableMap notification) {
                        Log.i(MODULE_NAME, "Dispatching background notification");
                        if (getRCTDeviceEventEmitter() != null) {
                            getRCTDeviceEventEmitter().emit(NATIVE_EVENTS_TOPIC, notification);
                        } else {
                            Log.e(MODULE_NAME, "Error dispatching backgrounded notification");
                        }
                    }
                });
    }

    @Nullable
    private DeviceEventManagerModule.RCTDeviceEventEmitter getRCTDeviceEventEmitter() {
        return this.getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

}
