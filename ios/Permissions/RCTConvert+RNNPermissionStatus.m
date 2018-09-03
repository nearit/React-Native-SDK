/*
 * Copyright (c) 2018 Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import "RCTConvert+RNNPermissionStatus.h"

@implementation RCTConvert (RNNPermissionStatus)

RCT_ENUM_CONVERTER(RNNPermissionType, (@{ @"location" : @(RNNPermissionTypeLocation),
                                @"notification" : @(RNNPermissionTypeNotification)
                                }),
                                RNNPermissionTypeUnknown, integerValue)

@end