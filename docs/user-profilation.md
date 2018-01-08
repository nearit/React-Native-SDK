# User Profiling & Opt-out

NearIT creates an anonymous profile for every user of your app. You can choose to add data to user profile. This data will be available inside recipes to allow the creation of user targets.

## Send User-Data to NearIT

We automatically create an anonymous profile for every installation of the app. You can check that a profile was created by checking the existance of a profile ID.
```js
const profileId = await NearIT.getUserProfileId();
```

If the result is null, it means that no profile is associated with the app installation (probably due to a network error). The SDK will re-try to create a profile at every start, and every time a new user data is set.

After the profile is created, to set user data call:
```js
NearIT.setUserData({
    'name': 'Jon',
    'surname': 'Snow'
})
```

**N.B:** If you try to set user data before creating a profile an error will be raised.

## Reset Profile
If you want to reset your user profile use this method:
```js
const newProfileId = await NearIT.resetUserProfile()
```

## Link NearIT profiles with an external User Database

You might want to link users in your CRM database with NearIT profiles. You can do it by storing the NearIT profileID in your CRM database. This way, you can link our analytics with your own user base and associate all the devices of an user to the same NearIT profile.


Furthermore, if you detect that your user already has a NearIT profileID in your CRM database, you can manually set it on a local app installation with the method:
```js
NearIT.setUserProfileId(profileId);
```
You can then set the relevant user-data to this profile with the aforementioned methods.

**N.B:** Please keep in mind that you will be responsible of storing our profile identifier in your system.

## Opt-out

You can **opt-out** a profile and its devices:
```js
NearIT.optOut();
```
If the opt-out call is successful all the **user-data** and **trackings** will be deleted and the **SDK will cease to work** (the user's devices will not receive further notifications).