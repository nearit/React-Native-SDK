/*
 * Copyright (c) 2018 Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/

#import "RNNLocationPermission.h"
#import "RCTConvert+RNNPermissionStatus.h"
#import <CoreLocation/CoreLocation.h>

@interface RNNLocationPermission() <CLLocationManagerDelegate>
@property (strong, nonatomic) CLLocationManager* locationManager;
@property (copy) void (^completionHandler)(NSString *);
@end

@implementation RNNLocationPermission

+ (NSString *)getStatus {
    int status = [CLLocationManager authorizationStatus];
    switch (status) {
        case kCLAuthorizationStatusAuthorizedAlways:
            return RNNStatusGrantedAlways;
        case kCLAuthorizationStatusAuthorizedWhenInUse:
            return RNNStatusGrantedWhenInUse;
        case kCLAuthorizationStatusUndetermined:
            return RNNStatusNeverAsked;
        default:
            return RNNStatusDenied;
    }
}

- (void)requestWithCompletionHandler:(void (^)(NSString *))completionHandler {
    NSString *status = [RNNLocationPermission getStatus];
    if (status == RNNPermissionStatusUndetermined) {
        self.completionHandler = completionHandler;

        if (self.locationManager == nil) {
            self.locationManager = [[CLLocationManager alloc] init];
            self.locationManager.delegate = self;
        }

        [self.locationManager requestAlwaysAuthorization];
    } else {
        completionHandler(status);
    }
}

-(void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
    if (status != kCLAuthorizationStatusNotDetermined) {
        if (self.locationManager) {
            self.locationManager.delegate = nil;
            self.locationManager = nil;
        }

        if (self.completionHandler) {
            //for some reason, checking permission right away returns denied. need to wait a tiny bit
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 0.1 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
                self.completionHandler([RNNLocationPermission getStatus]);
                self.completionHandler = nil;
            });
        }
    }
}

@end