/*
 * Copyright (c) 2017 Mattia Panzeri <mattia.panzeri93@gmail.com>
 * Latest changes by Federico Boschini <federico@nearit.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import "RNNearIt.h"

#define TAG @"RNNearIt"

#define IS_EMPTY(v) (v == nil || [v length] <= 0)

static RNNearIt* defaultManager;

@implementation RNNearIt

RCT_EXPORT_MODULE()

- (instancetype)init
{
    self = [super init];
    defaultManager = self;
    return self;
}

+ (RNNearIt* _Nullable)defaultManager
{
    return defaultManager;
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"NativeEventsTopic": RN_NATIVE_EVENTS_TOPIC,
             @"NativeNotificationHistoryTopic": RN_NATIVE_NOTIFICATION_HISTORY_TOPIC,
             @"Events": @{
                     @"SimpleNotification": EVENT_TYPE_SIMPLE,
                     @"CustomJson": EVENT_TYPE_CUSTOM_JSON,
                     @"Coupon": EVENT_TYPE_COUPON,
                     @"Content": EVENT_TYPE_CONTENT,
                     @"Feedback": EVENT_TYPE_FEEDBACK
                     },
             @"EventContent": @{
                     @"type": EVENT_TYPE,
                     @"trackingInfo": EVENT_TRACKING_INFO,
                     @"message": EVENT_CONTENT_MESSAGE,
                     @"content": EVENT_CONTENT,
                     @"fromUserAction": EVENT_FROM_USER_ACTION,
                     @"status": EVENT_STATUS,
                     @"title": EVENT_CONTENT_TITLE,
                     @"image": EVENT_IMAGE,
                     @"fullSize": EVENT_IMAGE_FULL_SIZE,
                     @"squareSize": EVENT_IMAGE_SQUARE_SIZE,
                     @"text": EVENT_CONTENT_TEXT,
                     @"cta": EVENT_CONTENT_CTA,
                     @"label": EVENT_CONTENT_CTA_LABEL,
                     @"url": EVENT_CONTENT_CTA_URL,
                     @"description": EVENT_COUPON_DESCRIPTION,
                     @"value": EVENT_COUPON_VALUE,
                     @"expiresAt": EVENT_COUPON_EXPIRES_AT,
                     @"redeemableFrom": EVENT_COUPON_REDEEMABLE_FROM,
                     @"serial": EVENT_COUPON_SERIAL,
                     @"claimedAt": EVENT_COUPON_CLAIMED_AT,
                     @"redeemedAt": EVENT_COUPON_REDEEMED_AT,
                     @"question": EVENT_FEEDBACK_QUESTION,
                     @"feedbackId": EVENT_FEEDBACK_ID,
                     @"notificationHistory": NOTIFICATION_HISTORY,
                     @"read": NOTIFICATION_HISTORY_READ,
                     @"timestamp": NOTIFICATION_HISTORY_TIMESTAMP,
                     @"isNew": NOTIFICATION_HISTORY_IS_NEW,
                     @"notificationContent": NOTIFICATION_HISTORY_CONTENT
                     },
             @"Statuses": @{
                     @"received": NITRecipeReceived,
                     @"opened": NITRecipeOpened
                     },
             @"Permissions": @{
                     @"location": PERMISSIONS_LOCATION_PERMISSION,
                     @"notifications": PERMISSIONS_NOTIFICATIONS_PERMISSION,
                     @"bluetooth": PERMISSIONS_BLUETOOTH,
                     @"locationServices": PERMISSIONS_LOCATION_SERVICES,
                     @"always": PERMISSIONS_LOCATION_ALWAYS,
                     @"whenInUse": PERMISSIONS_LOCATION_WHEN_IN_USE,
                     @"denied": PERMISSIONS_LOCATION_DENIED
                     }
             };
}

- (NSArray<NSString*>*)supportedEvents
{
    return @[RN_NATIVE_EVENTS_TOPIC, RN_NATIVE_NOTIFICATION_HISTORY_TOPIC];
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}



/*
 *  Delegates
 */

#pragma mark - CBCentralManagerDelegate

- (void)centralManagerDidUpdateState:(CBCentralManager*)manager
{
    switch (manager.state) {
            case CBCentralManagerStatePoweredOn:
            if (self.bluetoothResolve) {
                self.bluetoothResolve(@YES);
            }
            break;
        default:
            if (self.bluetoothResolve) {
                self.bluetoothResolve(@NO);
            }
            break;
    }
}

#pragma mark - NITNotificationUpdateDelegate

