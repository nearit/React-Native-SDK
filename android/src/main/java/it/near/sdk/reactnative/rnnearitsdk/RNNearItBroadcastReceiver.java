/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import it.near.sdk.recipes.background.NearItBroadcastReceiver;
import it.near.sdk.utils.AppVisibilityDetector;


public class RNNearItBroadcastReceiver extends NearItBroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (AppVisibilityDetector.sIsForeground) {
      final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);

      final Intent localEvent = new Intent(RNNearItModule.LOCAL_EVENTS_TOPIC);
      localEvent.putExtras(intent);

      localBroadcastManager.sendBroadcast(localEvent);
    } else {
      super.onReceive(context, intent);
    }
  }
}
