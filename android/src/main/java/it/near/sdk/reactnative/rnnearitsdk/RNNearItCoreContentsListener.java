/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 * Last changes by Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.trackings.TrackingInfo;
import it.near.sdk.utils.ContentsListener;

import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_COUPON;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_CTA;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_DATA;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_FEEDBACK;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_IMAGE;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_MESSAGE;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_QUESTION;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_TEXT;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_CONTENT_TITLE;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_FROM_USER_ACTION;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TRACKING_INFO;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_CONTENT;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_COUPON;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_CUSTOM_JSON;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_FEEDBACK;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.EVENT_TYPE_SIMPLE;
import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.NATIVE_EVENTS_TOPIC;


public class RNNearItCoreContentsListener implements ContentsListener {

    private static final String TAG = "RNNearItCoreContents";

    private Context context;
    private DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;
    private boolean fromUserAction;


    RNNearItCoreContentsListener(@NonNull Context context, @Nullable DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter, boolean fromUserAction) {
        this.context = context;
        this.eventEmitter = eventEmitter;
        this.fromUserAction = fromUserAction;
    }

    @Override
    public void gotContentNotification(Content content, TrackingInfo trackingInfo) {
        // Create EventContent map
        final WritableMap contentMap = new WritableNativeMap();
        contentMap.putString(EVENT_CONTENT_MESSAGE, content.notificationMessage);
        contentMap.putString(EVENT_CONTENT_TITLE, (content.title != null ? content.title : ""));
        contentMap.putString(EVENT_CONTENT_TEXT, (content.contentString != null ? content.contentString : ""));
        if (content.getImageLink() != null) {
            contentMap.putMap(EVENT_CONTENT_IMAGE, RNNearItUtils.bundleImageSet(content.getImageLink()));
        }
        if (content.getCta() != null) {
            contentMap.putMap(EVENT_CONTENT_CTA, RNNearItUtils.bundleContentLink(content.getCta()));
        }

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
        contentMap.putMap(EVENT_CONTENT_DATA, RNNearItUtils.bundleCustomJson(customJSON));

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
            contentMap.putString(EVENT_CONTENT_FEEDBACK, RNNearItUtils.feedbackToB64(feedback));

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
            final String trackingInfoData = RNNearItUtils.bundleTrackingInfo(trackingInfo);

            // Create Event map to send to JS
            final WritableMap eventMap = new WritableNativeMap();
            eventMap.putString(EVENT_TYPE, eventType);
            if (contentMap != null) {
                eventMap.putMap(EVENT_CONTENT, contentMap);
            }
            eventMap.putString(EVENT_TRACKING_INFO, trackingInfoData);
            eventMap.putBoolean(EVENT_FROM_USER_ACTION, fromUserAction);

            if (eventEmitter != null && RNNearItPersistedQueue.defaultQueue().hasListeners()) {
                // Send event to JS
                Log.d(TAG, "Listeners available, will send notification to JS now");
                this.eventEmitter.emit(NATIVE_EVENTS_TOPIC, eventMap);
            } else {
                // Defer event notification when at least a listener is available
                Log.d(TAG, "Listeners NOT available, will defer notification using RNNearItPersistedQueue");
                RNNearItPersistedQueue.addNotification(context, eventMap);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while sending event to JS");
        }
    }

}
