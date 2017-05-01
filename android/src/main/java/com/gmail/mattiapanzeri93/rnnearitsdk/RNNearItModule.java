/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.gmail.mattiapanzeri93.rnnearitsdk;

import android.app.Application;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.near.sdk.NearItManager;
import it.near.sdk.geopolis.beacons.ranging.ProximityListener;
import it.near.sdk.operation.NearItUserProfile;
import it.near.sdk.operation.ProfileCreationListener;
import it.near.sdk.operation.UserDataNotifier;
import it.near.sdk.reactions.content.Content;
import it.near.sdk.reactions.coupon.Coupon;
import it.near.sdk.reactions.customjson.CustomJSON;
import it.near.sdk.reactions.feedback.Feedback;
import it.near.sdk.reactions.feedback.FeedbackEvent;
import it.near.sdk.reactions.poll.Poll;
import it.near.sdk.reactions.simplenotification.SimpleNotification;
import it.near.sdk.recipes.NearITEventHandler;
import it.near.sdk.recipes.RecipeRefreshListener;
import it.near.sdk.recipes.models.Recipe;
import it.near.sdk.utils.CoreContentsListener;
import it.near.sdk.utils.NearUtils;

public class RNNearItModule extends ReactContextBaseJavaModule implements ProximityListener, LifecycleEventListener {

    // Module name
    private static final String MODULE_NAME = "RNNearIt";

    // Module Constants
    private static final String GEO_MESSAGE_ACTION = "GEO_MESSAGE";
    private static final String PUSH_MESSAGE_ACTION = "PUSH_MESSAGE";
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

    // Event types (used by JS to subscribe to generate events)
    private static final String EVENT_TYPE_CONTENT = "NearItContent";

    // NearIT specific
    private NearItManager nearItManager;


    public RNNearItModule(ReactApplicationContext reactContext) {
        super(reactContext);

        nearItManager = new NearItManager(reactContext, reactContext.getResources().getString(R.string.nearit_api_key));

        // calling this method on the Application onCreate is MANDATORY
        nearItManager.initLifecycleMethods((Application) reactContext.getApplicationContext());

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
                put("Actions", getActionConstants());
                put("Statuses", getStatusConstants());
            }

            private Map<String, Object> getActionConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put(GEO_MESSAGE_ACTION, NearItManager.GEO_MESSAGE_ACTION);
                        put(PUSH_MESSAGE_ACTION, NearItManager.PUSH_MESSAGE_ACTION);
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
        nearItManager.addProximityListener(this);
    }

    @Override
    public void onHostPause() {
        nearItManager.removeProximityListener(this);
    }

    @Override
    public void onHostDestroy() {

    }

    // NearIT SDK Listeners
    @Override
    public void foregroundEvent(Parcelable parcelable, Recipe recipe) {
        NearUtils.parseCoreContents(parcelable, recipe, new CoreContentsListener() {
            @Override
            public void gotPollNotification(@Nullable Intent intent, Poll poll, String s) {
                // TODO emit PollEvent
            }

            @Override
            public void gotContentNotification(@Nullable Intent intent, Content content, String s) {
                // TODO emit ContentEvent
            }

            @Override
            public void gotCouponNotification(@Nullable Intent intent, Coupon coupon, String s) {
                // TODO emit CouponEvent
            }

            @Override
            public void gotCustomJSONNotification(@Nullable Intent intent, CustomJSON customJSON, String s) {
                // TODO emit CustomJSONEvent
            }

            @Override
            public void gotSimpleNotification(@Nullable Intent intent, SimpleNotification simpleNotification, String s) {
                // TODO emit SimpleNotificationEvent
            }

            @Override
            public void gotFeedbackNotification(@Nullable Intent intent, Feedback feedback, String s) {
                // TODO emit FeedbackNotification
            }
        });
    }

    // NearIT Config
    @ReactMethod
    public void refreshConfig(final Promise promise) {
        nearItManager.refreshConfigs(new RecipeRefreshListener() {
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
            nearItManager.startRadar();
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject(E_START_RADAR_ERROR, e);
        }
    }

    @ReactMethod
    public void stopRadar(final Promise promise) {
        try {
            nearItManager.stopRadar();
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject(E_STOP_RADAR_ERROR, e);
        }
    }

    // NearIT Trackings
    @ReactMethod
    public void sendTracking(final String recipeId, final String status, final Promise promise) {
        try {
            nearItManager.getRecipesManager().sendTracking(recipeId, status);
            promise.resolve(null);
        } catch (JSONException e) {
            promise.reject(E_SEND_TRACKING_ERROR, e);
        }
    }

    // NearIT Feedbacks
    @ReactMethod
    public void sendFeedback(final String recipeId, final String feedbackId, final int rating, final String comment, final Promise promise) {

        nearItManager.sendEvent(new FeedbackEvent(feedbackId, rating, comment, recipeId), new NearITEventHandler() {
            @Override
            public void onSuccess() {
                promise.resolve(null);
            }

            @Override
            public void onFail(int errorCode, String errorMessage) {
                promise.reject(E_SEND_FEEDBACK_ERROR, String.valueOf(errorCode) + ": " + errorMessage);
            }
        });

    }

    // NearIT UserProfiling
    @ReactMethod
    public void getUserProfileId(final Promise promise) {
        final String profileId;
        try {
            profileId = NearItUserProfile.getProfileId(getReactApplicationContext());
            promise.resolve(profileId);
        } catch (Exception e) {
            promise.reject(E_USER_PROFILE_GET_ERROR, e);
        }
    }

    @ReactMethod
    public void setUserProfileId(final String profileId, final Promise promise) {
        try {
            NearItUserProfile.setProfileId(getReactApplicationContext(), profileId);
            promise.resolve(profileId);
        } catch (Exception e) {
            promise.reject(E_USER_PROFILE_SET_ERROR, e);
        }
    }

    @ReactMethod
    public void resetUserProfile(final Promise promise) {
        try {
            NearItUserProfile.resetProfileId(getReactApplicationContext());
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject(E_USER_PROFILE_RESET_ERROR, e);
        }
    }

    @ReactMethod
    public void createUserProfile(final Promise promise) {
        NearItUserProfile.createNewProfile(getReactApplicationContext(), new ProfileCreationListener() {
            @Override
            public void onProfileCreated(boolean created, String profileId) {
                promise.resolve(profileId);
            }

            @Override
            public void onProfileCreationError(String error) {
                promise.reject(E_USER_PROFILE_CREATE_ERROR, error);
            }
        });
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

        NearItUserProfile.setBatchUserData(getReactApplicationContext(), userDataMap, new UserDataNotifier() {
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
}
