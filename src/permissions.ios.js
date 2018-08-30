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

class NearITPermissions {
  canOpenSettings: () => Promise<boolean> = () =>
    PermissionsIOS.canOpenSettings()

  openSettings: () => Promise<*> = () =>
    PermissionsIOS.openSettings()

  checkLocation: () => Promise<Status> = () =>
    PermissionsIOS.getPermissionStatus('location')

  requestLocation: () => Promise<Status> = () =>
    PermissionsIOS.requestPermission('location')

  checkNotification: () => Promise<Status> = () =>
    PermissionsIOS.getPermissionStatus('notification')

  requestNotification: () => Promise<Status> = () =>
    PermissionsIOS.requestPermission('notification')
}
