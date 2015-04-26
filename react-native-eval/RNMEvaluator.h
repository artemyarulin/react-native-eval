#import "RCTBridgeModule.h"

@interface RNMEvaluator : NSObject <RCTBridgeModule>

typedef void (^EvaluatorCallback)(NSError* error, id returnValue);

+(void)callFunction:(RCTBridge*)bridge name:(NSString*)name args:(NSArray*)args cb:(EvaluatorCallback)cb;

@end
