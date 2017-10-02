/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import "RNNearItBackgroundQueue.h"

@implementation RNNearItBackgroundQueue

NSMutableArray<NSDictionary *>* _Nullable notificationsQueue;

+ (nonnull instancetype)defaultQueue {
    static RNNearItBackgroundQueue* sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [self new];
    });

    return sharedInstance;
}

- (instancetype)init {
    notificationsQueue = [NSMutableArray new];
    
    return self;
}

- (void)addNotification:(NSDictionary* _Nonnull)notification {
    if (!notificationsQueue) return;
    [notificationsQueue insertObject:notification atIndex:0];
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
    
    notificationsQueue = nil;
}

@end
