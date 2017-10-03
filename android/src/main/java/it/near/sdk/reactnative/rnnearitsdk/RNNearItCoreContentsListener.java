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

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.Gson;

import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.CoreContentsListener;

import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_AUDIO;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_COUPON;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_DATA;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_FEEDBACK;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_IMAGES;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_MESSAGE;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_QUESTION;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_TEXT;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_UPLOAD;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_VIDEO;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_FROM_USER_ACTION;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TRACKING_INFO;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_CONTENT;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_COUPON;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_CUSTOM_JSON;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_FEEDBACK;
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
    // Create EventContent map
    final WritableMap contentMap = new WritableNativeMap();
    contentMap.putString(EVENT_CONTENT_MESSAGE, content.notificationMessage);
    contentMap.putString(EVENT_CONTENT_TEXT, (content.contentString != null ? content.contentString : ""));
    contentMap.putString(EVENT_CONTENT_VIDEO, (content.video_link != null ? content.video_link : ""));

    final WritableArray images = new WritableNativeArray();
    for (ImageSet i : content.getImages_links()) {
      images.pushMap(RNNearItUtils.bundleImageSet(i));
    }
    contentMap.putArray(EVENT_CONTENT_IMAGES, images);

    contentMap.putString(EVENT_CONTENT_UPLOAD, (content.upload != null ? content.upload.getUrl() : ""));
    contentMap.putString(EVENT_CONTENT_AUDIO, (content.audio != null ? content.audio.getUrl() : ""));

    // Notify JS
    sendEventWithContent(EVENT_TYPE_CONTENT, contentMap, trackingInfo);
  }

  @Override
  public void gotCouponNotification(Coupon coupon, TrackingInfo trackingInfo) {
    // Create EventContent map
    final WritableMap contentMap = new WritableNativeMap();
    contentMap.putString(EVENT_CONTENT_MESSAGE, coupon.notificationMessage);
    contentMap.putMap(EVENT_CONTENT_COUPON, RNNearItUtils.bundleCoupon(coupon));

    // Notify JS
    sendEventWithContent(EVENT_TYPE_COUPON, contentMap, trackingInfo);
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
    contentMap.putString(EVENT_CONTENT_MESSAGE, simpleNotification.notificationMessage);

    // Notify JS
    sendEventWithContent(EVENT_TYPE_SIMPLE, contentMap, trackingInfo);
  }

  @Override
  public void gotFeedbackNotification(Feedback feedback, TrackingInfo trackingInfo) {
    try {
      // Create EventContent map
      final WritableMap contentMap = new WritableNativeMap();
      contentMap.putString(EVENT_CONTENT_MESSAGE, feedback.notificationMessage);
      contentMap.putString(EVENT_CONTENT_QUESTION, feedback.question);
      contentMap.putString(EVENT_CONTENT_FEEDBACK, RNNearItUtils.feedbackToBase64(feedback));

      // Notify JS
      sendEventWithContent(EVENT_TYPE_FEEDBACK, contentMap, trackingInfo);
    } catch (Exception e) {
      Log.e(TAG, "Error while encoding feedback event", e);
    }
  }

  // Private methods
  private void sendEventWithContent(final String eventType, @Nullable WritableMap contentMap, final TrackingInfo trackingInfo) {
    Log.d(TAG, String.format("sendEventWithContent: (eventType: %s)", eventType));
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

      if (RNNearItBackgroundQueue.defaultQueue().hasListeners()) {
        // Send event to JS
        Log.d(TAG, "Listeners available, will send notification to JS now");
        this.eventEmitter.emit(NATIVE_EVENTS_TOPIC, eventMap);
      } else {
        // Defer event notification when at least a listener is available
        Log.d(TAG, "Listeners NOT available, will defer notification using RNNearItBackgroundQueue");
        RNNearItBackgroundQueue.defaultQueue().addNotification(eventMap);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error while sending event to JS");
    }
  }

}
