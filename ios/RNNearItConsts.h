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
extern const NSString* RN_NATIVE_EVENTS_TOPIC;
extern const NSString* RN_NATIVE_NOTIFICATION_HISTORY_TOPIC;

// Event type
extern const NSString* EVENT_TYPE_SIMPLE;
extern const NSString* EVENT_TYPE_CUSTOM_JSON;
extern const NSString* EVENT_TYPE_COUPON;
extern const NSString* EVENT_TYPE_CONTENT;
extern const NSString* EVENT_TYPE_FEEDBACK;

// Event content
extern const NSString* EVENT_TYPE;
extern const NSString* EVENT_TRACKING_INFO;
extern const NSString* EVENT_CONTENT_MESSAGE;

extern const NSString* EVENT_CONTENT;

extern const NSString* EVENT_FROM_USER_ACTION;
extern const NSString* EVENT_STATUS;

// Common
extern const NSString* EVENT_CONTENT_TITLE;
// Image
extern const NSString* EVENT_IMAGE;
extern const NSString* EVENT_IMAGE_FULL_SIZE;
extern const NSString* EVENT_IMAGE_SQUARE_SIZE;

// Content
extern const NSString* EVENT_CONTENT_TEXT;
extern const NSString* EVENT_CONTENT_CTA;
extern const NSString* EVENT_CONTENT_CTA_LABEL;
extern const NSString* EVENT_CONTENT_CTA_URL;

// Coupon
extern const NSString* EVENT_COUPON_DESCRIPTION;
extern const NSString* EVENT_COUPON_VALUE;
extern const NSString* EVENT_COUPON_EXPIRES_AT;
extern const NSString* EVENT_COUPON_REDEEMABLE_FROM;
extern const NSString* EVENT_COUPON_SERIAL;
extern const NSString* EVENT_COUPON_CLAIMED_AT;
extern const NSString* EVENT_COUPON_REDEEMED_AT;

// Feedback
extern const NSString* EVENT_FEEDBACK_QUESTION;
extern const NSString* EVENT_FEEDBACK_ID;

// Notification History
extern const NSString* NOTIFICATION_HISTORY_READ;
extern const NSString* NOTIFICATION_HISTORY_TIMESTAMP;
extern const NSString* NOTIFICATION_HISTORY_IS_NEW;
extern const NSString* NOTIFICATION_HISTORY_CONTENT;

// Permissions
extern const NSString* PERMISSIONS_LOCATION_PERMISSION;
extern const NSString* PERMISSIONS_NOTIFICATIONS_PERMISSION;
extern const NSString* PERMISSIONS_BLUETOOTH;
extern const NSString* PERMISSIONS_LOCATION_SERVICES;
extern const NSString* PERMISSIONS_LOCATION_ALWAYS;
extern const NSString* PERMISSIONS_LOCATION_DENIED;
extern const NSString* PERMISSIONS_LOCATION_WHEN_IN_USE;

// Error codes
extern const NSString* E_SEND_FEEDBACK_ERROR;
extern const NSString* E_PROFILE_ID_GET_ERROR;
extern const NSString* E_PROFILE_ID_RESET_ERROR;
extern const NSString* E_PROFILE_GET_USER_DATA_ERROR;
extern const NSString* E_COUPONS_PARSING_ERROR;
extern const NSString* E_COUPONS_RETRIEVAL_ERROR;
extern const NSString* E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR;
extern const NSString* E_OPT_OUT_ERROR;

@end