- (void)historyUpdatedWithItems:(NSArray<NITHistoryItem*>* _Nullable)items
{
    NSArray* bundledHistory = [RNNearItUtils bundleNITHistory:items];
    if (_listeners > 0) {
        [self sendEventWithName:RN_NATIVE_NOTIFICATION_HISTORY_TOPIC body:@{NOTIFICATION_HISTORY:bundledHistory}];
    }
}

#pragma mark - NITManagerDelegate

- (void)manager:(NITManager*)manager eventWithContent:(id _Nonnull)content trackingInfo:(NITTrackingInfo* _Nonnull)trackingInfo {
    [self handleNearContent:content trackingInfo:trackingInfo fromUserAction:NO];
}

- (void)manager:(NITManager* _Nonnull)manager eventFailureWithError:(NSError* _Nonnull)error {
    // handle errors (only for information purpose)
}

// iOS9

- (void)manager:(NITManager* _Nonnull)manager alertWantsToShowContent:(id _Nonnull)content trackingInfo:(NITTrackingInfo* _Nonnull)trackingInfo {
    [self handleNearContent:content trackingInfo:trackingInfo fromUserAction:YES];
}



/*
 * Native API
 */

// DidFinishLaunchingWithOptions

- (void)application:(UIApplication* _Nonnull)application didFinishLaunchingWithOptions:(NSDictionary* _Nullable)launchOptions
{
    [self loadConfig];
}

// Background fetch

- (void)application:(UIApplication* _Nonnull)application performFetchWithCompletionHandler:(void (^_Nonnull)(UIBackgroundFetchResult))completionHandler
{
    [[NITManager defaultManager] application:application performFetchWithCompletionHandler:^(UIBackgroundFetchResult result) {
        completionHandler(result);
    }];
}

// Test devices

- (BOOL)application:(UIApplication* _Nonnull)app openUrl:(NSURL* _Nonnull)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id>* _Nullable)options
{
    return [[NITManager defaultManager] application:app openURL:url options:options];
}

// Notifications

- (void)userNotificationCenter:(UNUserNotificationCenter* _Nonnull)center willPresentNotification:(UNNotification* _Nonnull)notification withCompletionHandler:(void (^_Nonnull)(UNNotificationPresentationOptions))completionHandler
{
    [[NITManager defaultManager] userNotificationCenter:center willPresent:notification withCompletionHandler:completionHandler];
}

- (BOOL)userNotificationCenter:(UNUserNotificationCenter* _Nonnull)center didReceiveNotificationResponse:(UNNotificationResponse* _Nonnull)response withCompletionHandler:(void (^_Nonnull)(void))completionHandler
{
    BOOL isNearNotification = [[NITManager defaultManager] getContentFrom:response completion:^(NITReactionBundle* _Nullable content, NITTrackingInfo* _Nullable trackingInfo, NSError* _Nullable error) {
        if (content) {
            [self handleNearContent:content trackingInfo:trackingInfo fromUserAction:YES];
            completionHandler();
        }
    }];
    return isNearNotification;
}

- (void)application:(UIApplication* _Nonnull)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData* _Nullable)deviceToken
{
    [[NITManager defaultManager] setDeviceTokenWithData:deviceToken];
}

// Notificactions iOS9

- (BOOL)application:(UIApplication* _Nonnull)application didReceiveRemoteNotification:(NSDictionary* _Nonnull)userInfo
{
    return [self didReceiveNotification:userInfo fromUserAction:YES];
}

- (BOOL)application:(UIApplication* _Nonnull)application didReceiveLocalNotification:(UILocalNotification* _Nonnull)notification
{
    return [self didReceiveNotification:notification.userInfo fromUserAction:YES];
}



/*
 *  React-Native exported methods
 */

RCT_EXPORT_METHOD(onDeviceReady)
{
    [[RNNearItBackgroundQueue defaultQueue] dispatchNotificationsQueue:^(NSDictionary * _Nonnull notification) {
        [self sendEventWithName:RN_NATIVE_EVENTS_TOPIC body:notification];
    }];
}

RCT_EXPORT_METHOD(disableDefaultRangingNotifications)
{
    [NITManager defaultManager].showForegroundNotification = false;
}

RCT_EXPORT_METHOD(addProximityListener)
{
    [NITManager defaultManager].delegate = self;
}

RCT_EXPORT_METHOD(removeProximityListener)
{
    [NITManager defaultManager].delegate = nil;
}

// MARK: ReactNative listeners management

