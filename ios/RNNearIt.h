/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 * Last changes by Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTUtils.h>

#import <CoreBluetooth/CoreBluetooth.h>
#import <CoreLocation/CoreLocation.h>
#import <UserNotifications/UserNotifications.h>

#import <NearITSDK/NearITSDK.h>

#import "RNNearItUtils.h"
#import "RNNearItConsts.h"
#import "RNNearItBackgroundQueue.h"

@interface RNNearIt : RCTEventEmitter <RCTBridgeModule, CBCentralManagerDelegate, NITNotificationUpdateDelegate, NITManagerDelegate>

@property int listeners;
@property (nonatomic, strong) CBCentralManager* bluetoothManager;
@property RCTPromiseResolveBlock* bluetoothResolve;

- (void)loadConfig;

+ (BOOL)application:(UIApplication* _Nonnull)application didFinishLaunchingWithOptions:(NSDictionary* _Nullable)launchOptions;

// Background fetch
+ (void)application:(UIApplication* _Nonnull)application performFetchWithCompletionHandler:(void (^_Nonnull)(UIBackgroundFetchResult))completionHandler;

// Test devices
+ (BOOL)application:(UIApplication* _Nonnull)app openUrl:(NSURL* _Nonnull)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id>* _Nullable)options;

// Notifications
+ (void)userNotificationCenter:(UNUserNotificationCenter* _Nonnull)center willPresentNotification:(UNNotification* _Nonnull)notification withCompletionHandler:(void (^_Nonnull)(UNNotificationPresentationOptions))completionHandler;
+ (BOOL)userNotificationCenter:(UNUserNotificationCenter* _Nonnull)center didReceiveNotificationResponse:(UNNotificationResponse* _Nonnull)response withCompletionHandler:(void (^_Nonnull)(void))completionHandler;
+ (void)application:(UIApplication* _Nonnull)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData* _Nullable)deviceToken;

// Notifications iOS9
+ (void)application:(UIApplication* _Nonnull)application didReceiveRemoteNotification:(NSDictionary* _Nonnull)userInfo;
+ (void)application:(UIApplication* _Nonnull)application didReceiveLocalNotification:(UILocalNotification* _Nonnull)notification;

@end
