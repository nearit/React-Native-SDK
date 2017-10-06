/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTUtils.h>

#import <UserNotifications/UserNotifications.h>
#import <CoreLocation/CoreLocation.h>
#import <NearITSDK/NearITSDK.h>

#import "RNNearItBackgroundQueue.h"

@interface RNNearIt : RCTEventEmitter <RCTBridgeModule, UNUserNotificationCenterDelegate, CLLocationManagerDelegate, NITManagerDelegate>

@property int listeners;

#if !TARGET_OS_TV
    + (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
    + (void)didReceiveRemoteNotification:(NSDictionary* _Nonnull) userInfo;
    + (void)didReceiveLocalNotification:(UILocalNotification* _Nonnull) notification;
    + (void)didReceiveNotificationResponse:(UNNotificationResponse* _Nonnull) response withCompletionHandler:(void (^ _Nonnull)())completionHandler;
#endif

@end

