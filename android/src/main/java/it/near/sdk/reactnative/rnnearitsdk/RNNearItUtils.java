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

import it.near.sdk.logging.NearLog;
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

@SuppressWarnings({"CharsetObjectCanBeUsed", "Convert2Diamond"})
public class RNNearItUtils {

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
            Log.e("RNNearItUtils", "error while decoding trackingInfo");
        }

        return null;
    }

    static WritableMap bundleCoupon(final Coupon coupon) {
        final WritableMap couponMap = new WritableNativeMap();

        couponMap.putString("title", coupon.getTitle());
        couponMap.putString("description", coupon.description);
        couponMap.putString("value", coupon.value);
        couponMap.putString("expiresAt", coupon.expires_at);
        couponMap.putString("redeemableFrom", coupon.redeemable_from);
        couponMap.putString("serial", coupon.getSerial());
        couponMap.putString("claimedAt", coupon.getClaimedAt());
        couponMap.putString("redeemedAt", coupon.getRedeemedAt());

        // Coupon icon handling
        if (coupon.getIconSet() != null) {
            couponMap.putMap("image", bundleImageSet(coupon.getIconSet()));
        }

        return couponMap;
    }

    public static Coupon unbundleCoupon(final WritableMap bundledCoupon) {
        Coupon coupon = new Coupon();
        coupon.name = getNullableField(bundledCoupon, "title");
        coupon.description = getNullableField(bundledCoupon, "description");
        coupon.value = getNullableField(bundledCoupon, "value");
        coupon.expires_at = getNullableField(bundledCoupon, "expiresAt");
        coupon.redeemable_from = getNullableField(bundledCoupon, "redeemableFrom");
        List<Claim> claims = new ArrayList<Claim>();
        Claim claim = new Claim();
        claim.serial_number = getNullableField(bundledCoupon, "serial");
        claim.claimed_at = getNullableField(bundledCoupon, "claimedAt");
        claim.redeemed_at = getNullableField(bundledCoupon, "redeemedAt");
        claims.add(claim);
        coupon.claims = claims;
        if (bundledCoupon.hasKey("image")) {
            WritableMap imageSet = new Gson().fromJson(bundledCoupon.getString("image"), WritableMap.class);
            coupon.setIconSet(unbundleImageSet(imageSet));
        }
        return coupon;
    }

    public static WritableMap bundleContent(final Content content) {
        final WritableMap bundledContent = new WritableNativeMap();

        String title = content.title;
        if (title == null) {
            title = "";
        }
        bundledContent.putString("title", title);

        String text = content.contentString;
        if (text == null) {
            text = "";
        }
        bundledContent.putString("text", text);

        WritableMap image = null;
        if (content.getImageLink() != null) {
            image = bundleImageSet(content.getImageLink());
        }
        bundledContent.putMap("image", image);

        WritableMap cta = null;
        if (content.getCta() != null) {
            cta = bundleContentLink(content.getCta());
        }
        bundledContent.putMap("cta", cta);

        return bundledContent;
    }

    public static Content unbundleContent(final WritableMap bundledContent) {
        final Content content = new Content();
        content.title = getNullableField(bundledContent, "title");
        content.contentString = getNullableField(bundledContent, "text");
        final List<ImageSet> images = new ArrayList<ImageSet>();
        WritableMap imageSet = new Gson().fromJson(bundledContent.getString("image"), WritableMap.class);
        images.add(unbundleImageSet(imageSet));
        content.setImages_links(images);
        WritableMap bundledCta = new Gson().fromJson(bundledContent.getString("cta"), WritableMap.class);
        content.setCta(unbundleContentLink(bundledCta));
        return content;
    }

    public static WritableMap bundleHistoryItem(final HistoryItem item) {
        final WritableMap itemMap = new WritableNativeMap();

        itemMap.putBoolean("read", item.read);
        itemMap.putDouble("timestamp", item.timestamp);
        itemMap.putBoolean("isNew", item.isNew);

        try {
            itemMap.putString("trackingInfo", bundleTrackingInfo(item.trackingInfo));
        } catch (Exception e) {
            NearLog.d("RNNearItUtils", "historyItem encoding error", e);
        }
        itemMap.putString("message", item.reaction.notificationMessage);

        if (item.reaction instanceof SimpleNotification) {
            itemMap.putString("type", RNNearItModule.EVENT_TYPE_SIMPLE);
        } else if (item.reaction instanceof Content) {
            Content content = (Content) item.reaction;
            itemMap.putMap("notificationContent", bundleContent(content));
            itemMap.putString("type", RNNearItModule.EVENT_TYPE_CONTENT);
        } else if (item.reaction instanceof Feedback) {
            Feedback feedback = (Feedback) item.reaction;
            itemMap.putMap("notificationContent", bundleFeedback(feedback));
            itemMap.putString("type", RNNearItModule.EVENT_TYPE_FEEDBACK);
        } else if (item.reaction instanceof Coupon) {
            Coupon coupon = (Coupon) item.reaction;
            itemMap.putMap("notificationContent", bundleCoupon(coupon));
            itemMap.putString("type", RNNearItModule.EVENT_TYPE_COUPON);
        } else if (item.reaction instanceof CustomJSON) {
            CustomJSON customJson = (CustomJSON) item.reaction;
            itemMap.putMap("notificationContent", bundleCustomJson(customJson));
            itemMap.putString("type", RNNearItModule.EVENT_TYPE_CUSTOM_JSON);
        }

        return itemMap;
    }

    static WritableMap bundleImageSet(ImageSet imageSet) {
        final WritableMap image = new WritableNativeMap();
        image.putString("fullSize", imageSet.getFullSize());
        image.putString("squareSize", imageSet.getSmallSize());

        return image;
    }

    private static ImageSet unbundleImageSet(WritableMap bundledImage) {
        final ImageSet imageSet = new ImageSet();
        imageSet.setFullSize(getNullableField(bundledImage, "fullSize"));
        imageSet.setSmallSize(getNullableField(bundledImage, "squareSize"));
        return imageSet;
    }

    static WritableMap bundleContentLink(ContentLink cta) {
        final WritableMap contentLink = new WritableNativeMap();
        contentLink.putString("label", cta.label);
        contentLink.putString("url", cta.url);
        return contentLink;
    }

    private static ContentLink unbundleContentLink(WritableMap bundledCta) {
        return new ContentLink(getNullableField(bundledCta, "label"), getNullableField(bundledCta, "url"));
    }

    private static String getNullableField(WritableMap map, String key) {
        if (map.hasKey(key) && map.getString(key) != null) {
            return map.getString(key);
        }
        return null;
    }

    static WritableMap bundleFeedback(final Feedback feedback) {
        final WritableMap bundledFeedback = new WritableNativeMap();

        String question = feedback.question;
        if (question == null) {
            question = "";
        }
        bundledFeedback.putString("feedbackQuestion", question);

        String feedbackB64 = null;
        try {
            feedbackB64 = feedbackToB64(feedback);
        } catch (Exception e) {
            NearLog.d("RNNearItUtils", "feedback encoding error", e);
        }
        if (feedbackB64 != null) {
            bundledFeedback.putString("feedbackId", feedbackB64);
        }

        return bundledFeedback;
    }

    static Feedback unbundleFeedback(final String base64) throws Exception {
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

    static WritableMap bundleCustomJson(final CustomJSON customJson) {
        final WritableMap bundledCustomJson = new WritableNativeMap();
        bundledCustomJson.putMap("data", toWritableMap(customJson.content));
        return bundledCustomJson;
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
    private static JSONArray toJSONArray(ReadableArray readableArray) throws JSONException {
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

    private static Object[] toArray(JSONArray jsonArray) throws JSONException {
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

    private static Object[] toArray(ReadableArray readableArray) {
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

    private static WritableArray toWritableArray(Object[] array) {
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
    static JSONObject toJSONObject(ReadableMap readableMap) throws JSONException {
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

    static Map<String, Object> toMap(JSONObject jsonObject) throws JSONException {
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

    private static Map<String, Object> toMap(ReadableMap readableMap) {
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

    static WritableMap toWritableMap(Map<String, Object> map) {
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
            } else if (value.getClass() != null && value.getClass().isArray()) {
                writableMap.putArray((String) pair.getKey(), toWritableArray((Object[]) value));
            }

        }

        return writableMap;
    }
}
