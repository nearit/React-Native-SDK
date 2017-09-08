/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import "RNNearIt.h"

#define TAG @"RNNearIT"

#define IS_EMPTY(v) (v == nil || [v length] <= 0)

NSString* const RN_NATIVE_EVENTS_TOPIC = @"RNNearItEvent";

// Local Events topic (used by NotificationCenter to handle incoming notifications)
NSString* const RN_LOCAL_EVENTS_TOPIC = @"RNNearItLocalEvents";

NSString* const EVENT_TYPE_SIMPLE = @"NearIt.Events.SimpleNotification";
NSString* const EVENT_TYPE_CUSTOM_JSON = @"NearIt.Events.CustomJSON";

// Events content
NSString* const EVENT_TYPE = @"type";
NSString* const EVENT_TRACKING_INFO = @"trackingInfo";
NSString* const EVENT_CONTENT = @"content";
NSString* const EVENT_CONTENT_MESSAGE = @"message";
NSString* const EVENT_CONTENT_DATA = @"data";
NSString* const EVENT_FROM_USER_ACTION = @"fromUserAction";


// Recipe Statuses
NSString* const RECIPE_STATUS_NOTIFIED = @"RECIPE_STATUS_NOTIFIED";
NSString* const RECIPE_STATUS_ENGAGED = @"RECIPE_STATUS_ENGAGED";

// Error codes
NSString* const E_REFRESH_CONFIG_ERROR = @"E_REFRESH_CONFIG_ERROR";
NSString* const E_START_RADAR_ERROR = @"E_START_RADAR_ERROR";
NSString* const E_STOP_RADAR_ERROR = @"E_STOP_RADAR_ERROR";
NSString* const E_SEND_TRACKING_ERROR = @"E_SEND_TRACKING_ERROR";
NSString* const E_SEND_FEEDBACK_ERROR = @"E_SEND_FEEDBACK_ERROR";
NSString* const E_USER_PROFILE_GET_ERROR = @"E_USER_PROFILE_GET_ERROR";
NSString* const E_USER_PROFILE_SET_ERROR = @"E_USER_PROFILE_SET_ERROR";
NSString* const E_USER_PROFILE_RESET_ERROR = @"E_USER_PROFILE_RESET_ERROR";
NSString* const E_USER_PROFILE_CREATE_ERROR = @"E_USER_PROFILE_CREATE_ERROR";
NSString* const E_USER_PROFILE_DATA_ERROR = @"E_USER_PROFILE_DATA_ERROR";

@implementation RNNearIt

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

- (instancetype) init
{
    self = [super init];

    if (self != nil) {
        // Set up internal listener to send notification over bridge
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(handleNotificationReceived:)
                                                     name:RN_LOCAL_EVENTS_TOPIC
                                                   object:nil];
        
        [NITManager defaultManager].delegate = self;
    }

    return self;
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"NativeEventsTopic": RN_NATIVE_EVENTS_TOPIC,
             @"Events": @{
                        @"SimpleNotification": EVENT_TYPE_SIMPLE,
                        @"CustomJson": EVENT_TYPE_CUSTOM_JSON
                     },
             @"EventContent": @{
                        EVENT_TYPE: EVENT_TYPE,
                        EVENT_TRACKING_INFO: EVENT_TRACKING_INFO,
                        EVENT_CONTENT: EVENT_CONTENT,
                        EVENT_CONTENT_MESSAGE: EVENT_CONTENT_MESSAGE,
                        EVENT_CONTENT_DATA: EVENT_CONTENT_DATA
                     },
             @"Statuses": @{
                        RECIPE_STATUS_NOTIFIED: NITRecipeNotified,
                        RECIPE_STATUS_ENGAGED: NITRecipeEngaged
                     }
            };
}

// MARK: RCT_EventEmitter

- (NSArray<NSString *> *)supportedEvents
{
    return @[RN_NATIVE_EVENTS_TOPIC];
}

- (void) sendEventWithContent:(NSDictionary* _Nonnull) content NITEventType:(NSString* _Nonnull) eventType trackingInfo:(NITTrackingInfo* _Nonnull) trackingInfo fromUserAction:(BOOL) fromUserAction
{
    NSData* trackingInfoData = [NSKeyedArchiver archivedDataWithRootObject:trackingInfo];
    NSString* trackingInfoB64 = [trackingInfoData base64EncodedStringWithOptions:0];
    
    NSDictionary* event = @{
                            EVENT_TYPE: eventType,
                            EVENT_CONTENT: content,
                            EVENT_TRACKING_INFO: trackingInfoB64,
                            EVENT_FROM_USER_ACTION: [NSNumber numberWithBool:fromUserAction]
                        };
    
    
    [self sendEventWithName:RN_NATIVE_EVENTS_TOPIC
                       body:event];
}

