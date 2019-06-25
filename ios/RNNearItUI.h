//
//  RNNearItUI.h
//  RNNearIt
//
//  Created by Federico Boschini on 12/06/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTUtils.h>
#elif __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#import "RCTUtils.h"
#endif

#import <UserNotifications/UserNotifications.h>
#import <CoreLocation/CoreLocation.h>

#if __has_include(<NearITSDK/NearITSDK.h>)
#import <NearITSDK/NearITSDK.h>
#elif __has_include("NearIT.h")
#import "NearIT.h"
#endif

#import <NearUIBinding/NearUIBinding-Swift.h>

#import "RNNearItUtils.h"
#import "RNNearItConsts.h"

@interface RNNearItUI : NSObject <RCTBridgeModule, NITPermissionsViewControllerDelegate>

@property RCTPromiseResolveBlock permissionsResolve;

- (NSDictionary*)getPermissionsStatus:(BOOL)notificationGranted;
- (BOOL)isLocationGranted;

@end
