//
//  RNNearItUtils.h
//  RNNearIt
//
//  Created by Federico Boschini <federico@nearit.com> on 10/06/2019.
//  Copyright © 2019 NearIT. All rights reserved.
//

#if __has_include(<NearITSDK/NearITSDK.h>)
#import <NearITSDK/NearITSDK.h>
#elif __has_include("NearIT.h")
#import "NearIT.h"
#endif

#import "RNNearItConsts.h"

@interface RNNearItUtils : NSObject

+ (NSString* _Nullable)bundleTrackingInfo:(NITTrackingInfo* _Nullable)trackingInfo;
+ (NITTrackingInfo* _Nullable)unbundleTrackingInfo:(NSString * _Nullable)bundledTrackingInfo;

+ (NSDictionary* _Nullable)bundleNITCoupon:(NITCoupon* _Nonnull)coupon;
+ (NITCoupon* _Nullable)unbundleNITCoupon:(NSDictionary* _Nonnull)bundledCoupon;

+ (NSDictionary* _Nullable)bundleNITContent:(NITContent* _Nonnull)content;
+ (NITContent* _Nullable)unbundleNITContent:(NSDictionary* _Nonnull)bundledContent;

+ (NSArray* _Nullable)bundleNITHistory:(NSArray<NITHistoryItem*>* _Nonnull)history;

+ (NSDictionary* _Nullable)bundleNITFeedback:(NITFeedback* _Nonnull)feedback;
+ (NITFeedback* _Nullable)unbundleNITFeedback:(NSDictionary* _Nonnull)bundledFeedback;

+ (NSDictionary* _Nullable)bundleNITCustomJSON:(NITCustomJSON* _Nonnull)custom;

@end

