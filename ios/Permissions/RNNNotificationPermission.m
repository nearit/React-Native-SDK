/*
 * Copyright (c) 2018 Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import "RNNNotificationPermission.h"

static NSString* RNNDidAskForNotification = @"RNNDidAskForNotification";

@interface RNNNotificationPermission()
@property (copy) void (^completionHandler)(NSString*);
@end

@implementation RNNNotificationPermission

+ (NSString *)getStatus {
    BOOL didAskForPermission = [[NSUserDefaults standardUserDefaults] boolForKey:RNNDidAskForNotification];
    BOOL isEnabled = [[[UIApplication sharedApplication] currentUserNotificationSettings] types] != UIUserNotificationTypeNone;

    if (isEnabled) {
        return RNNStatusGrantedAlways;
    } else {
        return didAskForPermission ? RNNStatusDenied : RNNStatusNeverAsked;
    }
}

- (void)requestWithCompletionHandler:(void (^)(NSString*))completionHandler {
    NSString *status = [self.class getStatus];

    if (status == RNNStatusNeverAsked) {
        self.completionHandler = completionHandler;

        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(applicationDidBecomeActive)
                                                     name:UIApplicationDidBecomeActiveNotification
                                                   object:nil];

        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:(UIUserNotificationTypeSound |    UIUserNotificationTypeAlert | UIUserNotificationTypeBadge) categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
        [[UIApplication sharedApplication] registerForRemoteNotifications];

        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:RNNDidAskForNotification];
        [[NSUserDefaults standardUserDefaults] synchronize];
    } else {
        completionHandler(status);
    }
}

- (void)applicationDidBecomeActive {
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:UIApplicationDidBecomeActiveNotification
                                                  object:nil];

    if (self.completionHandler) {
        //for some reason, checking permission right away returns denied. need to wait a tiny bit
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 0.1 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
            self.completionHandler([self.class getStatus]);
            self.completionHandler = nil;
        });
    }
}

@end