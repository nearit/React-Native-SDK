/*
 * Copyright (c) 2018 Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import "RNNPermissions.h"

#if __has_include(<React/RCTBridge.h>)
  #import <React/RCTBridge.h>
#elif __has_include("React/RCTBridge.h")
  #import "React/RCTBridge.h"
#else
  #import "RCTBridge.h"
#endif

#if __has_include(<React/RCTConvert.h>)
  #import <React/RCTConvert.h>
#elif __has_include("React/RCTConvert.h")
  #import "React/RCTConvert.h"
#else
  #import "RCTConvert.h"
#endif

#if __has_include(<React/RCTEventDispatcher.h>)
  #import <React/RCTEventDispatcher.h>
#elif __has_include("React/RCTEventDispatcher.h")
  #import "React/RCTEventDispatcher.h"
#else
  #import "RCTEventDispatcher.h"
#endif

#import "RNNLocationPermission.h"
#import "RNNNotificationPermission.h"

@interface RNNPermissions()
@property (strong, nonatomic) RNNLocationPermission *locationMgr;
@property (strong, nonatomic) RNNNotificationPermission *notificationMgr;
@end

@implementation RNNPermissions

RCT_EXPORT_MODULE();
@synthesize bridge = _bridge;

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

#pragma mark Initialization

- (instancetype)init
{
    if (self = [super init]) {
    }

    return self;
}

/**
 * run on the main queue.
 */
- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

// MARK: Open app settings

RCT_REMAP_METHOD(canOpenSettings, canOpenSettings:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve(@(UIApplicationOpenSettingsURLString != nil));
}

RCT_EXPORT_METHOD(openSettings:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if (@(UIApplicationOpenSettingsURLString != nil)) {

        NSNotificationCenter * __weak center = [NSNotificationCenter defaultCenter];
        id __block token = [center addObserverForName:UIApplicationDidBecomeActiveNotification
                                               object:nil
                                                queue:nil
                                           usingBlock:^(NSNotification *note) {
                                               [center removeObserver:token];
                                               resolve(@YES);
                                           }];

        NSURL *url = [NSURL URLWithString:UIApplicationOpenSettingsURLString];
        [[UIApplication sharedApplication] openURL:url];
    }
}


// MARK: Check and request permissions

RCT_REMAP_METHOD(getPermissionStatus, getPermissionStatus:(RNNPermissionType)type json:(id)json resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *status;

    switch (type) {
        case RNNPermissionTypeLocation: {
            status = [RNNLocationPermission getStatus];
            break;
        }
        case RNNPermissionTypeNotification: {
            status = [RNNNotificationPermission getStatus];
            break;
        }
        default:
            break;
    }

    resolve(status);
}

RCT_REMAP_METHOD(requestPermission, permissionType:(RNNPermissionType)type json:(id)json resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    switch (type) {
        case RNNPermissionTypeLocation:
            return [self requestLocation:resolve];
        case RNNPermissionTypeNotification:
            return [self requestNotification:resolve];
        default:
            break;
    }
}

- (void) requestLocation:(RCTPromiseResolveBlock)resolve
{
    if (self.locationMgr == nil) {
        self.locationMgr = [[RNNLocationPermission alloc] init];
    }

    [self.locationMgr requestWithCompletionHandler:resolve];
}

- (void) requestNotification:(RCTPromiseResolveBlock)resolve
{
    if (self.notificationMgr == nil) {
        self.notificationMgr = [[RNNNotificationPermission alloc] init];
    }

    [self.notificationMgr requestWithCompletionHandler:resolve];
}

@end
