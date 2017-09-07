/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import "RNNearIt.h"
#import <NearITSDK/NearITSDK.h>

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

- (void) sendEventWithContent: (NSDictionary* _Nonnull) content
{
    [self sendEventWithName:RN_NATIVE_EVENTS_TOPIC body:content];
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

// MARK: Internal notification handling

- (void)handleNotificationReceived:(NSNotification *) notification
{
    NSLog(@"handleNotificationReceived: %@", notification);
    
    // Send event to ReactJS
    [self sendEventWithContent:@{@"Content": @"I'm an event from RNNearIT"}];
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

