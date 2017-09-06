
#import "RNNearIt.h"

NSString* const RN_NATIVE_EVENTS_TOPIC = @"RNNearItEvent";

NSString* const EVENT_TYPE_SIMPLE = @"NearIt.Events.SimpleNotification";
NSString* const EVENT_TYPE_CUSTOM_JSON = @"NearIt.Events.CustomJSON";

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
             @"EventContent": @{},
             @"Statuses": @{}
            };
}

@end
  