#import "RNMEvaluator.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"

@implementation RNMEvaluator

RCT_EXPORT_MODULE()

static NSMutableDictionary* callbacks;

RCT_EXPORT_METHOD(functionCallCompleted:(NSString*)callId error:(NSString*)error returnVaue:(id)returnVaue)
{
    EvaluatorCallback cb = [callbacks objectForKey:callId];
    cb(error,returnVaue);
    [callbacks removeObjectForKey:callId];
}

+(void)callFunction:(RCTBridge*)bridge name:(NSString *)name args:(NSArray *)args cb:(EvaluatorCallback)cb
{
    if (bridge.loading) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self callFunction:bridge name:name args:args cb:cb];
        });
        return;
    }
    
    NSString* callId = [[NSUUID UUID] UUIDString];

    if (!callbacks)
        callbacks = [@{} mutableCopy];
    
    callbacks[callId] = cb ? cb : (^(NSString* e, id v) { });
    
    [bridge.eventDispatcher sendAppEventWithName:@"RNMEvaluator.callFunctionSync"
                                               body:@{@"name": name,
                                                      @"args": args ? args : @[],
                                                      @"callId": callId}];
}

@end
