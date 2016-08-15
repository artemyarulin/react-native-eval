# react-native-eval

React has a good [tutorial](http://facebook.github.io/react-native/docs/embedded-app-ios.html#content) how to integrate React View to already existing application, but it doesn't provide a good way if you decided to migrate some of your business logic to JS first while maintaining the same UI.

## Automatic installation

* `npm install react-native-eval && react-native link react-native-eval`

### Manual installation `iOS`

*  `pod init`. Initialize [CocoaPods](https://cocoapods.org/).
*  Add following line to Podfile: `pod 'react-native-eval',:path => 'node_modules/react-native-eval/ios'`
*  `pod install`.  Update the project.

### Manual installation `Android`

* `android/settings.gradle`

```gradle
...
include ':react-native-eval'
project(':react-native-eval').projectDir = new File(settingsDir, '../node_modules/react-native-eval/android')
```
* `android/app/build.gradle`

```gradle
dependencies {
	...
	compile project(':react-native-eval')
}
```

* register module (in MainActivity.java)

```java
...

import com.evaluator.react.*; // <--- import

public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler {
	...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModuleName("index.android")
                .addPackage(new MainReactPackage())
                .addPackage(new RNMEPackage())           // <- add here
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();

        try {
            Method method = getReactContextInitialize();
            method.setAccessible(true);
            method.invoke(mReactInstanceManager);
            method.setAccessible(false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
    }

    private Method getReactContextInitialize() {
        Method method;
        try {
            // RN 14
            method = mReactInstanceManager.getClass()
              .getDeclaredMethod("initializeReactContext");
        } catch (NoSuchMethodException e) {
            method = null;
        }

        if (method == null) {
            try {
                // RN 15
                method = mReactInstanceManager.getClass()
                  .getDeclaredMethod("createReactContextInBackground");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return method;
    }
}
```

Buckle up, Dorothy

# iOS

*  Get a reference to `RCTBridge`, by getting it from `RCTRootView.bridge` that you have created (if you have any React Native view) or by creating `RCTBridge` manually:

```objc
RCTBridge* bridge = [[RCTBridge alloc] initWithBundleURL:[NSURL URLWithString:@"URL_TO_BUNDLE"]
                      moduleProvider:nil
                      launchOptions:nil];
RCTRootView* view = [[RCTRootView alloc] initWithBridge:bridge moduleName:@"app"];

// Call sync function
[RNMEvaluator callSyncFunction:bridge
                          name:@"Math.pow"
                          args:@[@2,@2]
                            cb:^(NSString *error, id returnValue) {
                                if (error)
                                    NSLog(@"Error occured: %@", error);
                                else
                                    NSLog(@"Function returned: %@", returnValue);
                            }];

// You can call async function as well. It has to have callback as a last argument.
// If callback would be called with Error object then it will be converted to
// NSString and passed as a first argument of native callback. Otherwise callback
// value would be passed as a second parameter
[RNMEvaluator callAsyncFunction:bridge
                           name:@"(function(a,b,cb) { setTimeout(function() { cb(Math.pow(a,b)) },0) })"
                           args:@[@2,@2]
                             cb:^(NSString *error, id returnValue) {
                                 if (error)
                                     NSLog(@"Error occured: %@", error);
                                 else
                                     NSLog(@"Function returned: %@", returnValue);
                             }]
```


# Javascript

*  Call `require('react-native-eval')`, to ensure that the module is included.
