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
NSString* const RN_NATIVE_EVENTS_TOPIC = @"RNNearItEvent";
NSString* const RN_NATIVE_NOTIFICATION_HISTORY_TOPIC = @"RNNearItNotificationHistory";

// Event type
NSString* const EVENT_TYPE_SIMPLE = @"NearIt.Events.SimpleNotification";
NSString* const EVENT_TYPE_CUSTOM_JSON = @"NearIt.Events.CustomJSON";
NSString* const EVENT_TYPE_COUPON = @"NearIt.Events.Coupon";
NSString* const EVENT_TYPE_CONTENT = @"NearIt.Events.Content";
NSString* const EVENT_TYPE_FEEDBACK = @"NearIt.Events.Feedback";

// Event content
NSString* const EVENT_TYPE = @"type";
NSString* const EVENT_TRACKING_INFO = @"trackingInfo";
NSString* const EVENT_CONTENT_MESSAGE = @"message";

NSString* const EVENT_CONTENT = @"content";

NSString* const EVENT_FROM_USER_ACTION = @"fromUserAction";
NSString* const EVENT_STATUS = @"status";

// Common
NSString* const EVENT_CONTENT_TITLE = @"title";
// Image
NSString* const EVENT_IMAGE = @"image";
NSString* const EVENT_IMAGE_FULL_SIZE = @"fullSize";
NSString* const EVENT_IMAGE_SQUARE_SIZE = @"squareSize";

// Content
NSString* const EVENT_CONTENT_TEXT = @"text";
NSString* const EVENT_CONTENT_CTA = @"cta";
NSString* const EVENT_CONTENT_CTA_LABEL = @"label";
NSString* const EVENT_CONTENT_CTA_URL = @"url";

// Coupon
NSString* const EVENT_COUPON_DESCRIPTION = @"description";
NSString* const EVENT_COUPON_VALUE = @"value";
NSString* const EVENT_COUPON_EXPIRES_AT = @"expiresAt";
NSString* const EVENT_COUPON_REDEEMABLE_FROM = @"redeemableFrom";
NSString* const EVENT_COUPON_SERIAL = @"serial";
NSString* const EVENT_COUPON_CLAIMED_AT = @"claimedAt";
NSString* const EVENT_COUPON_REDEEMED_AT = @"reddemedAt";

// Feedback
NSString* const EVENT_FEEDBACK_QUESTION = @"feedbackQuestion";
NSString* const EVENT_FEEDBACK_ID = @"feedbackId";

// Notification History
NSString* const NOTIFICATION_HISTORY = @"notificationHistory";
NSString* const NOTIFICATION_HISTORY_READ = @"read";
NSString* const NOTIFICATION_HISTORY_TIMESTAMP = @"timestamp";
NSString* const NOTIFICATION_HISTORY_IS_NEW = @"isNew";
NSString* const NOTIFICATION_HISTORY_CONTENT = @"notificationContent";

// Permissions
NSString* const PERMISSIONS_LOCATION_PERMISSION = @"location";
NSString* const PERMISSIONS_NOTIFICATIONS_PERMISSION = @"notifications";
NSString* const PERMISSIONS_BLUETOOTH = @"bluetooth";
NSString* const PERMISSIONS_LOCATION_SERVICES = @"locationServices";
NSString* const PERMISSIONS_LOCATION_ALWAYS = @"always";
NSString* const PERMISSIONS_LOCATION_DENIED = @"denied";
NSString* const PERMISSIONS_LOCATION_WHEN_IN_USE = @"whenInUse";

// Error codes
NSString* const E_SEND_FEEDBACK_ERROR = @"E_SEND_FEEDBACK_ERROR";
NSString* const E_PROFILE_ID_GET_ERROR = @"E_PROFILE_ID_GET_ERROR";
NSString* const E_PROFILE_ID_RESET_ERROR = @"E_PROFILE_ID_RESET_ERROR";
NSString* const E_PROFILE_GET_USER_DATA_ERROR = @"E_PROFILE_GET_USER_DATA_ERROR";
NSString* const E_COUPONS_PARSING_ERROR = @"E_COUPONS_PARSING_ERROR";
NSString* const E_COUPONS_RETRIEVAL_ERROR = @"E_COUPONS_RETRIEVAL_ERROR";
NSString* const E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR = @"E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR";
NSString* const E_OPT_OUT_ERROR = @"E_OPT_OUT_ERROR";

@end
