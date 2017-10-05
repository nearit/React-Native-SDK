/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
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
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.near.sdk.NearItManager;
import it.near.sdk.geopolis.beacons.ranging.ProximityListener;
import it.near.sdk.operation.UserDataNotifier;
import it.near.sdk.reactions.couponplugin.CouponListener;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.feedbackplugin.FeedbackEvent;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.recipes.NearITEventHandler;
import it.near.sdk.recipes.RecipeRefreshListener;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.NearUtils;

public class RNNearItModule extends ReactContextBaseJavaModule implements LifecycleEventListener,
        ProximityListener {

  // Module name
  static final String MODULE_NAME = "RNNearIt";

  // Event topic (used by JS to subscribe to generated events)
  static final String NATIVE_EVENTS_TOPIC = "RNNearItEvent";
  private static final String NATIVE_PERMISSIONS_TOPIC = "RNNearItPermissions";

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
  static final String EVENT_CONTENT_VIDEO = "video";
  static final String EVENT_CONTENT_IMAGES = "images";
  static final String EVENT_CONTENT_UPLOAD = "upload";
  static final String EVENT_CONTENT_AUDIO = "audio";
  static final String EVENT_CONTENT_FEEDBACK = "feedbackId";
  static final String EVENT_CONTENT_QUESTION = "feedbackQuestion";
  static final String EVENT_FROM_USER_ACTION = "fromUserAction";
  private static final String EVENT_STATUS = "status";

  // Location permission status
  private static final String PERMISSION_LOCATION_GRANTED = "NearIt.Permissions.Location.Granted";
  private static final String PERMISSION_LOCATION_DENIED = "NearIt.Permissions.Location.Denied";

  // Error codes
  private static final String E_REFRESH_CONFIG_ERROR = "E_REFRESH_CONFIG_ERROR";
  private static final String E_START_RADAR_ERROR = "E_START_RADAR_ERROR";
  private static final String E_STOP_RADAR_ERROR = "E_STOP_RADAR_ERROR";
  private static final String E_SEND_TRACKING_ERROR = "E_SEND_TRACKING_ERROR";
  private static final String E_SEND_FEEDBACK_ERROR = "E_SEND_FEEDBACK_ERROR";
  private static final String E_USER_PROFILE_GET_ERROR = "E_USER_PROFILE_GET_ERROR";
  private static final String E_USER_PROFILE_SET_ERROR = "E_USER_PROFILE_SET_ERROR";
  private static final String E_USER_PROFILE_RESET_ERROR = "E_USER_PROFILE_RESET_ERROR";
  private static final String E_USER_PROFILE_CREATE_ERROR = "E_USER_PROFILE_CREATE_ERROR";
  private static final String E_USER_PROFILE_DATA_ERROR = "E_USER_PROFILE_DATA_ERROR";
  private static final String E_COUPONS_PARSING_ERROR = "E_COUPONS_PARSING_ERROR";
  private static final String E_COUPONS_RETRIEVAL_ERROR = "E_COUPONS_RETRIEVAL_ERROR";


  public RNNearItModule(ReactApplicationContext reactContext) {
    super(reactContext);

    // Register LocalBroadcastReceiver to be used for Foreground notification handling
    final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(reactContext);
    localBroadcastManager.registerReceiver(new LocalBroadcastReceiver(), new IntentFilter(LOCAL_EVENTS_TOPIC));

    // Listen for Resume, Pause, Destroy events
    getReactApplicationContext().addLifecycleEventListener(this);
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
            put("text", EVENT_CONTENT_TEXT);
            put("images", EVENT_CONTENT_IMAGES);
            put("video", EVENT_CONTENT_VIDEO);
            put("upload", EVENT_CONTENT_UPLOAD);
            put("audio", EVENT_CONTENT_AUDIO);
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
            put("notified", Recipe.NOTIFIED_STATUS);
            put("engaged", Recipe.ENGAGED_STATUS);
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

  // ReactApp Lifecycle methods
  @Override
  public void onHostResume() {
    NearItManager.getInstance().addProximityListener(this);
  }

  @Override
  public void onHostPause() {
    RNNearItPersistedQueue.defaultQueue().resetListeners();
    NearItManager.getInstance().removeProximityListener(this);
  }

  @Override
  public void onHostDestroy() {
  }

  // ReactNative listeners management
  @ReactMethod
  public void listenerRegistered(final Promise promise) {
    final int listenersCount = RNNearItPersistedQueue.defaultQueue().registerListener();
    Log.i(MODULE_NAME, String.format("listenerRegistered (Registered listeners: %d)", listenersCount));
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
    promise.resolve(true);
  }

  @ReactMethod
  public void listenerUnregistered(final Promise promise) {
    final int listenersCount = RNNearItPersistedQueue.defaultQueue().unregisterListener();
    Log.d(MODULE_NAME, String.format("listenerUnregistered (Registered listeners: %d)", listenersCount));
    promise.resolve(true);
  }

  // NearIT SDK Listeners
  @Override
  public void foregroundEvent(Parcelable parcelable, TrackingInfo trackingInfo) {
    NearUtils.parseCoreContents(parcelable, trackingInfo, new RNNearItCoreContentsListener(getReactApplicationContext(), getRCTDeviceEventEmitter(), false));
  }

  // NearIT Config
  @ReactMethod
  public void refreshConfig(final Promise promise) {
    NearItManager.getInstance().refreshConfigs(new RecipeRefreshListener() {
      @Override
      public void onRecipesRefresh() {
        promise.resolve(null);
      }

      @Override
      public void onRecipesRefreshFail() {
        promise.reject(E_REFRESH_CONFIG_ERROR, "refreshConfig failed");
      }
    });
  }

  // NearIT Radar
  @ReactMethod
  public void startRadar(final Promise promise) {
    try {
      NearItManager.getInstance().startRadar();
      promise.resolve(null);
    } catch (Exception e) {
      promise.reject(E_START_RADAR_ERROR, e);
    }
  }

  @ReactMethod
  public void stopRadar(final Promise promise) {
    try {
      NearItManager.getInstance().stopRadar();
      promise.resolve(null);
    } catch (Exception e) {
      promise.reject(E_STOP_RADAR_ERROR, e);
    }
  }

  // NearIT Trackings
  @ReactMethod
  public void sendTracking(final String trackinInfoData, final String status, final Promise promise) {
    try {
      NearItManager.getInstance().getRecipesManager()
              .sendTracking(RNNearItUtils.trackingInfoFromBase64(trackinInfoData), status);

      promise.resolve(null);
    } catch (Exception e) {
      promise.reject(E_SEND_TRACKING_ERROR, e);
    }
  }

  // NearIT Feedback
  @ReactMethod
  public void sendFeedback(final String feedbackB64, final int rating, final String comment, final Promise promise) {
    final Feedback feedback;
    try {
      feedback = RNNearItUtils.feedbackFromBase64(feedbackB64);

      NearItManager.getInstance().sendEvent(new FeedbackEvent(feedback, rating, comment), new NearITEventHandler() {
        @Override
        public void onSuccess() {
          promise.resolve(null);
        }

        @Override
        public void onFail(int errorCode, String errorMessage) {
          promise.reject(E_SEND_FEEDBACK_ERROR, "Failed to send feedback to NearIT");
        }
      });
    } catch (Exception e) {
      promise.reject(E_SEND_FEEDBACK_ERROR, "Failed to encode feedback to be sent", e);
    }
  }

  // NearIT UserProfiling
  @ReactMethod
  public void getUserProfileId(final Promise promise) {
    final String profileId;
    try {
      profileId = NearItManager.getInstance().getProfileId();
      promise.resolve(profileId);
    } catch (Exception e) {
      promise.reject(E_USER_PROFILE_GET_ERROR, e);
    }
  }

  @ReactMethod
  public void setUserProfileId(final String profileId, final Promise promise) {
    try {
      NearItManager.getInstance().setProfileId(profileId);
      promise.resolve(profileId);
    } catch (Exception e) {
      promise.reject(E_USER_PROFILE_SET_ERROR, e);
    }
  }

  @ReactMethod
  public void resetUserProfile(final Promise promise) {
    try {
      NearItManager.getInstance().resetProfileId();
      promise.resolve(null);
    } catch (Exception e) {
      promise.reject(E_USER_PROFILE_RESET_ERROR, e);
    }
  }

  @ReactMethod
  public void setUserData(final ReadableMap userData, final Promise promise) {
    final HashMap<String, String> userDataMap = new HashMap<>();

    while (userData.keySetIterator().hasNextKey()) {
      final String k = userData.keySetIterator().nextKey();
      userDataMap.put(k, String.valueOf(userData.getDynamic(k)));
    }

    NearItManager.getInstance().setBatchUserData(userDataMap, new UserDataNotifier() {
      @Override
      public void onDataCreated() {
        promise.resolve(null);
      }

      @Override
      public void onDataNotSetError(String error) {
        promise.reject(E_USER_PROFILE_DATA_ERROR, error);
      }
    });
  }

  // NearIT Permissions request

  @ReactMethod
  public void requestNotificationPermission(final Promise promise) {
    promise.resolve(true);
  }

  @ReactMethod
  public void requestLocationPermission(final Promise promise) {
    promise.resolve(true);
  }

  // NearIT Coupons

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

  // Private methods
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
        NearUtils.parseCoreContents(intent, new RNNearItCoreContentsListener(getReactApplicationContext(), getRCTDeviceEventEmitter(), false));
      }
    }
  }
}
