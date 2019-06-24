//
//  RNNearItUtils.m
//  RNNearIt
//
//  Created by Federico Boschini <federico@nearit.com> on 10/06/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "RNNearItUtils.h"

#define TAG @"RNNearItUtils"

@implementation RNNearItUtils

// NITTrackingInfo

+ (NSString* _Nullable)bundleTrackingInfo:(NITTrackingInfo* _Nullable) trackingInfo
{
    NSString* trackingInfoB64;
    if (trackingInfo) {
        NSData* trackingInfoData = [NSKeyedArchiver archivedDataWithRootObject:trackingInfo];
        trackingInfoB64 = [trackingInfoData base64EncodedStringWithOptions:0];
    }
    
    return trackingInfoB64;
}

+ (NITTrackingInfo* _Nullable)unbundleTrackingInfo:(NSString * _Nullable)bundledTrackingInfo
{
    NSData* trackingInfoData = [[NSData alloc] initWithBase64EncodedString:bundledTrackingInfo
                                                                   options:NSDataBase64DecodingIgnoreUnknownCharacters];
    
    NITTrackingInfo *trackingInfo = [NSKeyedUnarchiver unarchiveObjectWithData:trackingInfoData];
    return trackingInfo;
}

// NITCoupon

+ (NSDictionary* _Nullable)bundleNITCoupon:(NITCoupon* _Nonnull) coupon
{
    NSMutableDictionary* couponDictionary = [[NSMutableDictionary alloc] init];
    [couponDictionary setObject:(coupon.title ? coupon.title : [NSNull null])
                         forKey:EVENT_CONTENT_TITLE];
    [couponDictionary setObject:(coupon.couponDescription ? coupon.couponDescription : [NSNull null])
                         forKey:EVENT_COUPON_DESCRIPTION];
    [couponDictionary setObject:(coupon.value ? coupon.value : [NSNull null])
                         forKey:EVENT_COUPON_VALUE];
    [couponDictionary setObject:(coupon.expiresAt ? coupon.expiresAt : [NSNull null])
                         forKey:EVENT_COUPON_EXPIRES_AT];
    [couponDictionary setObject:(coupon.redeemableFrom ? coupon.redeemableFrom : [NSNull null])
                         forKey:EVENT_COUPON_REDEEMABLE_FROM];
    [couponDictionary setObject:(coupon.serial ? coupon.serial : [NSNull null])
                         forKey:EVENT_COUPON_SERIAL];
    [couponDictionary setObject:(coupon.claimedAt ? coupon.claimedAt : [NSNull null])
                         forKey:EVENT_COUPON_CLAIMED_AT];
    [couponDictionary setObject:(coupon.redeemedAt ? coupon.redeemedAt : [NSNull null])
                         forKey:EVENT_COUPON_REDEEMED_AT];
    
    if (coupon.icon) {
        if (coupon.icon.url || coupon.icon.smallSizeURL) {
            [couponDictionary setObject:[self bundleNITImage:coupon.icon] forKey:EVENT_IMAGE];
        }
    }
    
    NSData* couponData = [NSKeyedArchiver archivedDataWithRootObject:coupon];
    NSString* couponB64 = [couponData base64EncodedStringWithOptions:0];
    [couponDictionary setObject:couponB64 forKey:@"couponData"];
    
    return couponDictionary;
}

+ (NITCoupon* _Nullable)unbundleNITCoupon:(NSDictionary* _Nonnull)bundledCoupon
{
    NSString* couponString = [bundledCoupon objectForKey:@"couponData"];
    NSData* couponData = [[NSData alloc] initWithBase64EncodedString:couponString
                                                             options:NSDataBase64DecodingIgnoreUnknownCharacters];
    NITCoupon *coupon = [NSKeyedUnarchiver unarchiveObjectWithData:couponData];
    return coupon;
}

// NITContent

+ (NSDictionary* _Nullable)bundleNITContent:(NITContent * _Nonnull) content
{
    NSString* title = [content title];
    if (!title) {
        title = [NSNull null];
    }
    
    NSString* text = [content content];
    if (!text) {
        text = [NSNull null];
    }
    
    id image;
    if (content.image) {
        image = [self bundleNITImage:content.image];
    } else {
        image = [NSNull null];
    }
    
    id cta;
    if (content.link) {
        cta = [self bundleNITContentLink:content.link];
    } else {
        cta = [NSNull null];
    }
    
    NSDictionary* bundledContent = @{
                                     EVENT_CONTENT_TITLE:title,
                                     EVENT_CONTENT_TEXT:text,
                                     EVENT_IMAGE:image,
                                     EVENT_CONTENT_CTA:cta
                                     };
    
    return bundledContent;
}

+ (NITContent* _Nullable)unbundleNITContent:(NSDictionary * _Nonnull)bundledContent
{
    NITContent* content = [[NITContent alloc] init];
    content.title = [bundledContent objectForKey:EVENT_CONTENT_TITLE];
    content.content = [bundledContent objectForKey:EVENT_CONTENT_TEXT];
    content.images = @[[self unbundleNITImage: [bundledContent objectForKey:EVENT_IMAGE]]];
    content.internalLink = [bundledContent objectForKey:EVENT_CONTENT_CTA];
    return content;
}

// NITFeedback

+ (NSDictionary* _Nullable)bundleNITFeedback:(NITFeedback * _Nonnull) feedback
{
    NSData* feedbackData = [NSKeyedArchiver archivedDataWithRootObject:feedback];
    NSString* feedbackB64 = [feedbackData base64EncodedStringWithOptions:0];
    
    NSDictionary* bundledFeedback = @{
                                      EVENT_FEEDBACK_ID: feedbackB64,
                                      EVENT_FEEDBACK_QUESTION: [feedback question]
                                      };
    
    return bundledFeedback;
}

