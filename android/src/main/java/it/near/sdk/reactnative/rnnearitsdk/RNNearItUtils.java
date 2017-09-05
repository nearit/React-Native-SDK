/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

 package it.near.sdk.reactnative.rnnearitsdk;

import android.util.Base64;

import com.google.gson.Gson;

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
}
