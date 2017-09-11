# Push Notifications

To enable push notification you will need to integrate **Google Firebase** in your app:

**1.** If you don't already have a **Firebase project**, create one at [Google Firebase Console](https://console.firebase.google.com/).<br>
Inside the project, select **"Add Firebase to your Android app"** (make sure to enter the right package name of your app).

**2.** Download `google-services.json` file to your computer and
copy it in your project `android/app` folder.
![google-services.json](push_help/google_services_json.png "")

**3.** Copy your project ***FCM Cloud Messaging Server Key*** from [Google Firebase Console](https://console.firebase.google.com/)<br>
(See the screenshot below and make sure to use the right api key)
![fcmkey](push_help/fcmkeylocation.png "")

**4.** Open [NearIT](https://go.nearit.com), select your app and navigate to **“Settings > Push Settings”**.
Paste your project FCM Key under the **“Setup Android push notifications”** block.
![nearitsettings](push_help/fcm_upload.gif "")
<br><br>
___
**WARNING**: Do not follow any further FCM-specific instructions: we automatically handle all the other part of the process inside the SDK code.
___