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

type NearItPermissions = {
  location: string,
  notifications: string,
  bluetooth: string,
  locationServices: string,
  always: string,
  whenInUse: string,
  denied: string
}

type NearItEvents = {
  SimpleNotification: string,
  Content: string,
  Feedback: string,
  Coupon: string,
  CustomJson: string
}

type NearItEventContent = {
  type: string,
    trackingInfo: string,
    message: string,
    content: string,
    fromUserAction: string,
    status: string,
    title: string,
    image: string,
    fullSize: string,
    squareSize: string,
    text: string,
    cta: string,
    label: string,
    url: string,
    description: string,
    value: string,
    expiresAt: string,
    redeemableFrom: string,
    serial: string,
    claimedAt: string,
    redeemedAt: string,
    question: string,
    feedbackId: string,
    read: string,
    timestamp: string,
    isNew: string,
    notificationContent: string
}

type NearItStatuses = {
  received: string,
  opened: string
}

type NearItConstants = {
  Events: NearItEvents,
  EventContent: NearItEventContent,
  Statuses: NearItStatuses,
  Permissions: NearItPermissions
}

type NearItEvent = {
  type: string
}

type NearItImage = {
  fullSize: ?string,
  squareSize: ?string
}

interface NearItCta {
  label: string,
  url: string
}

type NearItCoupon = {
  title: string,
  description: ?string,
  image: ?NearItImage,
  value: string,
  expiresAt: ?string,
  redeemableFrom: ?string,
  serial: string,
  claimedAt: ?string,
  redeemedAt: ?string
}

interface NearItHistoryItem {
  read: boolean,
  timestamp: string,
  isNew: boolean,
  notificationContent: any
}

type LocationPermissionStatus = NearItPermissions.always | NearItPermissions.denied | NearItPermissions.whenInUse

type NearItPermissionsResult = {
  bluetooth: boolean,
  location: LocationPermissionStatus,
  locationServices: boolean,
  notifications: boolean
}

type NearItRating = 0 | 1 | 2 | 3 | 4 | 5

type EmitterSubscription = {
  remove(): void
}

type NearItContentsListener = (event: NearItEvent) => void

type NearItNotificationHistoryUpdateListener = (history: NearItHistoryItem[]) => void

const NearItSdk = NativeModules.RNNearIt
const NearItUI = NativeModules.RNNearItUI

export class NearItManager {
  static constants: NearItConstants = {
    Events: NearItSdk.Events,
    EventContent: NearItSdk.EventContent,
    Statuses: NearItSdk.Statuses,
    Permissions: NearItSdk.Permissions
  }

  static onDeviceReady () {
    NearItSdk.onDeviceReady()
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

  static showCouponList (title: ?string) {
    return NearItUI.showCouponList(title)
  }

  // Notification history related methods

  // TODO: return type
  static getNotificationHistory (): Promise<NearItHistoryItem[]> {
    return NearItSdk.getNotificationHistory()
  }

  static showNotificationHistory (title: ?string) {
    return NearItUI.showNotificationHistory(title)
  }

  static addNotificationHistoryUpdateListener (listener: NearItNotificationHistoryUpdateListener): EmitterSubscription {
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

  static markNotificationHistoryAsOld () {
    NearItSdk.markNotificationHistoryAsOld()
  }

  // Permissions related methods

  static requestPermissions (explanation: ?string): Promise<NearItPermissionsResult> {
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

  static showContent (event: NearItEvent) {
    return NearItUI.showContent(event)
  }

  static disableDefaultRangingNotifications () {
    NearItSdk.disableDefaultRangingNotifications()
  }

  static addProximityListener (listener: NearItContentsListener): EmitterSubscription {
    const subscription = NearItManager._eventSource.addListener(NearItSdk.NativeEventsTopic, listener)
    NearItSdk.listenerRegistered()
    return subscription
  }

  static removeProximityListener (subscription: EmitterSubscription) {
    NearItSdk.listenerUnregistered()
      .then(res => {
        subscription.remove()
      })
  }
}
export const constants = NearItManager.constants
