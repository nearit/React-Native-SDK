var fs = require('fs')
var path = require('path')
var emoji = require('../utils').emoji

module.exports = () => {
  console.log(emoji.robot, 'Running Android postlink script')

  // Gradle deps to inject
  var googleServicesGradleDep = `classpath 'com.google.gms:google-services:3.1.1'`
  var nearitRequiredCompileSdkVersion = `compileSdkVersion 26`
  var nearitRequiredBuildToolsVersion= `buildToolsVersion "26.0.2"`

  // Common build file paths
  var mainBuildGradlePath = path.join('android', 'build.gradle')
  var appBuildGradlePath = path.join('android', 'app', 'build.gradle')

  if (!fs.existsSync(mainBuildGradlePath) || !fs.existsSync(appBuildGradlePath)) {
    return Promise.reject(new Error(`Couldn't find 'build.gradle' files. You might need to update them manually. \
    Please refer to plugin manual installation section for Android at \
    https://nearit-react-native-sdk.readthedocs.io/en/latest/manual-installation-android/`))
  }
  
  var mainBuildGradleContents = fs.readFileSync(mainBuildGradlePath, 'utf8')

  // 1. Add google-services dependency definition
  var androidToolsGradleLink = mainBuildGradleContents.match(/classpath ["']com\.android\.tools\.build:gradle:.*["']/)[0]
  var gmsGradleLinkMatches = mainBuildGradleContents.match(/classpath ["']com\.google\.gms:google-services:.*["']/)
  if (gmsGradleLinkMatches && gmsGradleLinkMatches.length > 0) {
    if (~mainBuildGradleContents.indexOf(googleServicesGradleDep)) {
      console.log(emoji.ok, `"google-services" already added.`)
    } else {
      console.log(emoji.running, `Updating "google-services" inside build definition`)
      var gmsGradleLink = gmsGradleLinkMatches[0]
      mainBuildGradleContents = mainBuildGradleContents.replace(gmsGradleLink, googleServicesGradleDep)
      fs.writeFileSync(mainBuildGradlePath, mainBuildGradleContents)
    }
  } else {
    console.log(emoji.running, `Adding "google-services" to the build definition`)
    mainBuildGradleContents = mainBuildGradleContents.replace(androidToolsGradleLink,
            `${androidToolsGradleLink}\n        ${googleServicesGradleDep}`)
    fs.writeFileSync(mainBuildGradlePath, mainBuildGradleContents)
  }

  // 2. Add google maven repository
  var jcenterMavenRepository = mainBuildGradleContents.match(/mavenLocal\(\)\n {8}jcenter\(\)/)[0]
  var googleMavenRepository = `maven { url "https://maven.google.com" }`
  if (~mainBuildGradleContents.indexOf(googleMavenRepository)) {
    console.log(emoji.ok, `"google" repository already added.`)
  } else {
    console.log(emoji.running, `Adding "google" repository to the build definition`)
    mainBuildGradleContents = mainBuildGradleContents.replace(jcenterMavenRepository,
            `${jcenterMavenRepository}\n        ${googleMavenRepository}`)
    fs.writeFileSync(mainBuildGradlePath, mainBuildGradleContents)
  }

  var appBuildGradleContents = fs.readFileSync(appBuildGradlePath, 'utf8')

  // 3. Add google services plugin to app build definition
  var googleServicesPlugin = `apply plugin: 'com.google.gms.google-services'`
  if (~appBuildGradleContents.indexOf(googleServicesPlugin)) {
    console.log(emoji.ok, `"google-services" plugin already added.`)
  } else {
    console.log(emoji.running, `Adding "google-services" plugin to the app build definition`)
    appBuildGradleContents = `${appBuildGradleContents}\n\n${googleServicesPlugin}`
    fs.writeFileSync(appBuildGradlePath, appBuildGradleContents)
  }

  // 4. Update compile SDK and Tools versions
  var compileSdkVersion = appBuildGradleContents.match(/compileSdkVersion [0-9]{2}/)[0]
  if (compileSdkVersion === nearitRequiredCompileSdkVersion) {
    console.log(emoji.ok, `"compileSdkVersion" already updated.`)
  } else {
    console.log(emoji.running, `Updating "compileSdkVersion" in the app build definition`)
    appBuildGradleContents = appBuildGradleContents.replace(compileSdkVersion, nearitRequiredCompileSdkVersion)
    fs.writeFileSync(appBuildGradlePath, appBuildGradleContents)
  }

  var buildToolsVersion = appBuildGradleContents.match(/buildToolsVersion "[0-9]*\.[0-9]*\.[0-9]*"/)[0]
  if (buildToolsVersion === nearitRequiredBuildToolsVersion) {
    console.log(emoji.ok, `"buildToolsVersion" already updated.`)
  } else {
    console.log(emoji.running, `Updating "buildToolsVersion" in the app build definition`)
    appBuildGradleContents = appBuildGradleContents.replace(buildToolsVersion, nearitRequiredBuildToolsVersion)
    fs.writeFileSync(appBuildGradlePath, appBuildGradleContents)
  }

  return Promise.resolve()
}
