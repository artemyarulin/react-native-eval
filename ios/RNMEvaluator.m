#import "RNMEvaluator.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"

@implementation RNMEvaluator

RCT_EXPORT_MODULE()

static NSMutableDictionary* callbacks;

/**
 * This broadcasts the given event via the NSNotficationCenter
 */
RCT_EXPORT_METHOD(emit:(NSString*)event value:(NSDictionary*)value)
{
    [[NSNotificationCenter defaultCenter] postNotificationName:event
                                                        object:(value==nil?nil:value[@"_value"])
                                                      userInfo:nil];
}

RCT_EXPORT_METHOD(functionCallCompleted:(NSString*)callId error:(NSString*)error returnVaue:(NSDictionary*)returnVaue)
{
    EvaluatorCallback cb = [callbacks objectForKey:callId];
    cb(error,returnVaue==nil?nil:returnVaue[@"_value"]);
    [callbacks removeObjectForKey:callId];
}

+(void)callSyncFunction:(RCTBridge*)bridge name:(NSString *)name args:(NSArray *)args cb:(EvaluatorCallback)cb {
    [self callFunction:bridge name:name args:args cb:cb event:@"RNMEvaluator.callSyncFunction"];
}

+(void)callAsyncFunction:(RCTBridge*)bridge name:(NSString *)name args:(NSArray *)args cb:(EvaluatorCallback)cb {
    [self callFunction:bridge name:name args:args cb:cb event:@"RNMEvaluator.callAsyncFunction"];
}

+(void)callFunction:(RCTBridge*)bridge name:(NSString *)name args:(NSArray *)args cb:(EvaluatorCallback)cb event:(NSString*)event {
    NSString* callId = [[NSUUID UUID] UUIDString];

    if (!callbacks)
        callbacks = [@{} mutableCopy];

    callbacks[callId] = cb ? cb : (^(NSString* e, id v) { });

    // TODO: move to sendAppEventWithName once App events are supported on android.
    [bridge.eventDispatcher sendDeviceEventWithName:event
                                            body:@{@"name": name,
                                                   @"args": args ? args : @[],
                                                   @"callId": callId}];
}

@end
