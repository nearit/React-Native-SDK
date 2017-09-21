# Location Based Notifications


When you want to start the radar for **geofences and beacons** call this method:

```js
// call this when you are given the proper permission for scanning
NearIT.startRadar()
```

## Permissions

To use `Location based Notifications` you first need to request `Location` permission by calling this method
```js
NearIT.requestLocationPermission()
```
This call will return a value of:

- `TRUE`, in case the permission has been granted
- `FALSE`, in case the permission has been denied 
- `null`, for the first request to the user, in this case you should listen for a `PERMISSION` event of either `NearItConstants.Permissions.LocationGranted` or `NearItConstants.Permissions.LocationDenied`

```js
import NearIT, { NearItConstants } from 'react-native-nearit'
...
async function askPermissions() {
    this.locationSubscription = NearIT.setContentsListener(event => {
        if (event[NearItConstants.EventContent.type] === NearItConstants.Events.PermissionStatus){
            if (event[NearItConstants.EventContent.status] === NearItConstants.Permissions.LocationGranted) {
                this.locationGranted = true
            } else if (event[NearItConstants.EventContent.status] === NearItConstants.Permissions.LocationDenied) {
                this.locationGranted = false
            }

            this.locationSubscription.remove()
        }
    })
    
    this.locationGranted = await NearIT.requestLocationPermission()
    if (typeof(this.locationGranted) !== 'undefined') {
        this.locationSubscription.remove()
    }
}
...

```
**N.B:** When the permission is granted for the first time `NearIT.startRadar` method is automatically invoked.

<br>

## Listen for Events

After starting the radar, events are delivered through events, to listen to them just add a new `ContentsListener` to `NearIT`
```js
this.eventsSubscription = NearIT.setContentsListener(event => {
    // Your events handling code here
})
```
**N.B:** As stated in the official `React Native` documentation, remember to unsubscribe from events (you'd want to do this when the component is unmounted)
```js
this.eventsSubscription.remove()
```