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

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.couponplugin.model.Coupon;
import it.near.sdk.reactions.feedbackplugin.model.Feedback;
import it.near.sdk.trackings.TrackingInfo;

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
}
