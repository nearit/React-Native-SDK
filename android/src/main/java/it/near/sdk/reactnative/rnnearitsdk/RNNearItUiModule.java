/*
 * Copyright (c) 2019 Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

 package it.near.sdk.reactnative.rnnearitsdk;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.nearit.ui_bindings.NearITUIBindings;
import com.nearit.ui_bindings.NearItLaunchMode;
import com.nearit.ui_bindings.coupon.CouponListIntentBuilder;
import com.nearit.ui_bindings.inbox.NotificationHistoryIntentBuilder;
import com.nearit.ui_bindings.permissions.PermissionsRequestIntentBuilder;
import com.nearit.ui_bindings.utils.PermissionsUtils;

import java.util.HashMap;
import java.util.Map;

import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.trackings.TrackingInfo;

import static it.near.sdk.reactnative.rnnearitsdk.RNNearItConstants.*;

@SuppressWarnings("Convert2Diamond")
public class RNNearItUiModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final String MODULE_NAME = "RNNearItUI";

    private static final int RNNEARIT_PERM_REQ = 9886;
    private Promise permissionsPromise;

    RNNearItUiModule(ReactApplicationContext reactContext) {
        super(reactContext);

        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == RNNEARIT_PERM_REQ) {
            if (permissionsPromise != null) {
                try {
                    boolean location = PermissionsUtils.checkLocationPermission(getReactApplicationContext());

                    Map<String, Object> res = new HashMap<String,Object>();
                    res.put(PERMISSIONS_LOCATION_PERMISSION, location ? PERMISSIONS_LOCATION_ALWAYS : PERMISSIONS_LOCATION_DENIED);
                    res.put(PERMISSIONS_NOTIFICATIONS_PERMISSION, PermissionsUtils.areNotificationsEnabled(getReactApplicationContext()));
                    res.put(PERMISSIONS_BLUETOOTH, PermissionsUtils.checkBluetooth(getReactApplicationContext()));
                    res.put(PERMISSIONS_LOCATION_SERVICES, PermissionsUtils.checkLocationServices(getReactApplicationContext()));
                    permissionsPromise.resolve(RNNearItUtils.toWritableMap(res));
                } catch (Exception e) {
                    Log.e(MODULE_NAME, "NITManager :: Could NOT handle permissions request callback", e);
                }
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @SuppressWarnings({"IfCanBeSwitch", "unused"})
    @ReactMethod
    public void showContent(@Nullable ReadableMap event) {
        if (event != null) {
            final String type = event.getString(EVENT_TYPE);
            if (type != null) {
                if (type.equals(EVENT_TYPE_CONTENT)) {
                    Content nearContent = RNNearItUtils.unbundleContent(event.getMap(EVENT_CONTENT));
                    TrackingInfo trackingInfo = RNNearItUtils.unbundleTrackingInfo(event.getString(EVENT_TRACKING_INFO));
                    showContentDialog(nearContent, trackingInfo);
                } else if (type.equals(EVENT_TYPE_FEEDBACK)) {
                    Feedback feedback = RNNearItUtils.unbundleFeedback(event.getMap(EVENT_CONTENT));
                    if (feedback != null) {
                        showFeedbackDialog(feedback);
                    } else {
                        Log.e(MODULE_NAME, "Could NOT parse feedback");
                    }
                } else if (type.equals(EVENT_TYPE_COUPON)) {
                    Coupon coupon = RNNearItUtils.unbundleCoupon(event.getMap(EVENT_CONTENT));
                    showCouponDialog(coupon);
                } else if (type.equals(EVENT_TYPE_CUSTOM_JSON)) {
                    Log.i(MODULE_NAME, "Could NOT show content because it is a custom json");
                } else if (type.equals(EVENT_TYPE_SIMPLE)) {
                    Log.i(MODULE_NAME, "Could NOT show content because it is a simple notification");
                } else {
                    Log.e(MODULE_NAME, "Could NOT show content because content can\'t be parsed");
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void showNotificationHistory(String activityTitle) {
        NotificationHistoryIntentBuilder builder = NearITUIBindings.getInstance(getReactApplicationContext())
            .notificationHistoryIntentBuilder()
            .includeCoupons();
        if (activityTitle != null) {
            builder.setTitle(activityTitle);
        }
        getReactApplicationContext().startActivity(builder.build().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void showCouponList(String activityTitle) {
        CouponListIntentBuilder builder = NearITUIBindings.getInstance(getReactApplicationContext()).couponListIntentBuilder();
        builder.jaggedBorders();
        if (activityTitle != null) {
            builder.setTitle(activityTitle);
        }
        getReactApplicationContext().startActivity(builder.build().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void requestPermissions(final String explanation, final Promise promise) {
        permissionsPromise = promise;
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            PermissionsRequestIntentBuilder builder = NearITUIBindings.getInstance(currentActivity).permissionsIntentBuilder(NearItLaunchMode.SINGLE_TOP);
            if (explanation != null) {
                builder.setExplanation(explanation);
            }
            getReactApplicationContext().startActivityForResult(builder.build(), RNNEARIT_PERM_REQ, null);
        }
    }

    private void showContentDialog(@NonNull Content content, TrackingInfo trackingInfo) {
        getReactApplicationContext()
                .startActivity(
                        NearITUIBindings.getInstance(getReactApplicationContext())
                                .contentIntentBuilder(content, trackingInfo)
                                .build()
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                );
    }

    private void showFeedbackDialog(@NonNull Feedback feedback) {
        getReactApplicationContext()
                .startActivity(
                        NearITUIBindings.getInstance(getReactApplicationContext())
                                .feedbackIntentBuilder(feedback)
                                .build()
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                );
    }

    private void showCouponDialog(@NonNull Coupon coupon) {
        getReactApplicationContext()
                .startActivity(
                        NearITUIBindings.getInstance(getReactApplicationContext())
                                .couponIntentBuilder(coupon)
                                .build()
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                );
    }
}
