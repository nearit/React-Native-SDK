# Installation #

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

## Android ##
To setup NearIT SDK for Android, simply add a new string resource to the Android resources specify your API key

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

## iOS ##
To setup NearIT SDK for iOS, you'll need `Cocoapods`.

### Cocoapod setup ###
Follow the instructions to install Cocoapods [here](https://guides.cocoapods.org/using/getting-started.html#getting-started), then initialize the `Podfile` inside your project `ios` folder.
```bash
$ cd ios
$ pod init
```

**NOTE:** The Podfile needs to be initialised in the `ios` directory of your project. Make sure to update cocoapods libs first by running `pod update`

### Dependency setup ###
Edit the created `Podfile` as follow:

- set `platform` to `9.0`
- add `NearITSDK` as dependency to your main project target

The resulting `Podfile` should look like the following
```ruby
# Uncomment the next line to define a global platform for your project
platform :ios, '9.0'
...
target 'nearsdksample' do
  ...
  # Pods for nearsdksample
  pod 'NearITSDK'
  ...
end
...
```

Run `pod install` to install the required dependencies

### iOS App Setup ###
Edit the file `ios/<your app name>/AppDelegate.m` and

- add the following import (after the `React` ones)
```obj-c
#import <NearITSDK/NearITSDK.h>
```

- add `NearIT` initialization method as first line inside the initialization method

```obj-c
...
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    [NITManager setupWithApiKey:@"Your.API.Key"]; // Needed by NearIT plugin

    ...
}
...
```
You can find your API key on [NearIT web interface](https://go.nearit.com/), under the "SDK Integration" section.

<br>

## Usage ##

To interact with `NearIT` SDK from your React code use the `NearItManager` class exported by the `react-native-nearit` module
```js
import NearIT from 'react-native-nearit'
```


<br>

## Manual Configuration Refresh ##

The SDK **initialization is done automatically** and handles the task of syncing the recipes with our servers when your app starts up.
<br>
However, if you need to sync the recipes configuration more often, you can call this method:

```js
NearIT.refreshConfig();
```

This call return a [Promise](https://developer.mozilla.org/it/docs/Web/JavaScript/Reference/Global_Objects/Promise) that is `resolved` as soon as the operation is completed successfully or `rejected` in case of errors.