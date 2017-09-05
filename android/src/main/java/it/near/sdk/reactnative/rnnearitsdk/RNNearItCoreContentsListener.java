/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.Gson;

import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.CoreContentsListener;

import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_DATA;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_MESSAGE;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_FROM_USER_ACTION;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TRACKING_INFO;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_CUSTOM_JSON;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_SIMPLE;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.NATIVE_EVENTS_TOPIC;


public class RNNearItCoreContentsListener implements CoreContentsListener {

  private static final String TAG = "RNNearItCoreContents";

  private final DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;
  private final boolean fromUserAction;

  RNNearItCoreContentsListener(DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter, boolean fromUserAction) {
    this.eventEmitter = eventEmitter;
    this.fromUserAction = fromUserAction;
  }

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
    // Create EventContent map
    final WritableMap contentMap = new WritableNativeMap();
    contentMap.putString(EVENT_CONTENT_MESSAGE, customJSON.notificationMessage);
    contentMap.putString(EVENT_CONTENT_DATA, new Gson().toJson(customJSON.content));

    // Notify JS
    sendEventWithContent(EVENT_TYPE_CUSTOM_JSON, contentMap, trackingInfo);
  }

  @Override
  public void gotSimpleNotification(SimpleNotification simpleNotification, TrackingInfo trackingInfo) {
    // Create EventContent map
    final WritableMap contentMap = new WritableNativeMap();
    contentMap.putString(EVENT_CONTENT_MESSAGE, simpleNotification.message);

    // Notify JS
    sendEventWithContent(EVENT_TYPE_SIMPLE, contentMap, trackingInfo);
  }

  @Override
  public void gotFeedbackNotification(Feedback feedback, TrackingInfo trackingInfo) {
    // TODO emit FeedbackNotification
  }

  // Private methods
  private void sendEventWithContent(final String eventType, @Nullable WritableMap contentMap, final TrackingInfo trackingInfo) {
    try {
      // Encode TrackingInfo
      final String trackingInfoData = RNNearItUtils.trackingInfoToBase64(trackingInfo);

      // Create Event map to send to JS
      final WritableMap eventMap = new WritableNativeMap();
      eventMap.putString(EVENT_TYPE, eventType);
      if (contentMap != null) {
        eventMap.putMap(EVENT_CONTENT, contentMap);
      }
      eventMap.putString(EVENT_TRACKING_INFO, trackingInfoData);
      eventMap.putBoolean(EVENT_FROM_USER_ACTION, fromUserAction);

      // Send event to JS
      this.eventEmitter.emit(NATIVE_EVENTS_TOPIC, eventMap);
    } catch (Exception e) {
      Log.e(TAG, "Error while sending event to JS");
    }
  }

}
