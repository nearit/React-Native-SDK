//
//  RNNearItConsts.m
//  RNNearIt
//
//  Created by Federico Boschini on 11/06/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "RNNearItConsts.h"

@implementation RNNearItConsts

// Event topic
NSString* RN_NATIVE_EVENTS_TOPIC = @"RNNearItEvent";
NSString* RN_NATIVE_NOTIFICATION_HISTORY_TOPIC = @"RNNearItNotificationHistory";

// Event type
NSString* EVENT_TYPE_SIMPLE = @"NearIt.Events.SimpleNotification";
NSString* EVENT_TYPE_CUSTOM_JSON = @"NearIt.Events.CustomJSON";
NSString* EVENT_TYPE_COUPON = @"NearIt.Events.Coupon";
NSString* EVENT_TYPE_CONTENT = @"NearIt.Events.Content";
NSString* EVENT_TYPE_FEEDBACK = @"NearIt.Events.Feedback";

// Event content
NSString* EVENT_TYPE = @"type";
NSString* EVENT_TRACKING_INFO = @"trackingInfo";
NSString* EVENT_CONTENT_MESSAGE = @"message";

NSString* EVENT_CONTENT = @"content";

NSString* EVENT_FROM_USER_ACTION = @"fromUserAction";
NSString* EVENT_STATUS = @"status";

// Common
NSString* EVENT_CONTENT_TITLE = @"title";
// Image
NSString* EVENT_IMAGE = @"image";
NSString* EVENT_IMAGE_FULL_SIZE = @"fullSize";
NSString* EVENT_IMAGE_SQUARE_SIZE = @"squareSize";

// Content
NSString* EVENT_CONTENT_TEXT = @"text";
NSString* EVENT_CONTENT_CTA = @"cta";
NSString* EVENT_CONTENT_CTA_LABEL = @"label";
NSString* EVENT_CONTENT_CTA_URL = @"url";

// Coupon
NSString* EVENT_COUPON_DESCRIPTION = @"description";
NSString* EVENT_COUPON_VALUE = @"value";
NSString* EVENT_COUPON_EXPIRES_AT = @"expiresAt";
NSString* EVENT_COUPON_REDEEMABLE_FROM = @"redeemableFrom";
NSString* EVENT_COUPON_SERIAL = @"serial";
NSString* EVENT_COUPON_CLAIMED_AT = @"claimedAt";
NSString* EVENT_COUPON_REDEEMED_AT = @"reddemedAt";

// Feedback
NSString* EVENT_FEEDBACK_QUESTION = @"feedbackQuestion";
NSString* EVENT_FEEDBACK_ID = @"feedbackId";

// Notification History
NSString* NOTIFICATION_HISTORY = @"notificationHistory";
NSString* NOTIFICATION_HISTORY_READ = @"read";
NSString* NOTIFICATION_HISTORY_TIMESTAMP = @"timestamp";
NSString* NOTIFICATION_HISTORY_IS_NEW = @"isNew";
NSString* NOTIFICATION_HISTORY_CONTENT = @"notificationContent";

// Permissions
NSString* PERMISSIONS_LOCATION_PERMISSION = @"location";
NSString* PERMISSIONS_NOTIFICATIONS_PERMISSION = @"notifications";
NSString* PERMISSIONS_BLUETOOTH = @"bluetooth";
NSString* PERMISSIONS_LOCATION_SERVICES = @"locationServices";
NSString* PERMISSIONS_LOCATION_ALWAYS = @"always";
NSString* PERMISSIONS_LOCATION_DENIED = @"denied";
NSString* PERMISSIONS_LOCATION_WHEN_IN_USE = @"whenInUse";

// Error codes
NSString* E_SEND_FEEDBACK_ERROR = @"E_SEND_FEEDBACK_ERROR";
NSString* E_PROFILE_ID_GET_ERROR = @"E_PROFILE_ID_GET_ERROR";
NSString* E_PROFILE_ID_RESET_ERROR = @"E_PROFILE_ID_RESET_ERROR";
NSString* E_PROFILE_GET_USER_DATA_ERROR = @"E_PROFILE_GET_USER_DATA_ERROR";
NSString* E_COUPONS_PARSING_ERROR = @"E_COUPONS_PARSING_ERROR";
NSString* E_COUPONS_RETRIEVAL_ERROR = @"E_COUPONS_RETRIEVAL_ERROR";
NSString* E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR = @"E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR";
NSString* E_OPT_OUT_ERROR = @"E_OPT_OUT_ERROR";

@end