RCT_EXPORT_METHOD(listenerRegistered:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    _listeners++;
    [[RNNearItBackgroundQueue defaultQueue] dispatchNotificationsQueue:^(NSDictionary * _Nonnull notification) {
        [self sendEventWithName:RN_NATIVE_EVENTS_TOPIC body:notification];
    }];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(listenerUnregistered:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    _listeners--;
    resolve([NSNull null]);
}

// MARK: Radar related methods

RCT_EXPORT_METHOD(startRadar)
{
    [[NITManager defaultManager] start];
}

RCT_EXPORT_METHOD(stopRadar)
{
    [[NITManager defaultManager] stop];
}

// MARK: Trackings related methods

RCT_EXPORT_METHOD(sendTracking:(NSString* _Nonnull)bundledTrackingInfo status:(NSString* _Nonnull)status)
{
    NITTrackingInfo* trackingInfo = [RNNearItUtils unbundleTrackingInfo:bundledTrackingInfo];
    
    if (trackingInfo) {
        [[NITManager defaultManager] sendTrackingWithTrackingInfo:trackingInfo event:status];
    } else {
        NITLogD(TAG, @"RNNearIt:: failed to send tracking for event (%@) with trackingInfo (%@)", status, trackingInfo);
    }
}

// MARK: Feedback related methods

RCT_EXPORT_METHOD(sendFeedback:(NSDictionary* _Nonnull)bundledFeedback
                  rating:(NSInteger)rating
                  comment:(NSString* _Nullable)comment
                  resolution:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    NITFeedback* feedback = [RNNearItUtils unbundleNITFeedback:bundledFeedback];
    if (feedback == nil) {
        reject(E_SEND_FEEDBACK_ERROR, @"Feedback unbundling failed", nil);
        NITLogE(TAG, @"NITFeedback from unbundling process is nil");
    } else {
        NITFeedbackEvent* feedbackEvent = [[NITFeedbackEvent alloc] initWithFeedback:feedback rating:rating comment:comment];
        [[NITManager defaultManager] sendEventWithEvent:feedbackEvent completionHandler:^(NSError* _Nullable error) {
            if (error != nil) {
                reject(E_SEND_FEEDBACK_ERROR, @"Failed to send feedback to NearIT", error);
            } else {
                resolve([NSNull null]);
            }
        }];
    }
}

// MARK: ProfileId related methods

RCT_EXPORT_METHOD(getProfileId:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    [[NITManager defaultManager] profileIdWithCompletionHandler:^(NSString* _Nullable profileId, NSError* _Nullable error) {
        if (error != nil) {
            reject(E_PROFILE_ID_GET_ERROR, @"Could NOT get profileId", error);
        } else {
            resolve(profileId);
        }
    }];
}

RCT_EXPORT_METHOD(setProfileId:(NSString* _Nonnull)profileId)
{
    [[NITManager defaultManager] setProfileId:profileId];
}

RCT_EXPORT_METHOD(resetProfileId:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    [[NITManager defaultManager] resetProfileWithCompletionHandler:^(NSString* _Nullable profileId, NSError* _Nullable error) {
        if (error != nil) {
            reject(E_PROFILE_ID_RESET_ERROR, @"Could NOT reset profileId", error);
        } else {
            resolve(profileId);
        }
    }];
}

// MARK: User data related methods

RCT_EXPORT_METHOD(setUserData:(NSString* _Nonnull)key
                  value:(NSString* _Nullable)value)
{
    [[NITManager defaultManager] setUserDataWithKey:key value:value];
}

RCT_EXPORT_METHOD(setMultiChoiceUserData:(NSString* _Nonnull)key
                  userData:(NSDictionary* _Nullable)userData)
{
    NSMutableDictionary* data = [[NSMutableDictionary alloc] init];
    for(id key in userData) {
        NSObject* object = [userData objectForKey:key];
        data[key] = object;
    }
    NITLogI(TAG, @"RNNearIt:: setting multichoice data key=%@ values=%@", key, data);
    [[NITManager defaultManager] setUserDataWithKey:key multiValue:data];
}

RCT_EXPORT_METHOD(getUserData:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    [[NITManager defaultManager] getUserDataWithCompletionHandler:^(NSDictionary<NSString*,id>* _Nullable userData, NSError* _Nullable error) {
        if (error != nil) {
            reject(E_PROFILE_GET_USER_DATA_ERROR, @"Could NOT get user data", nil);
        } else {
            resolve(userData);
        }
    }];
}

// MARK: Opt-out related methods

RCT_EXPORT_METHOD(optOut:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [[NITManager defaultManager] optOutWithCompletionHandler:^(BOOL success) {
        if (success) {
            resolve([NSNull null]);
        } else {
            reject(E_OPT_OUT_ERROR, @"Could NOT optOut user", nil);
        }
    }];
}

// MARK: Permissions related methods

RCT_EXPORT_METHOD(isLocationGranted:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    switch (CLLocationManager.authorizationStatus) {
            case kCLAuthorizationStatusNotDetermined:
            resolve([NSNull null]);
            break;
            
        default: {
            BOOL locationPermission = CLLocationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways || CLLocationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse;
            resolve(@(locationPermission));
            break;
        }
    }
}

RCT_EXPORT_METHOD(isNotificationGranted:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_9_x_Max) {
        UIUserNotificationSettings* notificationSettings = [RCTSharedApplication() currentUserNotificationSettings];
        BOOL notificationAuthorized = notificationSettings != UIUserNotificationTypeNone;
        resolve(@(notificationAuthorized));
    } else {
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
        [[UNUserNotificationCenter currentNotificationCenter]getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
            switch (settings.authorizationStatus) {
                    case UNAuthorizationStatusNotDetermined:
                    resolve([NSNull null]);
                    break;
                    
                default: {
                    BOOL notificationPermission = settings.authorizationStatus == UNAuthorizationStatusAuthorized;
                    resolve(@(notificationPermission));
                    break;
                }
            }
        }];
#endif
    }
}

