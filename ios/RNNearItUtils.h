//
//  RNNearItUtils.h
//  RNNearIt
//
//  Created by Federico Boschini <federico@nearit.com> on 10/06/2019.
//  Copyright © 2019 NearIT. All rights reserved.
//

#import <NearITSDK/NearITSDK.h>
#import "RNNearItConsts.h"

@interface RNNearItUtils : NSObject

+ (NITCoupon* _Nullable)unbundleNITCoupon:(NSDictionary* _Nonnull)bundledCoupon;
+ (NSDictionary* _Nullable)bundleNITCoupon:(NITCoupon* _Nonnull)coupon;
+ (NSDictionary* _Nullable)bundleNITHistoryItem:(NITHistoryItem* _Nonnull)item;
+ (NITContent* _Nullable)unbundleNITContent:(NSDictionary * _Nonnull)bundledContent;
+ (NSDictionary* _Nullable)bundleNITContent:(NITContent * _Nonnull)content;
+ (NITFeedback* _Nullable)unbundleNITFeedback:(NSDictionary * _Nonnull)bundledFeedback;
+ (NSDictionary* _Nullable)bundleNITFeedback:(NITFeedback * _Nonnull)feedback;
+ (NSDictionary* _Nullable)bundleNITCustomJSON:(NITCustomJSON* _Nonnull)custom;
+ (NITImage* _Nullable)unbundleNITImage:(NSDictionary* _Nonnull)bundledImage;
+ (NSDictionary* _Nullable)bundleNITImage:(NITImage* _Nonnull)image;
+ (NSDictionary* _Nullable)bundleNITContentLink:(NITContentLink* _Nonnull)cta;
+ (NITTrackingInfo* _Nullable)unbundleTrackingInfo:(NSString * _Nullable)bundledTrackingInfo;
+ (NSString* _Nullable)bundleTrackingInfo:(NITTrackingInfo* _Nullable)trackingInfo;

@end

