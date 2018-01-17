# React Native NearIT SDK

> The official [NearIT](https://www.nearit.com) SDK plugin for [React Native](https://facebook.github.io/react-native/)

[![license](https://img.shields.io/github/license/panz3r/react-native-nearit-sdk.svg)](https://github.com/panz3r/react-native-nearit-sdk/blob/master/LICENSE.md)
[![GitHub release](https://img.shields.io/github/release/nearit/React-Native-SDK/all.svg)](https://github.com/nearit/react-native-nearit-sdk/releases)
[![npm](https://img.shields.io/npm/v/react-native-nearit.svg)](https://www.npmjs.com/package/react-native-nearit)

[![React Native](https://img.shields.io/badge/RN-0.45.0+-green.svg)](https://facebook.github.io/react-native/)
![platforms](https://img.shields.io/badge/platforms-Android%20%7C%20iOS-brightgreen.svg)
[![Android](https://img.shields.io/badge/Android-16-blue.svg)](https://developer.android.com/about/dashboards/index.html#Platform)
[![iOS](https://img.shields.io/badge/iOS-9-blue.svg)](https://developer.apple.com/ios/)

[![Documentation Status](https://readthedocs.org/projects/nearit-react-native-sdk/badge/?version=latest)](https://nearit-react-native-sdk.readthedocs.io/en/latest/?badge=latest)
[![Gitter](https://img.shields.io/gitter/room/nearit/Lobby.svg)](https://gitter.im/nearit/Lobby)

NearIT allows to engage app users by sending **context-aware targeted content**.

## Recipes

NearIT allows to manage apps by defining "recipes". Those are simple rules made of 3 ingredients:

* **WHO**: define the target users
* **WHAT**: define what action NearIT should do
* **TRIGGER**: define when the action should be triggered

<br>

## How it works

[**NearIT web interface**](https://go.nearit.com/) allows you to configure all the features quickly.
Once the settings are configured, **everyone** - even people without technical skills - can manage context-aware mobile content.

**NearIT SDK** synchronizes with servers and behaves accordingly to the settings and the recipes. Any content will be delivered at the right time, you just need to handle its presentation.

<br>

## Installation

Minimum Requirements:

* **React Native**: 0.45.0+
* **Android** Min SDK: **_16_**
* **iOS** Min Platform: **_iOS 9_**

To start using the SDK, add this plugin to your React Native project

```bash
$ yarn add react-native-nearit
```

and link it

```bash
$ react-native link react-native-nearit
```

<br/>

## Integration guide

For information on how to integrate all NearIT features in your app, visit the [documentation website](https://nearit-react-native-sdk.readthedocs.io/)

<br/>

## Sample Integration

A sample ReactNative app integrating `react-native-nearit` SDK is available at [panz3r/nearit-react-native-sample-app](https://github.com/panz3r/nearit-react-native-sample-app)

<br/>

---

Made with :sparkles: & :heart: by [Mattia Panzeri](https://github.com/panz3r) and [contributors](https://github.com/nearit/React-Native-SDK/graphs/contributors)
