var fs = require('fs')
var glob = require('glob')
var emoji = require('../utils').emoji

const packageJson = require('../../../../package.json')

module.exports = () => {
  console.log(emoji.apple, 'Running iOS postlink script')

  var ignoreNodeModules = { ignore: 'node_modules/**' }
  var appDelegatePaths = glob.sync('**/AppDelegate.+(mm|m)', ignoreNodeModules)

  // Typical location of AppDelegate.m for newer RN versions: $PROJECT_ROOT/ios/<project_name>/AppDelegate.m
  // Let's try to find that path by filtering the whole array for any path containing <project_name>
  // If we can't find it there, play dumb and pray it is the first path we find.
  var appDelegatePath = findFileByAppName(appDelegatePaths, packageJson ? packageJson.name : null) || appDelegatePaths[0]

  if (!appDelegatePath) {
    return Promise.reject(new Error(`Couldn't find AppDelegate. You might need to update it manually \
    Please refer to plugin configuration section for iOS at \
    https://nearit-react-native-sdk.readthedocs.io/en/latest/manual-installation-ios/`))
  }

  var appDelegateContents = fs.readFileSync(appDelegatePath, 'utf8')

  // 1. Add the header import statement
  var nearItHeaderImportStatement = `#import "RNNearIt.h"`
  if (~appDelegateContents.indexOf(nearItHeaderImportStatement)) {
    console.log(emoji.ok, `"RNNearIT.h" header already imported.`)
  } else {
    console.log(emoji.running, `Editing AppDelegate.m to import RNNearIT.h`)
    var appDelegateHeaderImportStatement = `#import "AppDelegate.h"`
    appDelegateContents = appDelegateContents.replace(appDelegateHeaderImportStatement,
            `${appDelegateHeaderImportStatement}\n${nearItHeaderImportStatement}`)
  }

  // 2. Add RegisterForRemoteNotification method invocation
  var nearItAppDelegateRegisterMethodCall = `[RNNearIt registerForRemoteNotifications];`
  if (~appDelegateContents.indexOf(nearItAppDelegateRegisterMethodCall)) {
    console.log(emoji.ok, `"[RNNearIt registerForRemoteNotifications];" already added.`)
  } else {
    console.log(emoji.running, `Editing AppDelegate.m to enable Remote Notification`)
    var appDelegateMakeKeyAndVisible = `[self.window makeKeyAndVisible];`
    appDelegateContents = appDelegateContents.replace(appDelegateMakeKeyAndVisible,
            `${appDelegateMakeKeyAndVisible}\n\n  ${nearItAppDelegateRegisterMethodCall}\n`)
  }

  // 3. Add didFinishLaunchingWithOptions method invocation
  var nearItAppDelegateFinishLaunchingMethodCall = `[RNNearIt didFinishLaunchingWithOptions:launchOptions];`
  if (~appDelegateContents.indexOf(nearItAppDelegateFinishLaunchingMethodCall)) {
    console.log(emoji.ok, `"[RNNearIt didFinishLaunchingWithOptions:launchOptions];" already added.`)
  } else {
    console.log(emoji.running, `Editing AppDelegate.m to enable Remote Notification when app is killed`)
    appDelegateContents = appDelegateContents.replace(nearItAppDelegateRegisterMethodCall,
      `${nearItAppDelegateRegisterMethodCall}\n  ${nearItAppDelegateFinishLaunchingMethodCall}\n`)
  }

  // 4. Add Passthrough functions
  var nearItPassthroughComment = `// Needed by NearIT plugin -- DO NOT REMOVE THIS COMMENT`
  if (~appDelegateContents.indexOf(nearItPassthroughComment)) {
    console.log(emoji.ok, `Passthrough functions already added.`)
  } else {
    console.log(emoji.running, `Editing AppDelegate.m to add Passthrough functions`)
    var appDelegateEndTag = `@end`
    var nearItPassthroughFunctions = getPassthroughFunctionsCalls()
    appDelegateContents = appDelegateContents.replace(appDelegateEndTag,
            `${nearItPassthroughComment}\n${nearItPassthroughFunctions}\n\n${appDelegateEndTag}`)
  }

  // Commit changes to files
  writePatches()

  // Resolve promise
  return Promise.resolve()

  // Internal functions

  // Push notifications functions
  function getPassthroughFunctionsCalls () {
    return `
- (void)application:(UIApplication*) application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
  [RNNearIt didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)application:(UIApplication*) application didReceiveLocalNotification:(UILocalNotification *)notification {
  [RNNearIt didReceiveLocalNotification:notification];
}

- (void)application:(UIApplication*) application didReceiveRemoteNotification:(NSDictionary *)userInfo {
  [RNNearIt didReceiveRemoteNotification:userInfo];
}`
  }

  // Commit changes to files
  function writePatches () {
    fs.writeFileSync(appDelegatePath, appDelegateContents)
  }

  // Helper that filters an array with AppDelegate.m paths for a path with the app name inside it
  // Should cover nearly all cases
  function findFileByAppName (array, appName) {
    if (array.length === 0 || !appName) return null

    for (var i = 0; i < array.length; i++) {
      var path = array[i]
      if (path && path.indexOf(appName) !== -1) {
        return path
      }
    }

    return null
  }
}
