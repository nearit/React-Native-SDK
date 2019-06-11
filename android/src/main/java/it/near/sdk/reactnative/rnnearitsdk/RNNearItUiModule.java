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
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.nearit.ui_bindings.NearITUIBindings;
import com.nearit.ui_bindings.NearItLaunchMode;
import com.nearit.ui_bindings.coupon.CouponListIntentBuilder;
import com.nearit.ui_bindings.inbox.NotificationHistoryIntentBuilder;
import com.nearit.ui_bindings.permissions.PermissionsRequestIntentBuilder;

import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.trackings.TrackingInfo;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.nearit.ui_bindings.utils.PermissionsUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Convert2Diamond")
public class RNNearItUiModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    
    // Module name
    private static final String MODULE_NAME = "RNNearItUI";

    private static final int RNNEARIT_PERM_REQ = 9886;
    private Promise permissionsPromise;

    RNNearItUiModule(ReactApplicationContext reactContext) {
        super(reactContext);

        reactContext.addActivityEventListener(this);
    }

    // Module definition
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
                    boolean notifications = PermissionsUtils.areNotificationsEnabled(getReactApplicationContext());

                    Map<String, Object> res = new HashMap<String,Object>();
                    res.put("location", location ? "always" : "denied");
                    res.put("notifications", notifications ? "always" : "denied");
                    res.put("bluetooth", PermissionsUtils.checkBluetooth(activity));
                    res.put("locationServices", PermissionsUtils.checkLocationServices(activity));
                    JSONObject result = new JSONObject(res);
                    permissionsPromise.resolve(result);
                } catch (Exception e) {
                    Log.e(MODULE_NAME, "NITManager :: Could handle permissions request callback", e);
                }
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // TODO:
    }

    private void showContentDialog(@NonNull Content content, @NonNull TrackingInfo trackingInfo) {
        getReactApplicationContext().startActivity(NearITUIBindings.getInstance(getReactApplicationContext()).contentIntentBuilder(content, trackingInfo).build());
    }

    private void showFeedbackDialog(@NonNull Feedback feedback) {
        getReactApplicationContext().startActivity(NearITUIBindings.getInstance(getReactApplicationContext()).feedbackIntentBuilder(feedback).build());
    }

    private void showCouponDialog(@NonNull Coupon coupon) {
        getReactApplicationContext().startActivity(NearITUIBindings.getInstance(getReactApplicationContext()).couponIntentBuilder(coupon).build());
    }

    @ReactMethod
    public void showContent() {

    }

    @ReactMethod
    public void showNotificationHistory(String activityTitle) {
        NotificationHistoryIntentBuilder builder = NearITUIBindings.getInstance(getReactApplicationContext())
            .notificationHistoryIntentBuilder()
            .includeCoupons();
        if (activityTitle != null) {
            builder.setTitle(activityTitle);
        }
        getReactApplicationContext().startActivity(builder.build());
    }

    @ReactMethod
    public void showCouponList(String activityTitle) {
        CouponListIntentBuilder builder = NearITUIBindings.getInstance(getReactApplicationContext()).couponListIntentBuilder();
        if (activityTitle != null) {
            builder.setTitle(activityTitle);
        }
        getReactApplicationContext().startActivity(builder.build());
    }

    @ReactMethod
    public void requestPermissions(final String explanation, final Promise promise) {
        permissionsPromise = promise;
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            PermissionsRequestIntentBuilder builder = NearITUIBindings.getInstance(currentActivity).permissionsIntentBuilder(NearItLaunchMode.SINGLE_TOP);
            if (explanation != null) {
                builder.setExplanation(explanation);
            }
            currentActivity.startActivityForResult(builder.build(), RNNEARIT_PERM_REQ);
        }
    }
}