// MARK: NITManagerDelegate

- (void)manager:(NITManager *)manager eventWithContent:(id)content trackingInfo:(NITTrackingInfo *)trackingInfo
{
    [self handleNearContent:content trackingInfo:trackingInfo fromUserAction:NO];
}

- (void)manager:(NITManager *)manager eventFailureWithError:(NSError *)error
{
    // handle errors (only for information purpose)
}


// MARK: NearIT Config

RCT_EXPORT_METHOD(refreshConfig: (RCTPromiseResolveBlock) resolve
                       rejecter: (RCTPromiseRejectBlock) reject)
{
    [[NITManager defaultManager] refreshConfigWithCompletionHandler:^(NSError * _Nullable error) {
        if (!error) {
            resolve([NSNull null]);
        } else {
            reject(E_REFRESH_CONFIG_ERROR, @"refreshConfig failed", nil);
        }
    }];
}

// MARK: NearIT Radar

RCT_EXPORT_METHOD(startRadar: (RCTPromiseResolveBlock) resolve
                   rejection: (RCTPromiseRejectBlock) reject)
{
    [[NITManager defaultManager] start];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(stopRadar: (RCTPromiseResolveBlock) resolve
                  rejection: (RCTPromiseRejectBlock) reject)
{
    [[NITManager defaultManager] stop];
    resolve([NSNull null]);
}

// MARK: NearIT Trackings

RCT_EXPORT_METHOD(sendTracking: (NSString* _Nonnull) trackingInfoB64
                        status: (NSString* _Nonnull) status
                    resolution: (RCTPromiseResolveBlock) resolve
                     rejection: (RCTPromiseRejectBlock) reject)
{
    
    if (IS_EMPTY(trackingInfoB64)) {
        reject(E_SEND_TRACKING_ERROR, @"Missing trackingInfo parameter", nil);
    } else {
        NSData* trackingInfoData = [[NSData alloc] initWithBase64EncodedString:trackingInfoB64
                                                                       options:NSDataBase64DecodingIgnoreUnknownCharacters];
        
        NITTrackingInfo *trackingInfo = [NSKeyedUnarchiver unarchiveObjectWithData:trackingInfoData];
        
        if (trackingInfo) {
            NITLogD(TAG, @"NITManager :: track event (%@) with trackingInfo (%@)", status, trackingInfo);
            [[NITManager defaultManager] sendTrackingWithTrackingInfo:trackingInfo event:status];
            resolve([NSNull null]);
        } else {
            NITLogD(TAG, @"NITManager :: failed to send tracking for event (%@) with trackingInfo (%@)", status, trackingInfo);
            reject(E_SEND_TRACKING_ERROR, @"Failed to send tracking", nil);
        }
    }
}

// MARK: NearIT UserProfiling

RCT_EXPORT_METHOD(getUserProfileId: (RCTPromiseResolveBlock) resolve
                         rejection: (RCTPromiseRejectBlock) reject)
{
    resolve([[NITManager defaultManager] profileId]);
}

RCT_EXPORT_METHOD(setUserProfileId: (NSString* _Nonnull) profileId
                        resolution: (RCTPromiseResolveBlock) resolve
                         rejection: (RCTPromiseRejectBlock) reject)
{
    [[NITManager defaultManager] setProfileId:profileId];
    resolve(profileId);
}

RCT_EXPORT_METHOD(resetUserProfile: (RCTPromiseResolveBlock) resolve
                         rejection: (RCTPromiseRejectBlock) reject)
{
    [[NITManager defaultManager] resetProfile];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(setUserData: (NSDictionary* _Nonnull) userData
                   resolution: (RCTPromiseResolveBlock) resolve
                    rejection: (RCTPromiseRejectBlock) reject)
{
    [[NITManager defaultManager] setBatchUserDataWithDictionary:userData completionHandler:^(NSError * _Nullable error) {
        if (!error) {
            resolve([NSNull null]);
        } else {
            reject(E_USER_PROFILE_DATA_ERROR, @"Could NOT set UserData", error);
        }
    }];
}

// MARK: NearIT Permissions request

RCT_EXPORT_METHOD(requestNotificationPermission:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    if (RCTRunningInAppExtension()) {
        return;
    }
    
    if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_9_x_Max) {
        UIUserNotificationType allNotificationTypes = (UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge);
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:allNotificationTypes categories:nil];
        
        [RCTSharedApplication() registerUserNotificationSettings:settings];
        
        // Unfortunately on iOS 9 or below, there's no way to tell whether the user accepted or
        // rejected the permissions popup
        resolve(@(YES));
    } else {
        // iOS 10 or later
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
        // For iOS 10 display notification (sent via APNS)
        [UNUserNotificationCenter currentNotificationCenter].delegate = self;
        UNAuthorizationOptions authOptions = UNAuthorizationOptionAlert | UNAuthorizationOptionSound | UNAuthorizationOptionBadge;
        
        [[UNUserNotificationCenter currentNotificationCenter] requestAuthorizationWithOptions:authOptions completionHandler:^(BOOL granted, NSError * _Nullable error) {
            resolve(@(granted));
        }];
#endif
    }
    
    [RCTSharedApplication() registerForRemoteNotifications];
}

