/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.near.sdk.reactnative.rnnearitsdk;

import com.facebook.react.bridge.WritableMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RNNearItBackgroundQueue {

  private static RNNearItBackgroundQueue _defaultInstance;

  private AtomicInteger _listeners;
  private BlockingQueue<WritableMap> notificationsQueue;

  static public synchronized RNNearItBackgroundQueue defaultQueue() {
    if (_defaultInstance == null) {
      _defaultInstance = new RNNearItBackgroundQueue();
    }
    return _defaultInstance;
  }

  private RNNearItBackgroundQueue() {
    _listeners = new AtomicInteger(0);
    notificationsQueue = new LinkedBlockingQueue<>();
  }

  public boolean hasListeners() {
    return _listeners.get() > 0;
  }

  public int registerListener() {
    return _listeners.incrementAndGet();
  }

  public int unregisterListener() {
    return _listeners.decrementAndGet();
  }

  public void addNotification(final WritableMap notification) {
    if (notificationsQueue == null) {
      return;
    }
    notificationsQueue.offer(notification);
  }

  public void dispatchNotificationsQueue(RNNearItBackgroundQueue.NotificationDispatcher dispatcher) {
    // Avoid dequeue if no listener is registered
    if (!hasListeners()) {
      return;
    }

    // Dispatch all queue notifications
    while (notificationsQueue.peek() != null) {
      dispatcher.onNotification(notificationsQueue.poll());
    }
  }

  public interface NotificationDispatcher {
    void onNotification(final WritableMap notification);
  }
}
