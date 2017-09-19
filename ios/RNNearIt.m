/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import "RNNearIt.h"
#import <CoreLocation/CoreLocation.h>

#define TAG @"RNNearIT"

#define IS_EMPTY(v) (v == nil || [v length] <= 0)

NSString* const RN_NATIVE_EVENTS_TOPIC = @"RNNearItEvent";

// Local Events topic (used by NotificationCenter to handle incoming notifications)
NSString* const RN_LOCAL_EVENTS_TOPIC = @"RNNearItLocalEvents";

// Event types
NSString* const EVENT_TYPE_PERMISSIONS = @"NearIt.Events.PermissionStatus";
NSString* const EVENT_TYPE_SIMPLE = @"NearIt.Events.SimpleNotification";
NSString* const EVENT_TYPE_CUSTOM_JSON = @"NearIt.Events.CustomJSON";
NSString* const EVENT_TYPE_COUPON = @"NearIt.Events.Coupon";
NSString* const EVENT_TYPE_CONTENT = @"NearIt.Events.Content";
NSString* const EVENT_TYPE_FEEDBACK = @"NearIt.Events.Feedback";

// Events content
NSString* const EVENT_TYPE = @"type";
NSString* const EVENT_TRACKING_INFO = @"trackingInfo";
NSString* const EVENT_CONTENT = @"content";
NSString* const EVENT_CONTENT_MESSAGE = @"message";
NSString* const EVENT_CONTENT_DATA = @"data";
NSString* const EVENT_CONTENT_COUPON = @"coupon";
NSString* const EVENT_CONTENT_TEXT = @"text";
NSString* const EVENT_CONTENT_VIDEO = @"video";
NSString* const EVENT_CONTENT_IMAGES = @"images";
NSString* const EVENT_CONTENT_UPLOAD = @"upload";
NSString* const EVENT_CONTENT_AUDIO = @"audio";
NSString* const EVENT_CONTENT_FEEDBACK = @"feedbackId";
NSString* const EVENT_CONTENT_QUESTION = @"feedbackQuestion";
NSString* const EVENT_FROM_USER_ACTION = @"fromUserAction";
NSString* const EVENT_STATUS = @"status";

// Location permission status
NSString* const PERMISSION_LOCATION_GRANTED = @"NearIt.Permissions.Location.Granted";
NSString* const PERMISSION_LOCATION_DENIED = @"NearIt.Permissions.Location.Denied";

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
NSString* const E_COUPONS_RETRIEVAL_ERROR = @"E_COUPONS_RETRIEVAL_ERROR";

// CLLocationManager
CLLocationManager *locationManager;

@implementation RNNearIt {
    BOOL hasListeners;
}

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

        locationManager = [[CLLocationManager alloc]init];
        locationManager.delegate = self;
    }
    
    return self;
}


// Will be called when this module's first listener is added.
-(void)startObserving {
    hasListeners = YES;
    // Set up any upstream listeners or background tasks as necessary
}

