#import <XCTest/XCTest.h>
#import "RNMEvaluator.h"
#import "RCTBridge.h"
#import "RCTRootView.h"
#import "AGWaitForAsyncTestHelper.h"

@interface react_native_evalTests : XCTestCase

@end

@implementation react_native_evalTests
{
    BOOL isDone;
    RCTRootView* view;
    RCTBridge* bridge;
}

-(void)setUp {
    [super setUp];
    isDone = NO;
    
    NSString* bundlePath = [[NSBundle mainBundle] bundlePath];
    bridge = [[RCTBridge alloc] initWithBundleURL:[NSURL fileURLWithPath:[bundlePath stringByAppendingPathComponent:@"js/app.js"]]
                                   moduleProvider:nil
                                    launchOptions:nil];
    
    view = [[RCTRootView alloc] initWithBridge:bridge moduleName:@"app"];
}

- (void)execSync:(NSString*)cmd args:(NSArray*)args cb:(EvaluatorCallback)cb
{
    [RNMEvaluator callSyncFunction:bridge
                              name:cmd
                              args:args
                                cb:^(NSString *error, id returnValue) {
                                    cb(error,returnValue);
                                    isDone = YES;
                                }];
    if (!isDone)
        WAIT_WHILE(!isDone, 100);
}

- (void)execAsync:(NSString*)cmd args:(NSArray*)args cb:(EvaluatorCallback)cb
{
    [RNMEvaluator callAsyncFunction:bridge
                              name:cmd
                              args:args
                                cb:^(NSString *error, id returnValue) {
                                    cb(error,returnValue);
                                    isDone = YES;
                                }];
    if (!isDone)
        WAIT_WHILE(!isDone, 100);
}

- (void)testSync {
    [self execSync:@"Math.pow" args:@[@2,@2] cb:^(NSString *error, id returnValue) {
        XCTAssertNil(error,@"Error occured: %@", error);
        XCTAssertEqualObjects(returnValue, @4);
    }];
}

- (void)testAsync {
    [self execAsync:@"(function(a,b,cb) { setTimeout(function() { cb(Math.pow(a,b)) },0) })"
               args:@[@2,@2]
                 cb:^(NSString *error, id returnValue) {
                     XCTAssertNil(error,@"Error occured: %@", error);
                     XCTAssertEqualObjects(returnValue, @4);
    }];
}

- (void)testNotFound {
    [self execSync:@"NotDefined" args:@[] cb:^(NSString *error, id returnValue) {
        XCTAssert([error containsString:@"Error finding a function"],@"Incorrect message");
    }];
}

- (void)testNotFunction {
    [self execAsync:@"\"string\"" args:@[] cb:^(NSString *error, id returnValue) {
        XCTAssert([error containsString:@"not a function"],@"Incorrect message");
    }];
}

- (void)testSyncException {
    NSString* err = @"Ooops";
    [self execSync:@"(function(err) { throw new Error(err) })"
              args:@[err]
                cb:^(NSString *error, id returnValue) {
        XCTAssert([error containsString:err],@"Incorrect message");
    }];
}

- (void)testAsyncException {
    NSString* err = @"Ooops";
    [self execAsync:@"(function(err,cb) { setTimeout(function() { cb(new Error(err)) },0) })"
              args:@[err]
                cb:^(NSString *error, id returnValue) {
        XCTAssert([error containsString:err],@"Incorrect message");
    }];
}


@end
