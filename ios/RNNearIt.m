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
NSString* const RN_NATIVE_PERMISSIONS_TOPIC = @"RNNearItPermissions";

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
NSString* const EVENT_CONTENT_TITLE = @"title";
NSString* const EVENT_CONTENT_IMAGE = @"image";
NSString* const EVENT_CONTENT_CTA = @"cta";
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
        
        // Load API Key from NearIt.plist
        NSString *path = [[NSBundle mainBundle] pathForResource:@"NearIt" ofType:@"plist"];
        NSDictionary *dict = [[NSDictionary alloc] initWithContentsOfFile:path];
        NSString* NITApiKey = [dict objectForKey:@"API Key"];
        // Pass API Key to NITManager
        if (NITApiKey) {
            [NITManager setupWithApiKey:NITApiKey];
        } else {
            NSLog(@"Could not find NearIt.plist or 'API Key' field inside of it. NearIT won't work!");
        }
        
        // Delegates
        [NITManager defaultManager].delegate = self;
        [UNUserNotificationCenter currentNotificationCenter].delegate = self;
    }
    
    return self;
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"NativeEventsTopic": RN_NATIVE_EVENTS_TOPIC,
             @"NativePermissionsTopic": RN_NATIVE_PERMISSIONS_TOPIC,
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
                        @"title": EVENT_CONTENT_TITLE,
                        @"text": EVENT_CONTENT_TEXT,
                        @"image": EVENT_CONTENT_IMAGE,
                        @"cta": EVENT_CONTENT_CTA,
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
    return @[
             RN_NATIVE_EVENTS_TOPIC,
             RN_NATIVE_PERMISSIONS_TOPIC
         ];
}

RCT_EXPORT_METHOD(listenerRegistered: (RCTPromiseResolveBlock) resolve
                            rejecter: (RCTPromiseRejectBlock) reject) {
    _listeners++;
    resolve([NSNull null]);
    // Dispatch Notification received while app was backgrounded/dead
    [[RNNearItBackgroundQueue defaultQueue] dispatchNotificationsQueue:^(NSDictionary* notification) {
        [self sendEventWithName:RN_NATIVE_EVENTS_TOPIC
                           body:notification];
    }];
}

RCT_EXPORT_METHOD(listenerUnregistered: (RCTPromiseResolveBlock) resolve
                              rejecter: (RCTPromiseRejectBlock) reject) {
    _listeners--;
    resolve([NSNull null]);
}


- (void) sendEventWithContent:(NSDictionary* _Nonnull) content NITEventType:(NSString* _Nonnull) eventType trackingInfo:(NITTrackingInfo* _Nullable) trackingInfo fromUserAction:(BOOL) fromUserAction
{
    NSString* trackingInfoB64;
    if (trackingInfo) {
        NSData* trackingInfoData = [NSKeyedArchiver archivedDataWithRootObject:trackingInfo];
        trackingInfoB64 = [trackingInfoData base64EncodedStringWithOptions:0];
    }
    
    NSDictionary* event = @{
                            EVENT_TYPE: eventType,
                            EVENT_CONTENT: content,
                            EVENT_TRACKING_INFO: (trackingInfoB64 ? trackingInfoB64 : [NSNull null]),
                            EVENT_FROM_USER_ACTION: [NSNumber numberWithBool:fromUserAction]
                        };
    
    if (_listeners > 0) {
        [self sendEventWithName:RN_NATIVE_EVENTS_TOPIC
                           body:event];
    } else {
        [[RNNearItBackgroundQueue defaultQueue] addNotification:event];
    }
}

-(void) sendEventWithLocationPermissionStatus:(NSString* _Nonnull) permissionStatus {
    NSDictionary* event = @{
                            EVENT_TYPE: EVENT_TYPE_PERMISSIONS,
                            EVENT_STATUS: permissionStatus
                        };

    [self sendEventWithName:RN_NATIVE_PERMISSIONS_TOPIC
                       body:event];
}

// MARK: UNUserNotificationCenterDelegate

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler {
    completionHandler(UNNotificationPresentationOptionAlert);
}

// MARK: NITManagerDelegate

- (void)manager:(NITManager *)manager eventWithContent:(id)content trackingInfo:(NITTrackingInfo *)trackingInfo
{
    [self handleNearContent:content
               trackingInfo:trackingInfo
             fromUserAction:NO];
}

- (void)manager:(NITManager *)manager eventFailureWithError:(NSError *)error
{
    // handle errors (only for information purpose)
}

- (void)manager:(NITManager* _Nonnull)manager alertWantsToShowContent:(id _Nonnull)content {
    [self handleNearContent:content
               trackingInfo:nil
             fromUserAction:YES];
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

#if !TARGET_IPHONE_SIMULATOR
    NITLogV(TAG, @"registerForRemoteNotifications");
    [RCTSharedApplication() registerForRemoteNotifications];
#endif // TARGET_IPHONE_SIMULATOR
}

RCT_EXPORT_METHOD(requestLocationPermission:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    NITLogD(TAG, @"requestLocationPermission");
    
    if (CLLocationManager.authorizationStatus == kCLAuthorizationStatusNotDetermined) {
        // resolve null
        resolve([NSNull null]);
        
        // Request location permission to user
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
        [locationManager requestAlwaysAuthorization];
    } else {
        // resolve if user hase given 'Always' permission
        resolve(@(CLLocationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways));
    }
}

