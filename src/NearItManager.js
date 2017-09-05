/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import {
  DeviceEventEmitter, // android
  NativeAppEventEmitter, // ios
  NativeModules,
  Platform
} from 'react-native'

const NearItSdk = NativeModules.RNNearIt

export class NearItManager {
  static constants = {
    Actions: NearItSdk.Actions,
    Statuses: NearItSdk.Statuses
  }

  static setContentsListener (listener) {
    if (listener) {
      Platform.select({
        ios: NativeAppEventEmitter.addListener('NearItContent', listener),
        android: DeviceEventEmitter.addListener('NearItContent', listener)
      })
    }
  }

  static refreshConfig () {
    return NearItSdk.refreshConfig()
  }

  static startRadar () {
    return NearItSdk.startRadar()
  }

  static stopRadar () {
    return NearItSdk.stopRadar()
  }

  static sendTracking (trackingInfo, status) {
    return NearItSdk.sendTracking(trackingInfo, status)
  }

  /*static sendFeedback (recipeId, feedbackId, rating, comment = '') {
    return NearItSdk.sendFeedback(recipeId, feedbackId, rating, comment)
  }*/

  static getUserProfileId () {
    return NearItSdk.getUserProfileId()
  }

  static setUserProfileId (profileId) {
    return NearItSdk.setUserProfileId(profileId)
  }

  static resetUserProfile () {
    return NearItSdk.resetUserProfile()
  }

  static createUserProfile () {
    return NearItSdk.createUserProfile()
  }

  static setUserData (userDataObject) {
    return NearItSdk.setUserData(userDataObject)
  }
}

export const constants = NearItManager.constants
