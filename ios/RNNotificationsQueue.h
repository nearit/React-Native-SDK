/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#ifndef RNNotificationsQueue_h
#define RNNotificationsQueue_h

#import <Foundation/Foundation.h>

@interface RNNotificationsQueue : NSObject

+ (nonnull instancetype)defaultQueue;

- (BOOL)addNotification:(NSDictionary* _Nonnull)notification;
- (void)dispatchNotificationsQueue:(void (^ _Nonnull)(NSDictionary* _Nonnull))block;

@end

#endif /* RNNotificationsQueue_h */
