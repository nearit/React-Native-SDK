/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#ifndef RNNearItBackgroundQueue_h
#define RNNearItBackgroundQueue_h

#import <Foundation/Foundation.h>

@interface RNNearItBackgroundQueue : NSObject

+ (nonnull instancetype)defaultQueue;

- (void)addNotification:(NSDictionary* _Nonnull)notification;
- (void)dispatchNotificationsQueue:(void (^ _Nonnull)(NSDictionary* _Nonnull))block;

@end

#endif /* RNNearItBackgroundQueue_h */