RCT_EXPORT_METHOD(areLocationServicesOn:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    BOOL locationServicesOn = [CLLocationManager locationServicesEnabled];
    resolve(@(locationServicesOn));
}

RCT_EXPORT_METHOD(isBluetoothEnabled:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    self.bluetoothResolve = resolve;
    
    if (!self.bluetoothManager) {
        NSDictionary* options = @{CBCentralManagerOptionShowPowerAlertKey:@NO};
        self.bluetoothManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil options:options];
    }
    [self centralManagerDidUpdateState:self.bluetoothManager];
}

// MARK: In-app events related methods

RCT_EXPORT_METHOD(triggerInAppEvent:(NSString* _Nonnull)eventKey)
{
    [[NITManager defaultManager] triggerInAppEventWithKey:eventKey];
}

// MARK: Coupon related methods

RCT_EXPORT_METHOD(getCoupons:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    NSMutableArray* bundledCoupons = [[NSMutableArray alloc] init];
    
    [[NITManager defaultManager] couponsWithCompletionHandler:^(NSArray<NITCoupon*>* _Nullable coupons, NSError* _Nullable error) {
        if (error != nil) {
            reject(E_COUPONS_RETRIEVAL_ERROR, @"Could NOT fetch user coupons", error);
        } else {
            for (NITCoupon* c in coupons) {
                [bundledCoupons addObject:[RNNearItUtils bundleNITCoupon:c]];
            }
            resolve(bundledCoupons);
        }
    }];
}

// MARK: Notification History related methods

RCT_EXPORT_METHOD(getNotificationHistory:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    
    [[NITManager defaultManager] historyWithCompletion:^(NSArray<NITHistoryItem*>* _Nullable history, NSError* _Nullable error) {
        if (error != nil) {
            reject(E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR, @"Could NOT fetch user notification history", error);
        } else {
            resolve([RNNearItUtils bundleNITHistory:history]);
        }
    }];
}

RCT_EXPORT_METHOD(markNotificationHistoryAsOld)
{
    [[NITManager defaultManager] markNotificationHistoryAsOld];
}

RCT_EXPORT_METHOD(notificationHistoryListenerRegistered:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    [NITManager defaultManager].notificationDelegate = self;
    resolve(@YES);
}

RCT_EXPORT_METHOD(notificationHistoryListenerUnregistered:(RCTPromiseResolveBlock)resolve
                  rejection:(RCTPromiseRejectBlock)reject)
{
    [NITManager defaultManager].notificationDelegate = nil;
    resolve(@YES);
}



/*
 *  Private methods
 */

- (BOOL)didReceiveNotification:(NSDictionary* _Nonnull)userInfo fromUserAction:(BOOL)fromUserAction
{
    BOOL isNearNotification = [[NITManager defaultManager] processRecipeWithUserInfo:userInfo completion:^(NITReactionBundle* _Nullable content, NITTrackingInfo* _Nullable trackingInfo, NSError* _Nullable error) {
        if (content) {
            [self handleNearContent:content trackingInfo:trackingInfo fromUserAction:YES];
        }
    }];
    return isNearNotification;
}

