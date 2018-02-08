# Installation

Minimum Requirements:

- **React Native**: ***0.45.0+***
- **Android** Min SDK: ***16***
- **iOS** Min Platform: ***iOS 9***

To start using the SDK, add this plugin to your React Native project

```bash
$ yarn add react-native-nearit
```

and link it

```bash
$ react-native link react-native-nearit
```

<br>

## Android

To setup NearIT SDK for Android

### NearIT API Key
Add a new string resource to the Android resources to specify your API key

```xml
<resources>
    ...
    <string name="nearit_api_key" translatable="false">Your.API.Key</string>
    ...
</resources>
```
You can find your API key on [NearIT web interface](https://go.nearit.com/), under the "SDK Integration" section.

If you don't have a resources file, create a new `secrets.xml` file under your project `android/app/src/main/res/values` folder and add the previous line inside of it.

**N.B:** We suggests you to ignore this file from your versioning system.

<br>

## iOS

To setup NearIT SDK for iOS

### NearIT API Key

Create the `NearIt.plist` file at `ios/<your app name>/NearIt.plist` to specify your API Key
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
    <dict>
        <key>API Key</key>
        <string>Your.API.Key</string>
    </dict>
</plist>
```

You can find your API key on [NearIT web interface](https://go.nearit.com/), under the "SDK Integration" section.


**N.B:** We suggests you to ignore this file from your versioning system.

<br>

## Usage ##

To interact with `NearIT` SDK from your React code use the `NearItManager` class exported by the `react-native-nearit` module
```js
import NearIT from 'react-native-nearit'
```

## Enable test devices

Test Devices allow you to test NearIT features on single devices and is extremely useful for debugging.

Navigate "Settings > Test devices" section of NearIT and follow the instructions to enable this feature both for Android and iOS.

In the same section, you can send invite links to mail addresses. If users have the app installed, they can click the link on their smart-phone to be prompted with a request to enroll their device among the testers.

## iOS

After the `Info.plist` update, you have to override the AppDelegate method for URL management:

```ojb-c
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication annotation:(id)annotation {
      [RNNearIt application:application openURL:url options:nil];
  }
```

Finally, you have to open the iOS project of your app in Xcode and add the following file
`../YOUR_PROJECT/node_modules/react-native-nearit/ios/NearITSDK/NearITResources.bundle`
to `Copy bundle resources` in `Build phases` section of your target.