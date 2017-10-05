/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.trackings.TrackingInfo;

import static com.facebook.react.bridge.ReadableType.Array;

public class RNNearItUtils {

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

  static WritableMap bundleCoupon(final Coupon coupon) {
    final WritableMap couponMap = new WritableNativeMap();
    couponMap.putString("name", coupon.name);
    couponMap.putString("description", coupon.description);
    couponMap.putString("value", coupon.value);
    couponMap.putString("expiresAt", coupon.expires_at);
    couponMap.putString("redeemableFrom", coupon.redeemable_from);
    couponMap.putString("serial", coupon.getSerial());
    couponMap.putString("claimedAt", coupon.getClaimedAt());
    couponMap.putString("redeemedAt", coupon.getRedeemedAt());

    // Coupon icon handling
    if (coupon.getIconSet() != null) {
      couponMap.putMap("image", RNNearItUtils.bundleImageSet(coupon.getIconSet()));
    }

    return couponMap;
  }

  static WritableMap bundleImageSet(ImageSet imageSet) {
    final WritableMap image = new WritableNativeMap();
    image.putString("fullSize", imageSet.getFullSize());
    image.putString("squareSize", imageSet.getSmallSize());

    return image;
  }

  static String feedbackToBase64(final Feedback feedback) throws Exception {
    String base64 = null;

    final Parcel parcel = Parcel.obtain();
    try {
      feedback.writeToParcel(parcel, Parcelable.CONTENTS_FILE_DESCRIPTOR);
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

  static Feedback feedbackFromBase64(final String base64) throws Exception {
    Feedback feedback = null;
    final Parcel parcel = Parcel.obtain();
    try {
      final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
      final byte[] buffer = new byte[1024];
      final GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(base64, 0)));
      int len = 0;
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

  // ReadableArray Utils
  public static JSONArray toJSONArray(ReadableArray readableArray) throws JSONException {
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

  public static Object[] toArray(JSONArray jsonArray) throws JSONException {
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

  public static Object[] toArray(ReadableArray readableArray) {
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

  public static WritableArray toWritableArray(Object[] array) {
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
      if (value.getClass().isArray()) {
        writableArray.pushArray(toWritableArray((Object[]) value));
      }
    }

    return writableArray;
  }

  // ReactNative Map Utils
  public static JSONObject toJSONObject(ReadableMap readableMap) throws JSONException {
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

  public static Map<String, Object> toMap(JSONObject jsonObject) throws JSONException {
    Map<String, Object> map = new HashMap<>();
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

  public static Map<String, Object> toMap(ReadableMap readableMap) {
    Map<String, Object> map = new HashMap<>();
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

  public static WritableMap toWritableMap(Map<String, Object> map) {
    WritableMap writableMap = Arguments.createMap();
    Iterator iterator = map.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry pair = (Map.Entry)iterator.next();
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

      iterator.remove();
    }

    return writableMap;
  }
}
