//
//  NITReactionState.h
//  NearITSDK
//
//  Created by Cattaneo Stefano on 23/05/2018.
//  Copyright © 2018 NearIT. All rights reserved.
//

#import <Foundation/Foundation.h>

@class NITCacheManager;

extern NSTimeInterval const NITReactionStateDefaultTime;
extern NSString* _Nonnull const NITReactionStateCacheKey;
extern NSString* _Nonnull const NITReactionLastEditedTimeKey;

@interface NITReactionState : NSObject

@property (nonatomic) NSTimeInterval lastEditedTime;

- (instancetype _Nonnull)initWithCacheManager:(NITCacheManager* _Nonnull)cacheManager;

@end
