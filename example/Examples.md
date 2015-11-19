# Examples

## Android

The android app is a standard android application that loads in the react-native framework,
and runs arbitrary javascript code, with the help of the react-native-eval module.

### Setup

`npm install` From the android subdirectory, this will install any dependencies that the app itself has, including react-native.

#### Running

*  `$ cd android`
*  `$ npm start`
*  Open `index.android.js` in your text editor of choice and edit some lines.
*  Press the menu button (F2 by default, or ⌘-M in Genymotion) and select Reload JS to see your change!
*  Run `adb logcat *:S ReactNative:V ReactNativeJS:V` in a terminal to see your app's logs

***Note:*** If you are using a device, see the [Running on Device page](http://facebook.github.io/react-native/docs/running-on-device-android.html#content).

If you run into any issues getting started, see the [troubleshooting page](https://facebook.github.io/react-native/docs/troubleshooting.html#content).
