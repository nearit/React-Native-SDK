/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import "RNNotificationsQueue.h"

@implementation RNNotificationsQueue

NSMutableArray<NSDictionary *>* _Nullable notificationsQueue;
BOOL isReady;

+ (nonnull instancetype)defaultQueue {
    static RNNotificationsQueue* sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [self new];
    });

    return sharedInstance;
}

- (instancetype)init {
    notificationsQueue = [NSMutableArray new];
    isReady = NO;
    
    return self;
}

- (BOOL)addNotification:(NSDictionary* _Nonnull)notification {
    if (!notificationsQueue || isReady) {
        return NO;
    }
    
    [notificationsQueue insertObject:notification atIndex:0];
    return YES;
}

- (NSDictionary*)dispatchSingleNotification {
    if (!notificationsQueue || notificationsQueue.count == 0) return nil;
    
    NSDictionary* notification = [notificationsQueue lastObject];
    [notificationsQueue removeLastObject];
    
    return notification;
}

- (void)dispatchNotificationsQueue:(void (^)(NSDictionary* _Nonnull))block {
    NSDictionary* notification;

    while ((notification = [self dispatchSingleNotification]) != nil) {
        block(notification);
    }

    isReady = YES;
}

@end