+ (NITFeedback* _Nullable)unbundleNITFeedback:(NSDictionary * _Nonnull) bundledFeedback
{
    NSString* feedbackId = [bundledFeedback objectForKey:EVENT_FEEDBACK_ID];
    NSData* feedbackData = [[NSData alloc] initWithBase64EncodedString:feedbackId
                                                               options:NSDataBase64DecodingIgnoreUnknownCharacters];
    
    NITFeedback *feedback = [NSKeyedUnarchiver unarchiveObjectWithData:feedbackData];
    feedback.question = [bundledFeedback objectForKey:EVENT_FEEDBACK_QUESTION];
    return feedback;
}

// NITCustomJSON

+ (NSDictionary* _Nullable)bundleNITCustomJSON:(NITCustomJSON* _Nonnull) custom
{
    return @{[custom content]};;
}

// NITImage

- (NSDictionary* _Nullable)bundleNITImage:(NITImage* _Nonnull)image
{
    NSData* imageData = [NSKeyedArchiver archivedDataWithRootObject:image];
    NSString* imageB64 = [imageData base64EncodedStringWithOptions:0];
    return @{
             @"imageData": imageB64,
             EVENT_IMAGE_FULL_SIZE: (image.url ? [image.url absoluteString] : [NSNull null]),
             EVENT_IMAGE_SQUARE_SIZE: (image.smallSizeURL ? [image.smallSizeURL absoluteString] : [NSNull null])
             };
}

- (NITImage* _Nullable)unbundleNITImage:(NSDictionary* _Nonnull)bundledImage
{
    NITImage* image = [[NITImage alloc] init];
    if ([bundledImage objectForKey:@"imageData"]) {
        NSData* imageData = [[NSData alloc] initWithBase64EncodedString:[bundledImage objectForKey:@"imageData"]
                                                                options:NSDataBase64DecodingIgnoreUnknownCharacters];
        image = [NSKeyedUnarchiver unarchiveObjectWithData:imageData];
    }
    return image;
}

// NITContentLink

- (NSDictionary* _Nullable)bundleNITContentLink:(NITContentLink* _Nonnull)cta
{
    return @{
             EVENT_CONTENT_CTA_LABEL: cta.label,
             EVENT_CONTENT_CTA_URL: [cta.url absoluteString]
             };
}

- (NITContentLink* _Nullable)unbundleNITContentLink:(NSDictionary* _Nonnull)bundledCta
{
    
}

// NITHistoryItem

- (NSDictionary* _Nullable)bundleNITHistoryItem:(NITHistoryItem* _Nonnull) item
{
    NSMutableDictionary* historyDictionary = [[NSMutableDictionary alloc] init];
    
    NSNumber *read = [NSNumber numberWithBool:item.read];
    NSNumber *timestamp = [NSNumber numberWithDouble:item.timestamp];
    NSString *bundledTrackingInfo = [self bundleTrackingInfo:item.trackingInfo];
    NSNumber *isNew = [NSNumber numberWithBool:item.isNew];
    
    [historyDictionary setObject:read forKey:NOTIFICATION_HISTORY_READ];
    [historyDictionary setObject:timestamp forKey:NOTIFICATION_HISTORY_TIMESTAMP];
    [historyDictionary setObject:isNew forKey:NOTIFICATION_HISTORY_IS_NEW];
    [historyDictionary setObject:(bundledTrackingInfo ? bundledTrackingInfo : [NSNull null]) forKey:EVENT_TRACKING_INFO];
    
    NSString* message = item.reactionBundle.notificationMessage;
    if (!message) {
        message = [NSNull null];
    }
    [historyDictionary setObject:message forKey:EVENT_CONTENT_MESSAGE];
    
    if ([item.reactionBundle isKindOfClass:[NITSimpleNotification class]]) {
        
        [historyDictionary setObject:EVENT_TYPE_SIMPLE forKey:EVENT_TYPE];
        
    } else if ([item.reactionBundle isKindOfClass:[NITContent class]]) {
        
        [historyDictionary setObject:EVENT_TYPE_CONTENT forKey:EVENT_TYPE];
        
        NITContent *nearContent = (NITContent*)item.reactionBundle;
        NSDictionary* content = [self bundleNITContent:nearContent];
        [historyDictionary setObject:content forKey:NOTIFICATION_HISTORY_CONTENT];
        
    } else if ([item.reactionBundle isKindOfClass:[NITFeedback class]]) {
        
        [historyDictionary setObject:EVENT_TYPE_FEEDBACK forKey:EVENT_TYPE];
        
        NITFeedback* nearFeedback = (NITFeedback*)item.reactionBundle;
        NSDictionary* feedback = [self bundleNITFeedback:nearFeedback];
        [historyDictionary setObject:feedback forKey:NOTIFICATION_HISTORY_CONTENT];
        
    } else if ([item.reactionBundle isKindOfClass:[NITCoupon class]]) {
        
        [historyDictionary setObject:EVENT_TYPE_COUPON forKey:EVENT_TYPE];
        
        
    } else if ([item.reactionBundle isKindOfClass:[NITCustomJSON class]]) {
        
        [historyDictionary setObject:EVENT_TYPE_CUSTOM_JSON forKey:EVENT_TYPE];
        
        NITCustomJSON *nearCustom = (NITCustomJSON*)item.reactionBundle;
        NSDictionary* custom = [self bundleNITCustomJSON:nearCustom];
        [historyDictionary setObject:custom forKey:NOTIFICATION_HISTORY_CONTENT];
    }
    
    return historyDictionary;
}

@end
