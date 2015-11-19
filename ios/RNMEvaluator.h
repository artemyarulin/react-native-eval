#import "RCTBridgeModule.h"

@interface RNMEvaluator : NSObject <RCTBridgeModule>

typedef void (^EvaluatorCallback)(NSString* error, id returnValue);

+(void)callSyncFunction:(RCTBridge*)bridge name:(NSString *)name args:(NSArray *)args cb:(EvaluatorCallback)cb;
+(void)callAsyncFunction:(RCTBridge*)bridge name:(NSString *)name args:(NSArray *)args cb:(EvaluatorCallback)cb;

@end
