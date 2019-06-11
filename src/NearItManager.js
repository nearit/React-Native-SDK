/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 * Last changes by Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

// @flow
import { NativeEventEmitter, NativeModules } from 'react-native'

type NearItEvents = {
  SimpleNotification: string,
  Content: string,
  Feedback: string,
  Coupon: string,
  CustomJson: string
}

type NearItEventContent = {
  type: string,
  content: string,
  fromUserAction: string,
  trackingInfo: string
}

type NearItStatuses = {
  received: string,
  opened: string
}

type NearItConstants = {
  Events: NearItEvents,
  EventContent: NearItEventContent,
  Statuses: NearItStatuses
}

type NearItEvent = {
  'type': string
}

type NearItContentsListener = (event: NearItEvent) => void

type NearItImage = {
  'fullSize': ?string,
  'squareSize': ?string
}

type NearItCoupon = {
  'title': string,
  'description': string,
  'image': ?NearItImage,
  'value': string,
  'expiresAt': string,
  'redeemableFrom': string,
  'serial': ?string,
  'claimedAt': ?string,
  'redeemedAt': ?string
}

type NearItRating = 0 | 1 | 2 | 3 | 4 | 5

type EmitterSubscription = {
  remove(): void
}

type NearItNotificationHistoryUpdateListener = () => void

const NearItSdk = NativeModules.RNNearIt
const NearItUI = NativeModules.RNNearItUI

export class NearItManager {
  static constants: NearItConstants = {
    Events: NearItSdk.Events,
    EventContent: NearItSdk.EventContent,
    Statuses: NearItSdk.Statuses
  }

  static _eventSource = new NativeEventEmitter(NearItSdk)

  static addContentsListener (listener: NearItContentsListener): EmitterSubscription {
    const subscription = NearItManager._eventSource.addListener(NearItSdk.NativeEventsTopic, listener)
    NearItSdk.listenerRegistered()
    return subscription
  }

  static removeContentsListener (subscription: EmitterSubscription) {
    NearItSdk.listenerUnregistered()
      .then(res => {
        subscription.remove()
      })
  }

  // Radar related methods

  static startRadar () {
    NearItSdk.startRadar()
  }

  static stopRadar () {
    NearItSdk.stopRadar()
  }

  // Trackings related methods

  static sendTracking (trackingInfo: string, status: string) {
    NearItSdk.sendTracking(trackingInfo, status)
  }

  // Feedback related methods

  static sendFeedback (feedbackId: string, rating: NearItRating, comment: ?string): Promise<null> {
    return NearItSdk.sendFeedback(feedbackId, rating, comment)
  }

  // ProfileId related methods

  static getProfileId (): Promise<string> {
    return NearItSdk.getProfileId()
  }

  static setProfileId (profileId: string) {
    NearItSdk.setProfileId(profileId)
  }

  static resetProfileId (): Promise<string> {
    return NearItSdk.resetProfileId()
  }

  // User data related methods

  static setMultiChoiceUserData (key: String, userDataObject: { [string]: boolean }) {
    NearItSdk.setMultiChoiceUserData(key, userDataObject)
  }

  static setUserData (key: string, value: ?string) {
    NearItSdk.setUserData(key, value)
  }

  static getUserData (): Promise<any> {
    return NearItSdk.getUserData()
  }

  // Opt-out related methods

  static optOut (): Promise<null> {
    return NearItSdk.optOut()
  }

  // In-app events related methods

  static triggerInAppEvent (eventKey: string) {
    NearItSdk.triggerInAppEvent(eventKey)
  }

  // Coupon related methods

  static getCoupons (): Promise<NearItCoupon[]> {
    return NearItSdk.getCoupons()
  }

  static showCouponList (): Promise<null> {
    return NearItUI.showCouponList()
  }

  // Notification history related methods

  // TODO: return type
  static getNotificationHistory (): Promise<null> {
    return NearItSdk.getNotificationHistory()
  }

  static showNotificationHistory (): Promise<null> {
    return NearItUI.showNotificationHistory()
  }

  static setNotificationHistoryUpdateListener (listener: NearItNotificationHistoryUpdateListener): EmitterSubscription {
    const subscription = NearItManager._eventSource.addListener(NearItSdk.NativeNotificationHistoryTopic, listener)
    NearItSdk.notificationHistoryListenerRegistered()
    return subscription
  }

  static removeNotificationHistoryUpdateListener (subscription: EmitterSubscription) {
    NearItSdk.notificationHistoryListenerUnregistered()
      .then(res => {
        subscription.remove()
      })
  }

  static markNotificationHistoryAsOld (): Promise<null> {
    return NearItSdk.markNotificationHistoryAsOld()
  }

  // Permissions related methods

  static requestPermissions (explanation: ?string): Promise<null> {
    return NearItUI.requestPermissions(explanation)
  }

  static isBluetoothEnabled (): Promise<boolean> {
    return NearItSdk.isBluetoothEnabled()
  }

  static areLocationServicesOn (): Promise<boolean> {
    return NearItSdk.areLocationServicesOn()
  }

  static isLocationGranted (): Promise<boolean> {
    return NearItSdk.isLocationGranted()
  }

  static isNotificationGranted (): Promise<boolean> {
    return NearItSdk.isNotificationGranted()
  }

  // Content related methods

  // TODO: param type
  static showContent (content: any): Promise<null> {
    return NearItUI.showContent()
  }
}
export const constants = NearItManager.constants
