# Location Based Notifications


When you want to start the radar for **geofences and beacons** call this method:

```js
// call this when you are given the proper permission for scanning
NearIT.startRadar()
```

## Permissions

To use `Location based Notifications` you first need to request `Location` permission by calling this method
```js
NearIT.requestLocationPermission()
```
This call will return a value of:

- `TRUE`, in case the permission has been granted
- `FALSE`, in case the permission has been denied

**N.B:** When the permission is granted for the first time `NearIT.startRadar` method is automatically invoked.

<br>

## Listen for Events

After starting the radar, events are delivered through events, to listen to them just add a new `ContentsListener` to `NearIT`
```js
this.eventsSubscription = NearIT.addContentsListener(event => {
    // Your events handling code here
})
```
**N.B:** Remember to unsubscribe from events (you'd want to do this when the component is ***unmounted***)
```js
NearIT.removeContentsListener(this.eventsSubscription)
```