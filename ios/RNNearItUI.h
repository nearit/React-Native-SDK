//
//  RNNearItUI.h
//  RNNearIt
//
//  Created by Federico Boschini on 12/06/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTUtils.h>

#import <UserNotifications/UserNotifications.h>
#import <CoreLocation/CoreLocation.h>

#import <NearITSDK/NearITSDK.h>
#import <NearUIBinding/NearUIBinding-Swift.h>

#import "RNNearItUtils.h"
#import "RNNearItConsts.h"

@interface RNNearItUI : RCTEventEmitter <RCTBridgeModule, NITPermissionsViewControllerDelegate>

- (NSDictionary*)getPermissionsStatus;
- (BOOL)isNotificationGranted;
- (BOOL)isLocationGranted;

@end
