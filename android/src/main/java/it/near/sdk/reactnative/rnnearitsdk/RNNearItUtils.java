/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 * Last changes by Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import it.near.sdk.reactions.contentplugin.model.Content;
import it.near.sdk.reactions.contentplugin.model.ContentLink;
import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.couponplugin.model.Claim;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.customjsonplugin.model.CustomJSON;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.reactions.simplenotificationplugin.model.SimpleNotification;
import it.near.sdk.recipes.inbox.model.HistoryItem;
import it.near.sdk.trackings.TrackingInfo;

import static it.near.sdk.reactnative.rnnearitsdk.RNNearItConstants.*;

@SuppressWarnings({"CharsetObjectCanBeUsed", "Convert2Diamond"})
class RNNearItUtils {

    private static final String TAG = "RNNearItUtils";

    static String bundleTrackingInfo(final TrackingInfo trackingInfo) throws Exception {
        // JSONify trackingInfo
        final String trackingInfoJson = new Gson().toJson(trackingInfo);

        // Encode to base64
        return Base64.encodeToString(trackingInfoJson.getBytes("UTF-8"), Base64.DEFAULT);
    }

    @Nullable
    static TrackingInfo unbundleTrackingInfo(final String trackingInfoBase64) {
        // Decode from base64
        try {
            final String trackingInfoJsonString = new String(Base64.decode(trackingInfoBase64, Base64.DEFAULT), "UTF-8");
            return new Gson().fromJson(trackingInfoJsonString, TrackingInfo.class);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "error while decoding trackingInfo");
        }

