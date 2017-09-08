/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.near.sdk.NearItManager;
import it.near.sdk.geopolis.beacons.ranging.ProximityListener;
import it.near.sdk.operation.UserDataNotifier;
import it.near.sdk.recipes.RecipeRefreshListener;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.NearUtils;

public class RNNearItModule extends ReactContextBaseJavaModule implements ActivityEventListener,
        LifecycleEventListener,
        ProximityListener {

  // Module name
  private static final String MODULE_NAME = "RNNearIt";

  // Event topic (used by JS to subscribe to generated events)
  public static final String NATIVE_EVENTS_TOPIC = "RNNearItEvent";

  // Local Events topic (used by LocalBroadcastReceiver to handle foreground notifications)
  public static final String LOCAL_EVENTS_TOPIC = "RNNearItLocalEvents";

  // Module Constants
  // Events type
  public static final String EVENT_TYPE_SIMPLE = "NearIt.Events.SimpleNotification";
  public static final String EVENT_TYPE_CUSTOM_JSON = "NearIt.Events.CustomJSON";

  // Events content
  public static final String EVENT_TYPE = "type";
  public static final String EVENT_TRACKING_INFO = "trackingInfo";
  public static final String EVENT_CONTENT = "content";
  public static final String EVENT_CONTENT_MESSAGE = "message";
  public static final String EVENT_CONTENT_DATA = "data";
  public static final String EVENT_FROM_USER_ACTION = "fromUserAction";

  // Recipe Statuses
  private static final String RECIPE_STATUS_ENGAGED = "RECIPE_STATUS_ENGAGED";
  private static final String RECIPE_STATUS_NOTIFIED = "RECIPE_STATUS_NOTIFIED";

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


  public RNNearItModule(ReactApplicationContext reactContext) {
    super(reactContext);

    // Listen for onNewIntent event
    reactContext.addActivityEventListener(this);

    // Listen for Resume, Pause, Destroy events
    reactContext.addLifecycleEventListener(this);

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
        put("Events", getEventsConstants());
        put("EventContent", getEventContentConstants());
        put("Statuses", getStatusConstants());
      }

      private Map<String, Object> getEventsConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("SimpleNotification", EVENT_TYPE_SIMPLE);
            put("CustomJson", EVENT_TYPE_CUSTOM_JSON);
          }
        });
      }

      private Map<String, Object> getEventContentConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put(EVENT_TYPE, EVENT_TYPE);
            put(EVENT_TRACKING_INFO, EVENT_TRACKING_INFO);
            put(EVENT_CONTENT, EVENT_CONTENT);
            put(EVENT_CONTENT_MESSAGE, EVENT_CONTENT_MESSAGE);
            put(EVENT_CONTENT_DATA, EVENT_CONTENT_DATA);
          }
        });
      }

      private Map<String, Object> getStatusConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put(RECIPE_STATUS_ENGAGED, Recipe.ENGAGED_STATUS);
            put(RECIPE_STATUS_NOTIFIED, Recipe.NOTIFIED_STATUS);
          }
        });
      }
    });
  }

  // ReactApp Activity methods
  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    // Empty
  }

  @Override
  public void onNewIntent(Intent intent) {
    if (intent != null && NearUtils.carriesNearItContent(intent)) {
      // we got a NearIT intent
      // coming from a notification tap
      NearUtils.parseCoreContents(intent, new RNNearItCoreContentsListener(getRCTDeviceEventEmitter(), true));
    }
  }

  // ReactApp Lifecycle methods
  @Override
  public void onHostResume() {
    NearItManager.getInstance().addProximityListener(this);
  }

  @Override
  public void onHostPause() {
    NearItManager.getInstance().removeProximityListener(this);
  }

  @Override
  public void onHostDestroy() {
  }

  // NearIT SDK Listeners
  @Override
  public void foregroundEvent(Parcelable parcelable, TrackingInfo trackingInfo) {
    NearUtils.parseCoreContents(parcelable, trackingInfo, new RNNearItCoreContentsListener(getRCTDeviceEventEmitter(), false));
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

  // NearIT Feedbacks
  /*@ReactMethod
  public void sendFeedback(final String feedbackId, final int rating, final String comment, final Promise promise) {
    NearItManager.getInstance().sendEvent(new FeedbackEvent(feedbackId, rating, comment), new NearITEventHandler() {
      @Override
      public void onSuccess() {
        promise.resolve(null);
      }

      @Override
      public void onFail(int errorCode, String errorMessage) {
        promise.reject(E_SEND_FEEDBACK_ERROR, String.valueOf(errorCode) + ": " + errorMessage);
      }
    });
  }*/

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
      final ReadableType t = userData.getType(k);

      if (t == ReadableType.Number) {
        userDataMap.put(k, String.valueOf(userData.getDouble(k)));
      } else if (t == ReadableType.Boolean) {
        userDataMap.put(k, String.valueOf(userData.getBoolean(k)));
      } else if (t == ReadableType.String) {
        userDataMap.put(k, userData.getString(k));
      }
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

  // Private methods
  private DeviceEventManagerModule.RCTDeviceEventEmitter getRCTDeviceEventEmitter() {
    return this.getReactApplicationContext()
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
  }

  // LocalBroadcastReceiver
  public class LocalBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent != null && NearUtils.carriesNearItContent(intent)) {
        NearUtils.parseCoreContents(intent, new RNNearItCoreContentsListener(getRCTDeviceEventEmitter(), false));
      }
    }
  }
}
