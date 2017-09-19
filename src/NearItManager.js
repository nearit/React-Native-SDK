/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

// @flow
import { NativeEventEmitter, NativeModules } from 'react-native'

type NearItEvent = {
  'type': string
}

type NearItContentsListener = (event: NearItEvent) => void

type NearItImage = {
  'fullSize': ?string,
  'squareSize': ?string
}

type NearItCoupon = {
  'name': string,
  'description': string,
  'image': ?NearItImage,
  'value': string,
  'expiresAt': string,
  'redeemableFrom': string,
  'serial': ?string,
  'claimedAt': ?string,
  'redeemedAt': ?string
}

type EmitterSubscription = {
  remove(): void
}

const NearItSdk = NativeModules.RNNearIt

export class NearItManager {
  static constants = {
    Events: NearItSdk.Events,
    EventContent: NearItSdk.EventContent,
    Statuses: NearItSdk.Statuses,
    Permissions: NearItSdk.Permissions
  }

  static _eventSource = new NativeEventEmitter(NearItSdk)

  static setContentsListener (listener: NearItContentsListener): EmitterSubscription {
    return NearItManager._eventSource.addListener(NearItSdk.NativeEventsTopic, listener)
  }

  static refreshConfig (): Promise<null> {
    return NearItSdk.refreshConfig()
  }

  static startRadar (): Promise<null> {
    return NearItSdk.startRadar()
  }

  static stopRadar (): Promise<null> {
    return NearItSdk.stopRadar()
  }

  static sendTracking (trackingInfo: string, status: string): Promise<null> {
    return NearItSdk.sendTracking(trackingInfo, status)
  }

  /* static sendFeedback (recipeId, feedbackId, rating, comment = '') {
    return NearItSdk.sendFeedback(recipeId, feedbackId, rating, comment)
  } */

  static getUserProfileId (): Promise<string> {
    return NearItSdk.getUserProfileId()
  }

  static setUserProfileId (profileId: string): Promise<string> {
    return NearItSdk.setUserProfileId(profileId)
  }

  static resetUserProfile (): Promise<null> {
    return NearItSdk.resetUserProfile()
  }

  static setUserData (userDataObject: { [string]: any }): Promise<null> {
    return NearItSdk.setUserData(userDataObject)
  }

  static requestNotificationPermission (): Promise<boolean> {
    return NearItSdk.requestNotificationPermission()
  }

  static requestLocationPermission (): Promise<boolean | null> {
    return NearItSdk.requestLocationPermission()
  }

  static getCoupons (): Promise<NearItCoupon[]> {
    return NearItSdk.getCoupons()
  }
}

export const constants = NearItManager.constants