- (void)handleNearContent:(NITReactionBundle* _Nonnull)content trackingInfo:(NITTrackingInfo* _Nullable)trackingInfo fromUserAction:(BOOL)fromUserAction
{
    if ([content isKindOfClass:[NITSimpleNotification class]]) {
        NITSimpleNotification* simple = (NITSimpleNotification*)content;
        NSDictionary* eventContent = @{EVENT_CONTENT_MESSAGE: [simple notificationMessage]};
        [self sendEventWithContent:eventContent NITEventType:EVENT_TYPE_SIMPLE trackingInfo: trackingInfo fromUserAction:fromUserAction];
    } else if ([content isKindOfClass:[NITContent class]]) {
        NITContent* nitContent = (NITContent*)content;
        NSMutableDictionary* eventContent = [NSMutableDictionary dictionaryWithDictionary:@{EVENT_CONTENT_MESSAGE: [nitContent notificationMessage]}];
        NSDictionary* bundledContent = [RNNearItUtils bundleNITContent:nitContent];
        [eventContent addEntriesFromDictionary:bundledContent];
        [self sendEventWithContent:eventContent NITEventType:EVENT_TYPE_CONTENT trackingInfo: trackingInfo fromUserAction:fromUserAction];
    } else if ([content isKindOfClass:[NITFeedback class]]) {
        NITFeedback* feedback = (NITFeedback*)content;
        NSMutableDictionary* eventContent = [NSMutableDictionary dictionaryWithDictionary:@{EVENT_CONTENT_MESSAGE: [feedback notificationMessage]}];
        NSDictionary* bundledFeedback = [RNNearItUtils bundleNITFeedback:feedback];
        [eventContent addEntriesFromDictionary:bundledFeedback];
        [self sendEventWithContent:eventContent NITEventType:EVENT_TYPE_FEEDBACK trackingInfo: trackingInfo fromUserAction:fromUserAction];
    } else if ([content isKindOfClass:[NITCoupon class]]) {
        NITCoupon* coupon = (NITCoupon*)content;
        NSMutableDictionary* eventContent = [NSMutableDictionary dictionaryWithDictionary:@{EVENT_CONTENT_MESSAGE: [coupon notificationMessage]}];
        NSDictionary* bundledCoupon = [RNNearItUtils bundleNITCoupon:coupon];
        [eventContent addEntriesFromDictionary:bundledCoupon];
        [self sendEventWithContent:eventContent NITEventType:EVENT_TYPE_COUPON trackingInfo: trackingInfo fromUserAction:fromUserAction];
    } else if ([content isKindOfClass:[NITCustomJSON class]]) {
        NITCustomJSON* customJson = (NITCustomJSON*)content;
        NSMutableDictionary* eventContent = [NSMutableDictionary dictionaryWithDictionary:@{EVENT_CONTENT_MESSAGE: [customJson notificationMessage]}];
        NSDictionary* bundledCustomJson = [RNNearItUtils bundleNITCustomJSON:customJson];
        [eventContent addEntriesFromDictionary:bundledCustomJson];
        [self sendEventWithContent:eventContent NITEventType:EVENT_TYPE_CUSTOM_JSON trackingInfo: trackingInfo fromUserAction:fromUserAction];
    } else {
        NITLogW(TAG, [NSString stringWithFormat:@"unknown content type"]);
    }
}

- (void)loadConfig
{
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
    
    // Set NearIT framework name
    [NITManager setFrameworkName:@"react-native"];
}

- (void)sendEventWithContent:(NSDictionary* _Nonnull)content NITEventType:(NSString* _Nonnull)eventType trackingInfo:(NITTrackingInfo* _Nullable)trackingInfo fromUserAction:(BOOL)fromUserAction
{
    NSString* bundledTrackingInfo;
    if (trackingInfo) {
        bundledTrackingInfo = [RNNearItUtils bundleTrackingInfo:trackingInfo];
    }
    
    NSDictionary* event = @{
                            EVENT_TYPE:eventType,
                            EVENT_CONTENT:content,
                            EVENT_TRACKING_INFO: (bundledTrackingInfo ? bundledTrackingInfo : [NSNull null]),
                            EVENT_FROM_USER_ACTION: [NSNumber numberWithBool:fromUserAction]
                            };
    if (_listeners > 0) {
        [self sendEventWithName:RN_NATIVE_EVENTS_TOPIC body:event];
    } else {
        [[RNNearItBackgroundQueue defaultQueue] addNotification:event];
    }
}

@end
