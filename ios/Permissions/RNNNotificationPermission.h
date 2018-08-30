/*
 * Copyright (c) 2018 Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import <Foundation/Foundation.h>
#import "RCTConvert+RNNPermissionStatus.h"

@interface RNNNotificationPermission : NSObject

+ (NSString *)getStatus;
- (void)requestWithCompletionHandler:(void (^)(NSString*))completionHandler;

@end