/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

import static it.near.sdk.reactnative.rnnearitsdk.RNNearItModule.MODULE_NAME;

class RNNearItPersistedQueue {

  private static final String SP_LAST_NOTIFICATION_INDEX = "LastNotificationIndex";
  private static final String SP_LAST_READ_NOTIFICATION_INDEX = "LastReadNotificationIndex";

  private static RNNearItPersistedQueue defaultInstance;
  private AtomicInteger _listeners;

  public static synchronized RNNearItPersistedQueue defaultQueue() {
    if (defaultInstance == null) {
      defaultInstance = new RNNearItPersistedQueue();
    }
    return defaultInstance;
  }

  private RNNearItPersistedQueue() {
    _listeners = new AtomicInteger(0);

  }

  boolean hasListeners() {
    return _listeners.get() > 0;
  }

  int registerListener() {
    return _listeners.incrementAndGet();
  }

  int unregisterListener() {
    if (_listeners.get() > 0) {
      return _listeners.decrementAndGet();
    }
    return 0;
  }

  void resetListeners() {
    _listeners.set(0);
  }

  static void addNotification(final Context context, final WritableMap notification) {
    final int lastNotificationIndex = context.getSharedPreferences(MODULE_NAME, Context.MODE_PRIVATE)
            .getInt(SP_LAST_NOTIFICATION_INDEX, 0);

    final int newIndex = lastNotificationIndex + 1;

    try {
      context.getSharedPreferences(MODULE_NAME, Context.MODE_PRIVATE)
              .edit()
              .putString(MODULE_NAME + "Notification" + newIndex, RNNearItUtils.toJSONObject(notification).toString())
              .putInt(SP_LAST_NOTIFICATION_INDEX, newIndex)
              .apply();
    } catch (Exception e) {
      Log.e(MODULE_NAME, "Failed to store new notification", e);
    }
  }

  static void dispatchNotificationsQueue(final Context context, RNNearItPersistedQueue.NotificationDispatcher dispatcher) {
    if (!defaultQueue().hasListeners()) {
      // Should not be here if there are no listeners
      return;
    }

    final SharedPreferences preferences = context.getSharedPreferences(MODULE_NAME, Context.MODE_PRIVATE);

    final int lastIndex = preferences.getInt(SP_LAST_NOTIFICATION_INDEX, 0);
    int lastRead = preferences.getInt(SP_LAST_READ_NOTIFICATION_INDEX, 0);

    while(lastRead <= lastIndex) {
      try {
        lastRead = lastRead + 1;
        String notificationJSON = preferences.getString(MODULE_NAME + "Notification" + lastRead, "");
        if (notificationJSON.length() > 0) {
          final WritableMap notification = RNNearItUtils.toWritableMap(RNNearItUtils.toMap(new JSONObject(notificationJSON)));
          dispatcher.onNotification(notification);
          preferences.edit()
                  .putInt(SP_LAST_READ_NOTIFICATION_INDEX, lastRead)
                  .remove(MODULE_NAME + "Notification" + lastRead)
                  .commit();
        }
      } catch (Exception e) {
        Log.e(MODULE_NAME, "Failed to dispatch preference", e);
      }
    }
  }

  public interface NotificationDispatcher {
    void onNotification(final WritableMap notification);
  }
}