// MARK: NearIT Recipes handling

- (BOOL)handleNearContent: (id _Nonnull) content trackingInfo: (NITTrackingInfo* _Nonnull) trackingInfo fromUserAction: (BOOL) fromUserAction
{
    if ([content isKindOfClass:[NITSimpleNotification class]]) {
        
        // Simple notification
        NITSimpleNotification *simple = (NITSimpleNotification*)content;
        
        NSString* message = [simple message];
        if (!message) {
            message = @"";
        }
        
        NITLogI(TAG, @"simple message \"%@\" with trackingInfo %@", message, trackingInfo);
        
        NSDictionary* eventContent = @{
                                       EVENT_CONTENT_MESSAGE: message
                                    };
        
        [self sendEventWithContent:eventContent
                      NITEventType:EVENT_TYPE_SIMPLE
                      trackingInfo:trackingInfo
                    fromUserAction:fromUserAction];
        
        return YES;
        
    } else if ([content isKindOfClass:[NITCustomJSON class]]) {
        
        // Custom JSON notification
        NITCustomJSON *custom = (NITCustomJSON*)content;
        NITLogI(TAG, @"JSON message %@ trackingInfo %@", [custom content], trackingInfo);
        
        NSString* message = @""; // TODO Update when SDK sends this field
        
        NSDictionary* eventContent = @{
                                       EVENT_CONTENT_MESSAGE: message,
                                       EVENT_CONTENT_DATA: [custom content]
                                    };
        
        [self sendEventWithContent:eventContent
                      NITEventType:EVENT_TYPE_CUSTOM_JSON
                      trackingInfo:trackingInfo
                    fromUserAction:fromUserAction];
        
        return YES;
    } else {
        // unhandled content type
        NSString* message = [NSString stringWithFormat:@"unknown content type %@ trackingInfo %@", content, trackingInfo];
        NITLogW(TAG, message);
        
        return NO;
    }
}


// MARK: Internal notification handling

- (void)handleNotificationReceived:(NSNotification *) notification
{
    NSLog(@"handleNotificationReceived: %@", notification);
    
    NSMutableDictionary* data = notification.userInfo[@"data"];
    
    [[NITManager defaultManager] processRecipeWithUserInfo:data completion:^(id  _Nullable content, NITTrackingInfo * _Nullable trackingInfo, NSError * _Nullable error) {
        // Handle push notification message
        NITLogD(TAG, @"didReceiveRemoteNotification content=%@ trackingInfo=%@ error=%@", content, trackingInfo, error);
        
        if (error) {
            [self manager:[NITManager defaultManager] eventFailureWithError:error];
        } else {
            [self handleNearContent:content trackingInfo:trackingInfo fromUserAction:@(RCTSharedApplication().applicationState == UIApplicationStateInactive)];
        }
        
    }];
    
}

// MARK: Push Notification handling

+ (void)didReceiveRemoteNotification:(NSDictionary* _Nonnull) userInfo
{
    NSMutableDictionary* data = [[NSMutableDictionary alloc] initWithDictionary: userInfo];
    [data setValue:@YES forKey:@"fromUserAction"];

    [[NSNotificationCenter defaultCenter] postNotificationName:RN_LOCAL_EVENTS_TOPIC
                                                        object:self
                                                      userInfo:@{@"data": data}];
}

+ (void)didReceiveLocalNotification:(UILocalNotification* _Nonnull) notification
{
    NSMutableDictionary* data = [[NSMutableDictionary alloc] initWithDictionary: notification.userInfo];
    [data setValue:@YES forKey:@"fromUserAction"];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:RN_LOCAL_EVENTS_TOPIC
                                                        object:self
                                                      userInfo:@{@"data": data}];
}

+ (void)didReceiveNotificationResponse:(UNNotificationResponse* _Nonnull) response withCompletionHandler:(void (^ _Nonnull)())completionHandler
{
    NSMutableDictionary* data = [[NSMutableDictionary alloc] initWithDictionary: response.notification.request.content.userInfo];
    [data setValue:@YES forKey:@"fromUserAction"];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:RN_LOCAL_EVENTS_TOPIC
                                                        object:self
                                                      userInfo:@{@"data": data, @"completionHandler": completionHandler}];
}

@end

