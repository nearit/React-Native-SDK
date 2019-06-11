//
//  RNNearItUtils.h
//  RNNearIt
//
//  Created by Federico Boschini <federico@nearit.com> on 10/06/2019.
//  Copyright © 2019 NearIT. All rights reserved.
//

#import <NearIT/NearIT.h>
#import "RNNearItConsts.h"

@interface RNNearItUtils : NSObject

+ (NITCoupon*)unbundleNITCoupon:(NSDictionary* _Nonnull)bundledCoupon;
+ (NSDictionary*)bundleNITCoupon:(NITCoupon* _Nonnull)coupon;
+ (NSDictionary*)bundleNITHistoryItem:(NITHistoryItem* _Nonnull)item;
+ (NITContent*)unbundleNITContent:(NSDictionary * _Nonnull)bundledContent;
+ (NSDictionary*)bundleNITContent:(NITContent * _Nonnull)content;
+ (NITFeedback*)unbundleNITFeedback:(NSDictionary * _Nonnull)bundledFeedback;
+ (NSDictionary*)bundleNITFeedback:(NITFeedback * _Nonnull)feedback;
+ (NSDictionary*)bundleNITCustomJSON:(NITCustomJSON* _Nonnull)custom;
+ (NITImage*)unbundleNITImage:(NSDictionary* _Nonnull)bundledImage;
+ (NSDictionary*)bundleNITImage:(NITImage* _Nonnull)image;
+ (NSDictionary*)bundleNITContentLink:(NITContentLink* _Nonnull)cta;
+ (NITTrackingInfo*)unbundleTrackingInfo:(NSString * _Nullable)bundledTrackingInfo;
+ (NSString*)bundleTrackingInfo:(NITTrackingInfo* _Nullable)trackingInfo;

@end