        return null;
    }

    static WritableMap bundleCoupon(final Coupon coupon) {
        final WritableMap couponMap = new WritableNativeMap();

        couponMap.putString(EVENT_CONTENT_TITLE, coupon.getTitle());
        couponMap.putString(EVENT_COUPON_DESCRIPTION, coupon.description);
        couponMap.putString(EVENT_COUPON_VALUE, coupon.value);
        couponMap.putString(EVENT_COUPON_EXPIRES_AT, coupon.expires_at);
        couponMap.putString(EVENT_COUPON_REDEEMABLE_FROM, coupon.redeemable_from);
        couponMap.putString(EVENT_COUPON_SERIAL, coupon.getSerial());
        couponMap.putString(EVENT_COUPON_CLAIMED_AT, coupon.getClaimedAt());
        couponMap.putString(EVENT_COUPON_REDEEMED_AT, coupon.getRedeemedAt());

        // Coupon icon handling
        if (coupon.getIconSet() != null) {
            couponMap.putMap(EVENT_IMAGE, bundleImageSet(coupon.getIconSet()));
        }

        return couponMap;
    }

    static Coupon unbundleCoupon(final ReadableMap bundledCoupon) {
        Coupon coupon = new Coupon();
        coupon.name = getNullableField(bundledCoupon, EVENT_CONTENT_TITLE);
        coupon.description = getNullableField(bundledCoupon, EVENT_COUPON_DESCRIPTION);
        coupon.value = getNullableField(bundledCoupon, EVENT_COUPON_VALUE);
        coupon.expires_at = getNullableField(bundledCoupon, EVENT_COUPON_EXPIRES_AT);
        coupon.redeemable_from = getNullableField(bundledCoupon, EVENT_COUPON_REDEEMABLE_FROM);
        List<Claim> claims = new ArrayList<Claim>();
        Claim claim = new Claim();
        claim.serial_number = getNullableField(bundledCoupon, EVENT_COUPON_SERIAL);
        claim.claimed_at = getNullableField(bundledCoupon, EVENT_COUPON_CLAIMED_AT);
        claim.redeemed_at = getNullableField(bundledCoupon, EVENT_COUPON_REDEEMED_AT);
        claims.add(claim);
        coupon.claims = claims;
        if (bundledCoupon.hasKey(EVENT_IMAGE)) {
            ReadableMap imageSet = bundledCoupon.getMap(EVENT_IMAGE);
            coupon.setIconSet(unbundleImageSet(imageSet));
        }
        return coupon;
    }

    static WritableMap bundleContent(final Content content) {
        final WritableMap bundledContent = new WritableNativeMap();

        bundledContent.putString(EVENT_CONTENT_TITLE, content.title);
        bundledContent.putString(EVENT_CONTENT_TEXT, content.contentString);

        if (content.getImageLink() != null) {
            bundledContent.putMap(EVENT_IMAGE, bundleImageSet(content.getImageLink()));
        }

        if (content.getCta() != null) {
            bundledContent.putMap(EVENT_CONTENT_CTA, bundleContentLink(content.getCta()));
        }

        return bundledContent;
    }

    static Content unbundleContent(final ReadableMap bundledContent) {
        final Content content = new Content();
        content.title = getNullableField(bundledContent, EVENT_CONTENT_TITLE);
        content.contentString = getNullableField(bundledContent, EVENT_CONTENT_TEXT);
        if (bundledContent.hasKey(EVENT_IMAGE)) {
            final List<ImageSet> images = new ArrayList<ImageSet>();
            ReadableMap imageSet = bundledContent.getMap(EVENT_IMAGE);
            images.add(unbundleImageSet(imageSet));
            content.setImages_links(images);
        }
        if (bundledContent.hasKey(EVENT_CONTENT_CTA)) {
            ReadableMap bundledCta = bundledContent.getMap(EVENT_CONTENT_CTA);
            if (bundledCta != null) {
                content.setCta(unbundleContentLink(bundledCta));
            }
        }
        return content;
    }

    static WritableArray bundleNotificationHistory(final List<HistoryItem> history) {
        final WritableArray bundledHistory = new WritableNativeArray();

        for (HistoryItem item : history) {
            final WritableMap bundledItem = bundleHistoryItem(item);
            bundledHistory.pushMap(bundledItem);
        }

        return bundledHistory;
    }

    static WritableMap bundleFeedback(final Feedback feedback) {
        final WritableMap bundledFeedback = new WritableNativeMap();

        bundledFeedback.putString(EVENT_FEEDBACK_QUESTION, feedback.question);

        String feedbackB64 = null;
        try {
            feedbackB64 = feedbackToB64(feedback);
        } catch (Exception e) {
            Log.e(TAG, "feedback encoding error", e);
        }
        if (feedbackB64 != null) {
            bundledFeedback.putString(EVENT_FEEDBACK_ID, feedbackB64);
        }

        return bundledFeedback;
    }

    @Nullable
    static Feedback unbundleFeedback(final ReadableMap bundledFeedback) {
        Feedback feedback = null;
        if (bundledFeedback.hasKey(EVENT_FEEDBACK_ID)) {
            try {
                feedback = feedbackFromB64(bundledFeedback.getString(EVENT_FEEDBACK_ID));
            } catch (Exception e) {
                Log.e(TAG, "Could NOT parse Feedback");
            }
        } else {
            Log.e(TAG, "Could NOT parse Feedback");
        }
        return feedback;
    }

    static WritableMap bundleCustomJson(final CustomJSON customJson) {
        return toWritableMap(customJson.content);
    }

    private static WritableMap bundleHistoryItem(final HistoryItem item) {
        final WritableMap itemMap = new WritableNativeMap();

        itemMap.putBoolean(NOTIFICATION_HISTORY_READ, item.read);
        itemMap.putDouble(NOTIFICATION_HISTORY_TIMESTAMP, item.timestamp);
        itemMap.putBoolean(NOTIFICATION_HISTORY_IS_NEW, item.isNew);

        try {
            itemMap.putString(EVENT_TRACKING_INFO, bundleTrackingInfo(item.trackingInfo));
        } catch (Exception e) {
            Log.e(TAG, "historyItem encoding error", e);
        }
        itemMap.putString(EVENT_CONTENT_MESSAGE, item.reaction.notificationMessage);

        if (item.reaction instanceof SimpleNotification) {
            itemMap.putString(EVENT_TYPE, EVENT_TYPE_SIMPLE);
        } else if (item.reaction instanceof Content) {
            Content content = (Content) item.reaction;
            itemMap.putMap(NOTIFICATION_HISTORY_CONTENT, bundleContent(content));
            itemMap.putString(EVENT_TYPE, EVENT_TYPE_CONTENT);
        } else if (item.reaction instanceof Feedback) {
            Feedback feedback = (Feedback) item.reaction;
            itemMap.putMap(NOTIFICATION_HISTORY_CONTENT, bundleFeedback(feedback));
            itemMap.putString(EVENT_TYPE, EVENT_TYPE_FEEDBACK);
        } else if (item.reaction instanceof Coupon) {
            Coupon coupon = (Coupon) item.reaction;
            itemMap.putMap(NOTIFICATION_HISTORY_CONTENT, bundleCoupon(coupon));
            itemMap.putString(EVENT_TYPE, EVENT_TYPE_COUPON);
        } else if (item.reaction instanceof CustomJSON) {
            CustomJSON customJson = (CustomJSON) item.reaction;
            itemMap.putMap(NOTIFICATION_HISTORY_CONTENT, bundleCustomJson(customJson));
            itemMap.putString(EVENT_TYPE, EVENT_TYPE_CUSTOM_JSON);
        }

        return itemMap;
    }

    private static WritableMap bundleImageSet(final ImageSet imageSet) {
        final WritableMap image = new WritableNativeMap();
        image.putString(EVENT_IMAGE_FULL_SIZE, imageSet.getFullSize());
        image.putString(EVENT_IMAGE_SQUARE_SIZE, imageSet.getSmallSize());

        return image;
    }

    private static ImageSet unbundleImageSet(final ReadableMap bundledImage) {
        final ImageSet imageSet = new ImageSet();
        imageSet.setFullSize(getNullableField(bundledImage, EVENT_IMAGE_FULL_SIZE));
        imageSet.setSmallSize(getNullableField(bundledImage, EVENT_IMAGE_SQUARE_SIZE));
        return imageSet;
    }

    private static WritableMap bundleContentLink(final ContentLink cta) {
        final WritableMap contentLink = new WritableNativeMap();
        contentLink.putString(EVENT_CONTENT_CTA_LABEL, cta.label);
        contentLink.putString(EVENT_CONTENT_CTA_URL, cta.url);
        return contentLink;
    }

    private static ContentLink unbundleContentLink(final ReadableMap bundledCta) {
        return new ContentLink(
                getNullableField(bundledCta, EVENT_CONTENT_CTA_LABEL),
                getNullableField(bundledCta, EVENT_CONTENT_CTA_URL));
    }

    private static String getNullableField(final ReadableMap map, final String key) {
        if (map.hasKey(key) && map.getString(key) != null) {
            return map.getString(key);
        }
        return null;
    }

    private static Feedback feedbackFromB64(final String base64) throws Exception {
        Feedback feedback;
        final Parcel parcel = Parcel.obtain();
        try {
            final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            final GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(base64, 0)));
            int len;
            while ((len = zis.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            zis.close();
            parcel.unmarshall(byteBuffer.toByteArray(), 0, byteBuffer.size());
            parcel.setDataPosition(0);

            feedback = Feedback.CREATOR.createFromParcel(parcel);
        } finally {
            parcel.recycle();
        }

        return feedback;
    }

    private static String feedbackToB64(final Feedback feedback) throws Exception {
        String base64;

        final Parcel parcel = Parcel.obtain();
        try {
            feedback.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
            zos.write(parcel.marshall());
            zos.close();
            base64 = Base64.encodeToString(bos.toByteArray(), 0);
        } finally {
            parcel.recycle();
        }

        return base64;
    }

    // ReadableArray Utils
    private static JSONArray toJSONArray(final ReadableArray readableArray) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType type = readableArray.getType(i);

            switch (type) {
                case Null:
                    jsonArray.put(i, null);
                    break;
                case Boolean:
                    jsonArray.put(i, readableArray.getBoolean(i));
                    break;
                case Number:
                    jsonArray.put(i, readableArray.getDouble(i));
                    break;
                case String:
                    jsonArray.put(i, readableArray.getString(i));
                    break;
                case Map:
                    jsonArray.put(i, toJSONObject(readableArray.getMap(i)));
                    break;
                case Array:
                    jsonArray.put(i, toJSONArray(readableArray.getArray(i)));
                    break;
            }
        }

        return jsonArray;
    }

    private static Object[] toArray(final JSONArray jsonArray) throws JSONException {
        Object[] array = new Object[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);

            if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            if (value instanceof JSONArray) {
                value = toArray((JSONArray) value);
            }

            array[i] = value;
        }

        return array;
    }

    private static Object[] toArray(final ReadableArray readableArray) {
        Object[] array = new Object[readableArray.size()];

        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType type = readableArray.getType(i);

            switch (type) {
                case Null:
                    array[i] = null;
                    break;
                case Boolean:
                    array[i] = readableArray.getBoolean(i);
                    break;
                case Number:
                    array[i] = readableArray.getDouble(i);
                    break;
                case String:
                    array[i] = readableArray.getString(i);
                    break;
                case Map:
                    array[i] = toMap(readableArray.getMap(i));
                    break;
                case Array:
                    array[i] = toArray(readableArray.getArray(i));
                    break;
            }
        }

        return array;
    }

    private static WritableArray toWritableArray(final Object[] array) {
        WritableArray writableArray = Arguments.createArray();

        for (int i = 0; i < array.length; i++) {
            Object value = array[i];

            if (value == null) {
                writableArray.pushNull();
            }
            if (value instanceof Boolean) {
                writableArray.pushBoolean((Boolean) value);
            }
            if (value instanceof Double) {
                writableArray.pushDouble((Double) value);
            }
            if (value instanceof Integer) {
                writableArray.pushInt((Integer) value);
            }
            if (value instanceof String) {
                writableArray.pushString((String) value);
            }
            if (value instanceof Map) {
                writableArray.pushMap(toWritableMap((Map<String, Object>) value));
            }
            if (value != null && value.getClass().isArray()) {
                writableArray.pushArray(toWritableArray((Object[]) value));
            }
        }

        return writableArray;
    }

    // ReactNative Map Utils
    static JSONObject toJSONObject(final ReadableMap readableMap) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);

            switch (type) {
                case Null:
                    jsonObject.put(key, null);
                    break;
                case Boolean:
                    jsonObject.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    jsonObject.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    jsonObject.put(key, readableMap.getString(key));
                    break;
                case Map:
                    jsonObject.put(key, toJSONObject(readableMap.getMap(key)));
                    break;
                case Array:
                    jsonObject.put(key, toJSONArray(readableMap.getArray(key)));
                    break;
            }
        }

        return jsonObject;
    }

    static Map<String, Object> toMap(final JSONObject jsonObject) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> iterator = jsonObject.keys();

        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            if (value instanceof JSONArray) {
                value = toArray((JSONArray) value);
            }

            map.put(key, value);
        }

        return map;
    }

    static Map<String, Object> toMap(final ReadableMap readableMap) {
        Map<String, Object> map = new HashMap<String, Object>();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);

            switch (type) {
                case Null:
                    map.put(key, null);
                    break;
                case Boolean:
                    map.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    map.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    map.put(key, readableMap.getString(key));
                    break;
                case Map:
                    map.put(key, toMap(readableMap.getMap(key)));
                    break;
                case Array:
                    map.put(key, toArray(readableMap.getArray(key)));
                    break;
            }
        }

        return map;
    }

    static WritableMap toWritableMap(final Map<String, Object> map) {
        WritableMap writableMap = Arguments.createMap();

        for (Object o : map.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            Object value = pair.getValue();

            if (value == null) {
                writableMap.putNull((String) pair.getKey());
            } else if (value instanceof Boolean) {
                writableMap.putBoolean((String) pair.getKey(), (Boolean) value);
            } else if (value instanceof Double) {
                writableMap.putDouble((String) pair.getKey(), (Double) value);
            } else if (value instanceof Integer) {
                writableMap.putInt((String) pair.getKey(), (Integer) value);
            } else if (value instanceof String) {
                writableMap.putString((String) pair.getKey(), (String) value);
            } else if (value instanceof Map) {
                writableMap.putMap((String) pair.getKey(), toWritableMap((Map<String, Object>) value));
            } else if (value.getClass().isArray()) {
                writableMap.putArray((String) pair.getKey(), toWritableArray((Object[]) value));
            }

        }

        return writableMap;
    }
}
