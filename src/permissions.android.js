/*
 * Copyright (c) 2018 Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

// @flow
import { AsyncStorage, NativeModules, PermissionsAndroid } from 'react-native'

type Status = 'always' | 'denied' | 'when_in_use' | 'never_asked'

const permissionTypes = {
  location: PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
}

const RESULTS = {
  [PermissionsAndroid.RESULTS.GRANTED]: 'always',
  [PermissionsAndroid.RESULTS.DENIED]: 'denied',
  [PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN]: 'denied'
}

const STORAGE_KEY = '@RNNermissions:didAskPermission:'

const setDidAskOnce = (permission: string) =>
  AsyncStorage.setItem(STORAGE_KEY + permission, 'true')

const getDidAskOnce = (permission: string) =>
  AsyncStorage.getItem(STORAGE_KEY + permission).then(item => !!item)

export class NearITPermissions {
  canOpenSettings: () => Promise<boolean> = () => Promise.resolve(false)

  openSettings: () => Promise<*> = () =>
    Promise.reject(new Error("'openSettings' is deprecated on android"))

  checkLocation: () => Promise<Status> = () => {
    return PermissionsAndroid.check(permissionTypes['location']).then(
      isAuthorized => {
        if (isAuthorized) {
          return 'always'
        }

        return getDidAskOnce('permission').then(didAsk => {
          if (didAsk) {
            return NativeModules.PermissionsAndroid.shouldShowRequestPermissionRationale(
              permissionTypes['location']
            ).then(shouldShow => ('denied'))
          }

          return 'never_asked'
        })
      }
    )
  }

  requestLocation: () => Promise<Status> = () => {
    let rationale

    return PermissionsAndroid.request(
      permissionTypes['location'],
      rationale
    ).then(result => {
      // PermissionsAndroid.request() to native module resolves to boolean
      // rather than string if running on OS version prior to Android M
      if (typeof result === 'boolean') {
        return result ? 'always' : 'denied'
      }

      return setDidAskOnce('location').then(() => RESULTS[result])
    })
  }

  checkNotification: () => Promise<Status> = () => {
    return Promise.resolve('always')
  }

  requestNotification: () => Promise<Status> = () => {
    return Promise.resolve('always')
  }
}
