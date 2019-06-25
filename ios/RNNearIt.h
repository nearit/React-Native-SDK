/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 * Last changes by Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTUtils.h>
#elif __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#import "RCTUtils.h"
#endif

#import <CoreBluetooth/CoreBluetooth.h>
#import <CoreLocation/CoreLocation.h>
#import <UserNotifications/UserNotifications.h>

#if __has_include(<NearITSDK/NearITSDK.h>)
#import <NearITSDK/NearITSDK.h>
#elif __has_include("NearIT.h")
#import "NearIT.h"
#endif

#import "RNNearItUtils.h"
#import "RNNearItConsts.h"
#import "RNNearItBackgroundQueue.h"

@interface RNNearIt : RCTEventEmitter <RCTBridgeModule, CBCentralManagerDelegate, NITNotificationUpdateDelegate, NITManagerDelegate>

@property int listeners;
@property (nonatomic, strong) CBCentralManager* _Nullable bluetoothManager;
@property RCTPromiseResolveBlock _Nullable bluetoothResolve;

+ (RNNearIt* _Nullable)defaultManager;

// Background fetch
- (void)application:(UIApplication* _Nonnull)application performFetchWithCompletionHandler:(void (^_Nonnull)(UIBackgroundFetchResult))completionHandler;

// Test devices
- (BOOL)application:(UIApplication* _Nonnull)app openUrl:(NSURL* _Nonnull)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id>* _Nullable)options;

// Notifications
- (void)userNotificationCenter:(UNUserNotificationCenter* _Nonnull)center willPresentNotification:(UNNotification* _Nonnull)notification withCompletionHandler:(void (^_Nonnull)(UNNotificationPresentationOptions))completionHandler;
- (BOOL)userNotificationCenter:(UNUserNotificationCenter* _Nonnull)center didReceiveNotificationResponse:(UNNotificationResponse* _Nonnull)response withCompletionHandler:(void (^_Nonnull)(void))completionHandler;
- (void)application:(UIApplication* _Nonnull)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData* _Nullable)deviceToken;

// Notifications iOS9
- (BOOL)application:(UIApplication* _Nonnull)application didReceiveRemoteNotification:(NSDictionary* _Nonnull)userInfo;
- (BOOL)application:(UIApplication* _Nonnull)application didReceiveLocalNotification:(UILocalNotification* _Nonnull)notification;

@end
