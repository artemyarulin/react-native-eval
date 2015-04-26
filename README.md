# react-native-eval

React has a good [tutorial](http://facebook.github.io/react-native/docs/embedded-app.html#content) how to integrate React View to alrady existsing application, but it doesn't provide a good way if you decided to migrate some of your business logic to JS first while maintaining the same UI. Doing that you would be able to update your BL multiple times per day, without long wait time of review. 

React Native doesn't provide a way to [call any JS](https://github.com/facebook/react-native/blob/d937071517b47b3d2e54510a1f695885a27c5e52/React/Executors/RCTContextExecutor.m#L243), so react-native-eval comes to the rescue.

# Preparation
- Add a `react-native-eval` library to your project
- Get a referenct to `RCTBridge`, by getting it from `RCTRootView.bridge` that you have created (if you have any React Native view) or by creating `RCTBridge` manually:
```objc
[[RCTBridge alloc] initWithBundleURL:[NSURL URLWithString:@"URL_TO_BUNDLE"]
                      moduleProvider:nil
                       launchOptions:nil];
```
# Usage
```objc
[RNMEvaluator callFunction:bridge
                      name:@"Math.pow"
                      args:@[@2,@2]
                        cb:^(NSError *error, id returnValue) {
                            if (error)
                                NSLog(@"Error occured: %@", error);
                            else
                                NSLog(@"Function returned: %@", returnValue);
                        }];
```