// MARK: CLLocationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status
{
    NITLogV(TAG, @"didChangeAuthorizationStatus status=%d", status);
    
    if (status != kCLAuthorizationStatusNotDetermined) {
        if (status == kCLAuthorizationStatusAuthorizedAlways) {
            [self sendEventWithLocationPermissionStatus: PERMISSION_LOCATION_GRANTED];
            
            NITLogI(TAG, @"NITManager start");
            [[NITManager defaultManager] start];
        } else {
            [self sendEventWithLocationPermissionStatus: PERMISSION_LOCATION_DENIED];
            
            NITLogI(TAG, @"NITManager stop");
            [[NITManager defaultManager] stop];
        }
        
        // Remove CLLocationManagerDelegate
        locationManager.delegate = nil;
    }
}

// MARK: NearIT Coupons handling

RCT_EXPORT_METHOD(getCoupons:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    NSMutableArray *coupons = [[NSMutableArray alloc] init];
    
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

- (BOOL)handleNearContent: (id _Nonnull) content trackingInfo: (NITTrackingInfo* _Nullable) trackingInfo fromUserAction: (BOOL) fromUserAction
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
        
        NSString* title = [nearContent title];
        if (!title) {
            title = @"";
        }
        
        NSString* text = [nearContent content];
        if (!text) {
            text = @"";
        }
        
        id image;
        if (nearContent.image) {
            image = [self bundleNITImage:nearContent.image];
        } else {
            image = [NSNull null];
        }
        
        id cta;
        if (nearContent.link) {
            cta = [self bundleNITContentLink:nearContent.link];
        } else {
            cta = [NSNull null];
        }
        
        NSDictionary* eventContent = @{
                                       EVENT_CONTENT_MESSAGE:message,
                                         EVENT_CONTENT_TITLE:title,
                                          EVENT_CONTENT_TEXT:text,
                                         EVENT_CONTENT_IMAGE:image,
                                           EVENT_CONTENT_CTA:cta
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
    [couponDictionary setObject:(coupon.title ? coupon.title : [NSNull null])
                         forKey:@"name"];
    [couponDictionary setObject:(coupon.couponDescription ? coupon.couponDescription : [NSNull null])
                         forKey:@"description"];
    [couponDictionary setObject:(coupon.value ? coupon.value : [NSNull null])
                         forKey:@"value"];
    [couponDictionary setObject:(coupon.expiresAt ? coupon.expiresAt : [NSNull null])
                         forKey:@"expiresAt"];
    [couponDictionary setObject:(coupon.redeemableFrom ? coupon.redeemableFrom : [NSNull null])
                         forKey:@"redeemableFrom"];
    
    if (coupon.claims.count > 0) {
        [couponDictionary setObject:coupon.claims[0].serialNumber forKey:@"serial"];
        [couponDictionary setObject:coupon.claims[0].claimedAt forKey:@"claimedAt"];
        [couponDictionary setObject:(coupon.claims[0].redeemedAt ? coupon.claims[0].redeemedAt : [NSNull null]) forKey:@"redeemedAt"];
    }
    
    if (coupon.icon) {
        if (coupon.icon.url || coupon.icon.smallSizeURL) {
            [couponDictionary setObject:[self bundleNITImage:coupon.icon] forKey:@"image"];
        }
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

- (NSDictionary*)bundleNITContentLink:(NITContentLink* _Nonnull) cta {
    return @{
             @"label": cta.label,
             @"url": cta.url
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
            [self handleNearContent:content
                       trackingInfo:trackingInfo
                     fromUserAction:@(RCTSharedApplication().applicationState == UIApplicationStateInactive)];
        }
    }];
}

// MARK: Foreground Notifications handling

- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void(^)(void))completionHandler {

    NSMutableDictionary* data = [[NSMutableDictionary alloc] initWithDictionary: response.notification.request.content.userInfo];
    [data setObject:@YES forKey:@"fromUserAction"];

    [[NSNotificationCenter defaultCenter] postNotificationName:RN_LOCAL_EVENTS_TOPIC
                                                        object:self
                                                      userInfo:@{
                                                                 @"data": data,
                                                                 @"completionHandler": completionHandler
                                                             }];
 
     completionHandler();
}

// MARK: Push Notifications handling

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *) deviceToken {
    [[NITManager defaultManager] setDeviceTokenWithData:deviceToken];
}

+ (void)didReceiveRemoteNotification:(NSDictionary* _Nonnull) userInfo {
    NSMutableDictionary* data = [[NSMutableDictionary alloc] initWithDictionary: userInfo];
    [data setObject:@YES forKey:@"fromUserAction"];

    [[NSNotificationCenter defaultCenter] postNotificationName:RN_LOCAL_EVENTS_TOPIC
                                                        object:self
                                                      userInfo:@{@"data": data}];
}

+ (void)didReceiveLocalNotification:(UILocalNotification* _Nonnull) notification {
    NSMutableDictionary* data = [[NSMutableDictionary alloc] initWithDictionary: notification.userInfo];
    [data setObject:@YES forKey:@"fromUserAction"];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:RN_LOCAL_EVENTS_TOPIC
                                                        object:self
                                                      userInfo:@{
                                                                 @"data": data
                                                             }];
}

+ (void)didReceiveNotificationResponse:(UNNotificationResponse* _Nonnull) response withCompletionHandler:(void (^ _Nonnull)())completionHandler {
    NSMutableDictionary* data = [[NSMutableDictionary alloc] initWithDictionary: response.notification.request.content.userInfo];
    [data setObject:@YES forKey:@"fromUserAction"];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:RN_LOCAL_EVENTS_TOPIC
                                                        object:self
                                                      userInfo:@{
                                                                 @"data": data,
                                                                 @"completionHandler": completionHandler
                                                                 }];
}

@end

