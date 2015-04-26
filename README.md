# react-native-eval

React has a good [tutorial](http://facebook.github.io/react-native/docs/embedded-app.html#content) how to integrate React View to alrady existsing application, but it doesn't provide a good way if you decided to migrate some of your business logic to JS first while maintaining the same UI.

React Native doesn't provide a way to [call any JS](https://github.com/facebook/react-native/blob/d937071517b47b3d2e54510a1f695885a27c5e52/React/Executors/RCTContextExecutor.m#L243), so react-native-eval comes to the rescue.

# Installation

- `npm init` if it is your first module and you don't have `package.json` yet
- `npm install --save react-native-eval`
- Drag `react-native-eval.xcodeproj` from the `node_modules/react-native-eval` folder into your XCode project
- Click on the your project in XCode, goto Build Phases then Link Binary With Libraries and add `libreact-native-eval.a`
- Add `node_modules/react-native-eval` to `Header search path`
- If you cannot compile `react-native-eval` because of `RCTBridgeModule.h` missing - open `react-native-eval.xcodeproject` that you've just added and append `Header search path` with React-Native location

# Usage
- Get a referenct to `RCTBridge`, by getting it from `RCTRootView.bridge` that you have created (if you have any React Native view) or by creating `RCTBridge` manually:
```objc
RCTBridge* bridge = [[RCTBridge alloc] initWithBundleURL:[NSURL URLWithString:@"URL_TO_BUNDLE"]
                      moduleProvider:nil
                       launchOptions:nil];

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

On a JS side be sure to `require('RNMEvaluator')`.