// Will be called when this module's last listener is removed, or on dealloc.
-(void)stopObserving {
    hasListeners = NO;
    // Remove upstream listeners, stop unnecessary background tasks
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"NativeEventsTopic": RN_NATIVE_EVENTS_TOPIC,
             @"Events": @{
                        @"PermissionStatus": EVENT_TYPE_PERMISSIONS,
                        @"SimpleNotification": EVENT_TYPE_SIMPLE,
                        @"CustomJson": EVENT_TYPE_CUSTOM_JSON,
                        @"Coupon": EVENT_TYPE_COUPON,
                        @"Content": EVENT_TYPE_CONTENT,
                        @"Feedback": EVENT_TYPE_FEEDBACK
                     },
             @"EventContent": @{
                        @"type": EVENT_TYPE,
                        @"trackingInfo": EVENT_TRACKING_INFO,
                        @"content": EVENT_CONTENT,
                        @"message": EVENT_CONTENT_MESSAGE,
                        @"data": EVENT_CONTENT_DATA,
                        @"coupon": EVENT_CONTENT_COUPON,
                        @"text": EVENT_CONTENT_TEXT,
                        @"images": EVENT_CONTENT_IMAGES,
                        @"video": EVENT_CONTENT_VIDEO,
                        @"upload": EVENT_CONTENT_UPLOAD,
                        @"audio": EVENT_CONTENT_AUDIO,
                        @"feedbackId": EVENT_CONTENT_FEEDBACK,
                        @"question": EVENT_CONTENT_QUESTION,
                        @"fromUserAction": EVENT_FROM_USER_ACTION,
                        @"status": EVENT_STATUS
                     },
             @"Statuses": @{
                        @"notified": NITRecipeNotified,
                        @"engaged": NITRecipeEngaged
                     },
             @"Permissions": @{
                        @"LocationGranted": PERMISSION_LOCATION_GRANTED,
                        @"LocationDenied": PERMISSION_LOCATION_DENIED
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
    
    if (hasListeners) {
        [self sendEventWithName:RN_NATIVE_EVENTS_TOPIC
                           body:event];
    }
}
-(void) sendEventWithLocationPermissionStatus:(NSString* _Nonnull) permissionStatus
{
    NSDictionary* event = @{
                            EVENT_TYPE: EVENT_TYPE_PERMISSIONS,
                            EVENT_STATUS: permissionStatus
                        };

    if (hasListeners) {
        [self sendEventWithName:RN_NATIVE_EVENTS_TOPIC
                           body:event];
    }
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

// MARK: NearIT Feedback

RCT_EXPORT_METHOD(sendFeedback: (NSString* _Nonnull)feedbackB64
                        rating: (NSInteger* _Nonnull)rating
                       comment: (NSString* _Nullable)comment
                    resolution: (RCTPromiseResolveBlock) resolve
                     rejection: (RCTPromiseRejectBlock) reject)
{
    if (IS_EMPTY(feedbackB64)) {
        reject(E_SEND_FEEDBACK_ERROR, @"Missing feedbackId parameter", nil);
    } else {
        NSData* feedbackData = [[NSData alloc] initWithBase64EncodedString:feedbackB64
                                                                   options:NSDataBase64DecodingIgnoreUnknownCharacters];
        
        NITFeedback *feedback = [NSKeyedUnarchiver unarchiveObjectWithData:feedbackData];
        
        NSString* feedbackComment = comment ? comment : @"";
        
        NITFeedbackEvent *feedbackEvent = [[NITFeedbackEvent alloc] initWithFeedback:feedback
                                                                              rating:*rating
                                                                             comment:feedbackComment];
        
        [[NITManager defaultManager] sendEventWithEvent:feedbackEvent
                                      completionHandler:^(NSError * _Nullable error) {
                                          if (error) {
                                              reject(E_SEND_FEEDBACK_ERROR, @"Failed to send feedback to NearIT", error);
                                          } else {
                                              resolve([NSNull null]);
                                          }
                                      }];
    }
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
    
    NITLogD(TAG, @"requestNotificationPermission");
    
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

RCT_EXPORT_METHOD(requestLocationPermission:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    NITLogD(TAG, @"requestLocationPermission");
    
    if (CLLocationManager.authorizationStatus == kCLAuthorizationStatusNotDetermined) {
        [locationManager requestAlwaysAuthorization];
        resolve([NSNull null]);
    } else {
        resolve(@(CLLocationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways));
    }
}

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status
{
    NITLogV(TAG, @"didChangeAuthorizationStatus status=%d", status);
    
    if (status == kCLAuthorizationStatusAuthorizedAlways) {
        [self sendEventWithLocationPermissionStatus: PERMISSION_LOCATION_GRANTED];
        
        NITLogI(TAG, @"NITManager start");
        [[NITManager defaultManager] start];
    } else {
        [self sendEventWithLocationPermissionStatus: PERMISSION_LOCATION_DENIED];
        
        NITLogI(TAG, @"NITManager stop");
        [[NITManager defaultManager] stop];
    }
}

// MARK: NearIT Coupons handling

RCT_EXPORT_METHOD(getCoupons:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    NSMutableArray *coupons = [NSMutableArray init];
    
    [[NITManager defaultManager] couponsWithCompletionHandler:^(NSArray<NITCoupon *> *coupones, NSError *error) {
        if (!error) {
            for(NITCoupon *c in coupones) {
                [coupons addObject:[self bundleNITCoupon:c]];
            }

            resolve(coupons);
        } else {
            reject(E_COUPONS_RETRIEVAL_ERROR, @"Could NOT fetch user Coupons", error);
        }
        
    }];
}

// MARK: NearIT Recipes handling

- (BOOL)handleNearContent: (id _Nonnull) content trackingInfo: (NITTrackingInfo* _Nonnull) trackingInfo fromUserAction: (BOOL) fromUserAction
{
    if ([content isKindOfClass:[NITSimpleNotification class]]) {
        // Simple notification
        NITSimpleNotification *simple = (NITSimpleNotification*)content;
        
        NSString* message = [simple notificationMessage];
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
    } else if ([content isKindOfClass:[NITContent class]]) {
        // Notification with Content
        NITContent *nearContent = (NITContent*)content;
        NITLogI(TAG, @"Content %@ trackingInfo %@", nearContent, trackingInfo);
        
        NSString* message = [nearContent notificationMessage];
        if (!message) {
            message = @"";
        }
        
        NSString* text = [nearContent content];
        if (!text) {
            text = @"";
        }
        
        NSString* videoUrl = [nearContent videoLink];
        if (!videoUrl){
            videoUrl = @"";
        }
        
        NSMutableArray* images = [NSMutableArray init];
        if ([nearContent images]) {
            for(NITImage* i in [nearContent images]) {
                [images addObject:[self bundleNITImage:i]];
            }
        }
        
        NSString* uploadUrl = @"";
        NITUpload* upload = [nearContent upload];
        if (upload) {
            uploadUrl = upload.url ? upload.url.absoluteString : @"";
        }
        
        NSString* audioUrl = @"";
        NITAudio* audioResource = [nearContent audio];
        if (audioResource) {
            audioUrl = audioResource.url ? audioResource.url.absoluteString : @"";
        }
        
        NSDictionary* eventContent = @{
                                       EVENT_CONTENT_MESSAGE:message,
                                          EVENT_CONTENT_TEXT:text,
                                         EVENT_CONTENT_VIDEO:videoUrl,
                                        EVENT_CONTENT_IMAGES:images,
                                        EVENT_CONTENT_UPLOAD:uploadUrl,
                                         EVENT_CONTENT_AUDIO:audioUrl
                                    };
        
        [self sendEventWithContent:eventContent
                      NITEventType:EVENT_TYPE_CONTENT
                      trackingInfo:trackingInfo
                    fromUserAction:fromUserAction];
        
        return YES;
    
    } else if ([content isKindOfClass:[NITFeedback class]]) {
        // Feedback
        NITFeedback* feedback = (NITFeedback*)content;
        NITLogI(TAG, @"Feedback %@ trackingInfo %@", feedback, trackingInfo);
        
        NSString* message = [feedback notificationMessage];
        if (!message) {
            message = @"";
        }
        
        NSData* feedbackData = [NSKeyedArchiver archivedDataWithRootObject:feedback];
        NSString* feedbackB64 = [feedbackData base64EncodedStringWithOptions:0];
        
        NSDictionary* eventContent = @{
                                       EVENT_CONTENT_MESSAGE: message,
                                      EVENT_CONTENT_FEEDBACK: feedbackB64,
                                      EVENT_CONTENT_QUESTION: [feedback question]
                                    };
        
        [self sendEventWithContent:eventContent
                      NITEventType:EVENT_TYPE_FEEDBACK
                      trackingInfo:trackingInfo
                    fromUserAction:fromUserAction];
        
        return YES;
        
    } else if ([content isKindOfClass:[NITCoupon class]]) {
        // Coupon notification
        NITCoupon *coupon = (NITCoupon*)content;
        NITLogI(TAG, @"Coupon %@ trackingInfo %@", coupon, trackingInfo);
        
        NSString* message = [coupon notificationMessage];
        if (!message) {
            message = @"";
        }
        
        NSDictionary* eventContent = @{
                                       EVENT_CONTENT_MESSAGE: message,
                                       EVENT_CONTENT_COUPON: [self bundleNITCoupon:coupon]
                                    };
        
        [self sendEventWithContent:eventContent
                      NITEventType:EVENT_TYPE_COUPON
                      trackingInfo:trackingInfo
                    fromUserAction:fromUserAction];
        
        return YES;
        
    } else if ([content isKindOfClass:[NITCustomJSON class]]) {
        // Custom JSON notification
        NITCustomJSON *custom = (NITCustomJSON*)content;
        NITLogI(TAG, @"JSON message %@ trackingInfo %@", [custom content], trackingInfo);
        
        NSString* message = [custom notificationMessage];
        if (!message) {
            message = @"";
        }
        
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

// MARK: Internal contents handling

- (NSDictionary*)bundleNITCoupon:(NITCoupon* _Nonnull) coupon
{
    NSMutableDictionary* couponDictionary = [[NSMutableDictionary alloc] init];
    [couponDictionary setValue:coupon.name forKey:@"name"];
    [couponDictionary setValue:coupon.couponDescription forKey:@"description"];
    [couponDictionary setValue:coupon.value forKey:@"value"];
    [couponDictionary setValue:coupon.expiresAt forKey:@"expiresAt"];
    [couponDictionary setValue:coupon.redeemableFrom forKey:@"redeemableFrom"];
    
    if (coupon.claims.count > 0) {
        [couponDictionary setValue:coupon.claims[0].serialNumber forKey:@"serial"];
        [couponDictionary setValue:coupon.claims[0].claimedAt forKey:@"claimedAt"];
        [couponDictionary setValue:(coupon.claims[0].redeemedAt ? coupon.claims[0].redeemedAt : [NSNull null]) forKey:@"redeemedAt"];
    }
    
    if (coupon.icon) {
        [couponDictionary setValue:[self bundleNITImage:coupon.icon] forKey:@"image"];
    }
    
    return couponDictionary;
}

- (NSDictionary*)bundleNITImage:(NITImage* _Nonnull) image
{
    return @{
             @"fullSize": (image.url ? image.url : [NSNull null]),
             @"squareSize": (image.smallSizeURL ? image.smallSizeURL : [NSNull null])
            };
}


// MARK: Internal notification handling

- (void)handleNotificationReceived:(NSNotification*) notification
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

