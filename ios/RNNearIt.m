
#import "RNNearIt.h"
#import <NearITSDK/NearITSDK.h>

NSString* const RN_NATIVE_EVENTS_TOPIC = @"RNNearItEvent";

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

@implementation RNNearIt

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

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

@end
  
