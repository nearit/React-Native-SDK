var fs = require('fs')
var glob = require('glob')
var emoji = require('../utils').emoji

const packageJson = require('../../../../package.json')

module.exports = () => {
  console.log(emoji.apple, 'Running iOS postlink script')

  var ignoreNodeModules = { ignore: [ 'node_modules/**', '**/build/**' ] }
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

  var infoPlistPaths = glob.sync('**/Info.plist', ignoreNodeModules)
  var infoPlistPath = findFileByAppName(infoPlistPaths, packageJson ? packageJson.name : null) || infoPlistPaths[0]

  if (!infoPlistPath) {
    return Promise.reject(new Error(`Couldn't find Info.plist file. You might need to update it manually \
    Please refer to plugin configuration section for iOS at \
    https://nearit-react-native-sdk.readthedocs.io/en/latest/manual-installation-ios/`))
  }

  var projectPbxPaths = glob.sync('**/project.pbxproj', ignoreNodeModules)
  var projectPbxPath = findFileByAppName(projectPbxPaths, packageJson ? packageJson.name : null) || projectPbxPaths[0]

  if (!projectPbxPaths) {
    return Promise.reject(new Error(`Couldn't find project.pbxproj file. You might need to enable Background Modes capability manually \
    Please refer to plugin configuration section for iOS at \
    https://nearit-react-native-sdk.readthedocs.io/en/latest/manual-installation-ios/`))
  }

  var appDelegateContents = fs.readFileSync(appDelegatePath, 'utf8')
  var infoPlistContents = fs.readFileSync(infoPlistPath, 'utf8')
  var pbxprojFileContents = fs.readFileSync(projectPbxPath, 'utf8')

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
  var nearItAppDelegateFinishLaunchingMethodCall = `[RNNearIt application:application didFinishLaunchingWithOptions:launchOptions];`
  if (~appDelegateContents.indexOf(nearItAppDelegateFinishLaunchingMethodCall)) {
    console.log(emoji.ok, `"[RNNearIt application:application didFinishLaunchingWithOptions:launchOptions];" already added.`)
  } else {
    console.log(emoji.running, `Editing AppDelegate.m to enable Remote Notification when app is killed`)
    appDelegateContents = appDelegateContents.replace(nearItAppDelegateRegisterMethodCall,
      `${nearItAppDelegateRegisterMethodCall}\n  ${nearItAppDelegateFinishLaunchingMethodCall}\n`)
  }

  // AppDelegate.m @end tag
  var appDelegateEndTag = `@end`

  // 4. Add Passthrough functions
  var nearItPassthroughComment = `// Needed by NearIT plugin -- DO NOT REMOVE THIS COMMENT`
  if (~appDelegateContents.indexOf(nearItPassthroughComment)) {
    console.log(emoji.ok, `Passthrough functions already added.`)
  } else {
    console.log(emoji.running, `Editing AppDelegate.m to add Passthrough functions`)
    var nearItPassthroughFunctions = getPassthroughFunctionsCalls()
    appDelegateContents = appDelegateContents.replace(appDelegateEndTag,
            `${nearItPassthroughComment}\n${nearItPassthroughFunctions}\n\n${appDelegateEndTag}`)
  }

  // 5. Enable Background Fetch

  // 5.1 Add passthrough function to AppDelegate.m
  var nearItBackgroundFetchComment = `// Needed by NearIT plugin -- Enable Background Updates -- DO NOT REMOVE THIS COMMENT`
  if (~appDelegateContents.indexOf(nearItBackgroundFetchComment)) {
    console.log(emoji.ok, `Background Fetch passthrough function already added.`)
  } else {
    console.log(emoji.running, `Editing AppDelegate.m to add Background Fetch passthrough function`)
    var nearItBkgFetchFunctions = getBackgroundFetchPassthroughFunctionsCalls()
    appDelegateContents = appDelegateContents.replace(appDelegateEndTag,
      `${nearItBackgroundFetchComment}\n${nearItBkgFetchFunctions}\n\n${appDelegateEndTag}`)
  }

  // 5.2 Patch Info.plist
  var uiBkgModesPlistEntry = `<key>UIBackgroundModes</key>`
  if (~infoPlistContents.indexOf(uiBkgModesPlistEntry)) {
    console.log(emoji.ok, `Background Fetch capability already enabled. Info.plist keys are present.`)
  } else {
    console.log(emoji.running, `Editing Info.plist to add Background Fetch capability entry`)
    var plistEndTag = `</dict>\n</plist>`
    var uiBackgroundModesEntries = getBackgroundFetchInfoPlistKeys()
    infoPlistContents = infoPlistContents.replace(plistEndTag,
      `${uiBackgroundModesEntries}\n${plistEndTag}`)
  }

  // 5.3 Patch App Project file
  var bkgModesSystemCapability = `com.apple.BackgroundModes`
  if (~pbxprojFileContents.indexOf(bkgModesSystemCapability)) {
    console.log(emoji.ok, `Background Fetch capability already enabled. Project is already up-to-date.`)
  } else {
    console.log(emoji.running, `Editing Project.pbxproj file to enable Background Fetch capability`)
    var systemCapabilitiesMap = `SystemCapabilities = {`
    var backgroundModesSystemCap = getBackgroundModesCapability()
    pbxprojFileContents = pbxprojFileContents.replace(systemCapabilitiesMap,
      `${systemCapabilitiesMap}${backgroundModesSystemCap}`)
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

  // Background fetch functions
  function getBackgroundFetchPassthroughFunctionsCalls () {
    return `
- (void)application:(UIApplication *)application performFetchWithCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
  [RNNearIt application:application performFetchWithCompletionHandler:completionHandler];
}`
  }

  // Background Fetch Info.plist keys
  function getBackgroundFetchInfoPlistKeys () {
    return `  <key>UIBackgroundModes</key>
  <array>
    <string>fetch</string>
  </array>`
  }

  // Background Modes System Capability
  function getBackgroundModesCapability () {
    return `
              com.apple.BackgroundModes = {
                enabled = 1;
              };`
  }

  // Commit changes to files
  function writePatches () {
    fs.writeFileSync(appDelegatePath, appDelegateContents)
    fs.writeFileSync(infoPlistPath, infoPlistContents)
    fs.writeFileSync(projectPbxPath, pbxprojFileContents)
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
