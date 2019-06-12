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

// MARK: Permissions related methods

RCT_EXPORT_METHOD(requestPermissions: (NSString* _Nullable) explanation)
{
    NITPermissionsViewController *controller = [[NITPermissionsViewController alloc] init];
    controller.delegate = delegate;
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

RCT_EXPORT_METHOD(showCoupons: (NSString* _Nullable) title)
{
    NITCouponListViewController *couponsVC = [[NITCouponListViewController alloc] init];
    if (title != nil) {
        [couponsVC showWithTitle:title];
    } else {
        [couponsVC show];
    }
}

// MARK: Content related methods

RCT_EXPORT_METHOD(showContent)
{
    // TODO:
}

@end
