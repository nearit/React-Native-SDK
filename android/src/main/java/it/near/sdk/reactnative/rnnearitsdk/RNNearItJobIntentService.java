/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import it.near.sdk.recipes.background.NearBackgroundJobIntentService;
import it.near.sdk.utils.AppVisibilityDetector;
import it.near.sdk.utils.NearUtils;


public class RNNearItJobIntentService extends NearBackgroundJobIntentService {

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    if (AppVisibilityDetector.sIsForeground) {
      final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

      final Intent localEvent = new Intent(RNNearItModule.LOCAL_EVENTS_TOPIC);
      localEvent.putExtras(intent);

      localBroadcastManager.sendBroadcast(localEvent);
    } else {
      if (NearUtils.carriesNearItContent(intent)) {
        NearUtils.parseCoreContents(intent, new RNNearItCoreContentsListener(this, null, true));
      }
      super.onHandleWork(intent);
    }
  }
}
