/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.near.sdk.NearItManager;
import it.near.sdk.geopolis.beacons.ranging.ProximityListener;
import it.near.sdk.operation.UserDataNotifier;
import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.recipes.RecipeRefreshListener;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.CoreContentsListener;
import it.near.sdk.utils.NearUtils;

public class RNNearItModule extends ReactContextBaseJavaModule implements ProximityListener, LifecycleEventListener {

  // Module name
  private static final String MODULE_NAME = "RNNearIt";

  // Module Constants
  private static final String EVENT_SIMPLE = "NearIt.Events.SimpleNotification";
  private static final String EVENT_CUSTOM_JSON = "NearIt.Events.CustomJSON";
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

  // Event types (used by JS to subscribe to generated events)
  private static final String EVENT_TYPE_CONTENT = "NearItContent";


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
        put("Events", getEventsConstants());
        put("Statuses", getStatusConstants());
      }

      private Map<String, Object> getEventsConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("SimpleNotification", EVENT_SIMPLE);
            put("CustomJson", EVENT_CUSTOM_JSON);
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
    NearUtils.parseCoreContents(parcelable, trackingInfo, new CoreContentsListener() {

      @Override
      public void gotContentNotification(Content content, TrackingInfo trackingInfo) {
        // TODO emit ContentEvent
      }

      @Override
      public void gotCouponNotification(Coupon coupon, TrackingInfo trackingInfo) {
        // TODO emit CouponEvent
      }

      @Override
      public void gotCustomJSONNotification(CustomJSON customJSON, TrackingInfo trackingInfo) {
        // TODO emit CustomJSONEvent
      }

      @Override
      public void gotSimpleNotification(SimpleNotification simpleNotification, TrackingInfo trackingInfo) {
        // TODO emit SimpleNotificationEvent
      }

      @Override
      public void gotFeedbackNotification(Feedback feedback, TrackingInfo trackingInfo) {
        // TODO emit FeedbackNotification
      }
    });
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
      NearItManager.getInstance().getRecipesManager().sendTracking(trackingInfoFromBase64(trackinInfoData), status);
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

  // Private methods
  private void sendContentEvent(@Nullable WritableMap params) {
    this.getReactApplicationContext()
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(EVENT_TYPE_CONTENT, params);
  }


  // Utils
  static String trackingInfoToBase64(final TrackingInfo trackingInfo) throws Exception {
    // JSONify trackingInfo
    final String trackingInfoJson = new Gson().toJson(trackingInfo);

    // Encode to base64
    return Base64.encodeToString(trackingInfoJson.getBytes("UTF-8"), Base64.DEFAULT);
  }

  static TrackingInfo trackingInfoFromBase64(final String trackingInfoBase64) throws Exception {
    // Decode from base64
    final String trackingInfoJsonString = new String(Base64.decode(trackingInfoBase64, Base64.DEFAULT), "UTF-8");

    // DeJSONify trackingInfo
    return new Gson().fromJson(trackingInfoJsonString, TrackingInfo.class);
  }
}
