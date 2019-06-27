//
//  RNNearItUI.m
//  RNNearIt
//
//  Created by Federico Boschini on 12/06/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "RNNearItUI.h"

#define TAG @"RNNearItUI"

@implementation RNNearItUI

RCT_EXPORT_MODULE()

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}



/*
 *  Delegates
 */

#pragma NITPermissionsViewControllerDelegate

- (void)dialogClosedWithLocationGranted:(BOOL)locationGranted notificationsGranted:(BOOL)notificationsGranted {
    if (self.permissionsResolve) {
        NSDictionary* result = [self getPermissionsStatus:notificationsGranted];
        self.permissionsResolve(result);
    }
}

- (void)locationGranted:(BOOL)granted {
    
}

- (void)notificationsGranted:(BOOL)granted {
    
}



/*
 *  React-Native exported methods
 */

// MARK: Permissions related methods

RCT_EXPORT_METHOD(requestPermissions:(NSString* _Nullable)explanation
                  resolution:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    self.permissionsResolve = resolve;
    
    NITPermissionsViewController *controller = [[NITPermissionsViewController alloc] init];
    controller.delegate = self;
    if (explanation != nil) {
        controller.explainText = explanation;
    }
    controller.closeText = @"Close";
    [controller show];
}

// MARK: Notification History related methods

RCT_EXPORT_METHOD(showNotificationHistory: (NSString* _Nullable) title)
{
    NITNotificationHistoryViewController *historyVC = [[NITNotificationHistoryViewController alloc] init];
    if (title != nil) {
        [historyVC showWithTitle:title];
    } else {
        [historyVC show];
    }
}

// MARK: Coupon related methods

RCT_EXPORT_METHOD(showCouponList: (NSString* _Nullable) title)
{
    NITCouponListViewController *couponsVC = [[NITCouponListViewController alloc] init];
    if (title != nil) {
        [couponsVC showWithTitle:title];
    } else {
        [couponsVC show];
    }
}

// MARK: Content related methods

RCT_EXPORT_METHOD(showContent: (NSDictionary* _Nullable) event)
{
    if (event != nil) {
        NSString* type = [event objectForKey:EVENT_TYPE];
        NSDictionary* content = [event objectForKey:EVENT_CONTENT];
        if ([type isEqualToString:EVENT_TYPE_CONTENT]) {
            NITContent* nearContent = [RNNearItUtils unbundleNITContent:content];
            NITTrackingInfo* trackingInfo = [RNNearItUtils unbundleTrackingInfo:[event objectForKey:EVENT_TRACKING_INFO]];
            NITContentViewController *vc = [[NITContentViewController alloc] initWithContent:nearContent trackingInfo:trackingInfo];
            [vc show];
        } else if ([type isEqualToString:EVENT_TYPE_FEEDBACK]) {
            NITFeedback* feedback = [RNNearItUtils unbundleNITFeedback:content];
            NITFeedbackViewController *vc = [[NITFeedbackViewController alloc] initWithFeedback:feedback];
            [vc show];
        } else if ([type isEqualToString:EVENT_TYPE_COUPON]) {
            NITCoupon* coupon = [RNNearItUtils unbundleNITCoupon:content];
            NITCouponViewController* vc = [[NITCouponViewController alloc] initWithCoupon:coupon];
            [vc show];
        } else if ([type isEqualToString:EVENT_TYPE_CUSTOM_JSON]) {
            NSLog(@"Could NOT show content because it is a custom json");
        } else if ([type isEqualToString:EVENT_TYPE_SIMPLE]) {
            NSLog(@"Could NOT show content because it is a simple notification");
        } else {
            NSLog(@"Could NOT show content because content can't be parsed");
        }
    }
}



/*
 *  Private methods
 */

- (NSDictionary*)getPermissionsStatus:(BOOL)notificationGranted
{
    BOOL locationServicesOn = [CLLocationManager locationServicesEnabled];
    BOOL locationGranted = [self isLocationGranted];
    // TODO: bluetooth
    return @{
             PERMISSIONS_LOCATION_PERMISSION: @(locationGranted),
             PERMISSIONS_NOTIFICATIONS_PERMISSION: @(notificationGranted),
             PERMISSIONS_BLUETOOTH: @YES,
             PERMISSIONS_LOCATION_SERVICES: @(locationServicesOn)
             };
}

- (BOOL)isLocationGranted
{
    switch (CLLocationManager.authorizationStatus) {
        case kCLAuthorizationStatusNotDetermined:
            return NO;
            break;
            
        default: {
            BOOL locationPermission = CLLocationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways || CLLocationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse;
            return locationPermission;
            break;
        }
    }
}

@end
