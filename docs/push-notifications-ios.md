# Push Notifications

To enable push notification you will need to enable **Push Capabilities** in your app.<br>

**N.B:** Remember to open `ios/<your-app-name>.xcworkspace` file when opening XCode.

<br>

## iOS App Setup

### Edit AppDelegate.m
Edit your project `ios/<app-name>/AppDelegate.m` as follow

- add the following import (after the `React` ones)
```obj-c
// Import 'RNNearIt` headers - Needed by NearIT plugin
#import "RNNearIt.h"
```

- add `RNNearIT` notification handling methods

```obj-c
...
// Needed by NearIT plugin
- (void)application:(UIApplication*) application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
  [RNNearIt didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)application:(UIApplication*) application didReceiveLocalNotification:(UILocalNotification *)notification {
  [RNNearIt didReceiveLocalNotification:notification];
}

- (void)application:(UIApplication*) application didReceiveRemoteNotification:(NSDictionary *)userInfo {
  [RNNearIt didReceiveRemoteNotification:userInfo];
}
```

<br>

## Enable push capabilities

Inside Xcode, navigate to the “**Capabilities**” tab of your app. You will need to enable “**Push Notifications**”.

![capabilities](push_help/capabilities.png "")

## Get Push Certificates

### 1) Generating a Certificate Request

1.1) Launch the “**Keychain Access**” app on your Mac.

1.2) Select “**Keychain Access> Certificate Assistant> Request a Certificate From a Certificate Authority**”.

![keychain_request](push_help/pushtutorial00.png "")

1.3) Enter your e-mail, select “**Save to disk**” and press “**Continue**”. You should get a .certSigningRequest file.

![save_request](push_help/pushtutorial01.png "")


### 2) Get the Certificate

2.1 Open [developer.apple.com](https://developer.apple.com/account/ios/identifier/bundle), select your app and press “**Edit**”.

![edit_app](push_help/pushtutorial02.gif "")

2.2 Scroll down the page and enable Push Notifications. Press “**Create Certificate**”.

![enable_push](push_help/pushtutorial03.gif "")

2.3 You should find yourself in the “Add iOS Certificate” wizard. Press “**Continue**”.

![add_certificate](push_help/pushtutorial04.png "")

2.4 Press “**Choose File**” and upload the .certSigningRequest file you generated previously (chapter 1.3)

![add_certificate](push_help/pushtutorial05.png "")

2.5 Your certificate is ready. Press “**Download**” to get the .cer file.

![get_certificate](push_help/pushtutorial06.png "")


### 3) Upload a .p12 Key to NearIT

3.1 Click on the .cer certificate you generated previously (chapter 2.5). It will open the “**Keychain Access**” app on your mac.


3.2 Find your certificate, click on it and select “**Export**”. Enter a password, you will get a .p12 file.

![export_p12](push_help/pushtutorial08.png "")


3.3 Open [NearIT](https://go.nearit.com), select your app and navigate to “**Settings> Push Settings**”. Upload your .p12 under the “**Setup iOS push notifications**” block. You will be prompted to enter the password you have chosen previously (chapter 3.2)

![export_p12](push_help/09.gif "")