/*
 * Copyright (c) 2018 Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

// @flow
import { NativeModules } from 'react-native'
const PermissionsIOS = NativeModules.RNNPermissions

type Status = 'always' | 'denied' | 'when_in_use' | 'never_asked'

export class NearITPermissions {
  static canOpenSettings (): Promise<boolean> {
    return PermissionsIOS.canOpenSettings()
  }

  static openSettings (): Promise<*> {
    return PermissionsIOS.openSettings()
  }

  static checkLocation (): Promise<Status> {
    return PermissionsIOS.getPermissionStatus('location')
  }

  static requestLocation (): Promise<Status> {
    return PermissionsIOS.requestPermission('location')
  }

  static checkNotification (): Promise<Status> {
    return PermissionsIOS.getPermissionStatus('notification')
  }

  static requestNotification (): Promise<Status> {
    return PermissionsIOS.requestPermission('notification')
  }
}
