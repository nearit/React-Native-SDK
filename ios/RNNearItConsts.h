//
//  RNNearItConsts.h
//  RNNearIt
//
//  Created by Federico Boschini on 11/06/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RNNearItConsts : NSObject

// Event topic
extern NSString* const RN_NATIVE_EVENTS_TOPIC;
extern NSString* const RN_NATIVE_NOTIFICATION_HISTORY_TOPIC;

// Event type
extern NSString* const EVENT_TYPE_SIMPLE;
extern NSString* const EVENT_TYPE_CUSTOM_JSON;
extern NSString* const EVENT_TYPE_COUPON;
extern NSString* const EVENT_TYPE_CONTENT;
extern NSString* const EVENT_TYPE_FEEDBACK;

// Event content
extern NSString* const EVENT_TYPE;
extern NSString* const EVENT_TRACKING_INFO;
extern NSString* const EVENT_CONTENT_MESSAGE;

extern NSString* const EVENT_CONTENT;

extern NSString* const EVENT_FROM_USER_ACTION;
extern NSString* const EVENT_STATUS;

// Common
extern NSString* const EVENT_CONTENT_TITLE;
// Image
extern NSString* const EVENT_IMAGE;
extern NSString* const EVENT_IMAGE_FULL_SIZE;
extern NSString* const EVENT_IMAGE_SQUARE_SIZE;

// Content
extern NSString* const EVENT_CONTENT_TEXT;
extern NSString* const EVENT_CONTENT_CTA;
extern NSString* const EVENT_CONTENT_CTA_LABEL;
extern NSString* const EVENT_CONTENT_CTA_URL;

// Coupon
extern NSString* const EVENT_COUPON_DESCRIPTION;
extern NSString* const EVENT_COUPON_VALUE;
extern NSString* const EVENT_COUPON_EXPIRES_AT;
extern NSString* const EVENT_COUPON_REDEEMABLE_FROM;
extern NSString* const EVENT_COUPON_SERIAL;
extern NSString* const EVENT_COUPON_CLAIMED_AT;
extern NSString* const EVENT_COUPON_REDEEMED_AT;

// Feedback
extern NSString* const EVENT_FEEDBACK_QUESTION;
extern NSString* const EVENT_FEEDBACK_ID;

// Notification History
extern NSString* const NOTIFICATION_HISTORY;
extern NSString* const NOTIFICATION_HISTORY_READ;
extern NSString* const NOTIFICATION_HISTORY_TIMESTAMP;
extern NSString* const NOTIFICATION_HISTORY_IS_NEW;
extern NSString* const NOTIFICATION_HISTORY_CONTENT;

// Permissions
extern NSString* const PERMISSIONS_LOCATION_PERMISSION;
extern NSString* const PERMISSIONS_NOTIFICATIONS_PERMISSION;
extern NSString* const PERMISSIONS_BLUETOOTH;
extern NSString* const PERMISSIONS_LOCATION_SERVICES;
extern NSString* const PERMISSIONS_LOCATION_ALWAYS;
extern NSString* const PERMISSIONS_LOCATION_DENIED;
extern NSString* const PERMISSIONS_LOCATION_WHEN_IN_USE;

// Error codes
extern NSString* const E_SEND_FEEDBACK_ERROR;
extern NSString* const E_PROFILE_ID_GET_ERROR;
extern NSString* const E_PROFILE_ID_RESET_ERROR;
extern NSString* const E_PROFILE_GET_USER_DATA_ERROR;
extern NSString* const E_COUPONS_PARSING_ERROR;
extern NSString* const E_COUPONS_RETRIEVAL_ERROR;
extern NSString* const E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR;
extern NSString* const E_OPT_OUT_ERROR;

@end
