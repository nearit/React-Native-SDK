/*
 * Copyright (c) 2018 Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#if __has_include(<React/RCTConvert.h>)
  #import <React/RCTConvert.h>
#elif __has_include("React/RCTConvert.h")
  #import "React/RCTConvert.h"
#else
  #import "RCTConvert.h"
#endif

static NSString* RNNStatusNeverAsked = @"never_asked";
static NSString* RNNStatusDenied = @"denied";
static NSString* RNNStatusGrantedAlways = @"always";
static NSString* RNNStatusGrantedWhenInUse = @"when_in_use";

typedef NS_ENUM(NSInteger, RNNPermissionType) {
    RNNPermissionTypeUnknown,
    RNNPermissionTypeLocation,
    RNNPermissionTypeNotification
};

@interface RCTConvert (RNNPermissionStatus)

@end