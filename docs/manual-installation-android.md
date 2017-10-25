## Manual Installation - Android

**Read this section ONLY in the case the postlink script fails, or to check that everything worked as expected**

### Dependency setup

In your project `android/build.gradle` make sure to include the following:
```java
buildscript {
    ...
    dependencies {
        ...
        classpath 'com.google.gms:google-services:3.1.0'
    }
}

allprojects {
    repositories {
        ...
        maven { url "https://maven.google.com" }
    }
}
```


In your project `android/app/build.gradle` make sure to include the following:
```java
android {
    compileSdkVersion 26
    buildToolsVersion "26.0.2"
    ...
}

...

apply plugin: 'com.google.gms.google-services' // Include at the end of file
```