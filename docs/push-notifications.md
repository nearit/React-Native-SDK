# Push Notifications

## Setup

To enable push notification, you first need to follow the integration instruction for your target operating system:

- [iOS](push-notifications-ios.md)
- [Android](push-notifications-android.md)

<br>

## Notification Permission

### Check
To check `Notification` permission call this method from ReactJS
```js
NearIT.checkNotificationPermission()
```

This call will return a value of:

- `null`, in case the permission request has not been done yet
- `TRUE`, in case the permission has been granted
- `FALSE`, in case the permission has been denied

### Request
To request `Notification` permission call this method from ReactJS
```js
NearIT.requestNotificationPermission()
```

This call will return a value of:

- `TRUE`, in case the permission has been granted
- `FALSE`, in case the permission has been denied

<br>

## Listen for Events
**N.B:** If you have already enabled `Location Based Notifications` you can use the same `ContentsListener`, there's no need to register a second one to handle different event types.

Events are delivered through events, to listen to them just add a new `ContentsListener` to `NearIT`
```js
this.eventsSubscription = NearIT.setContentsListener(event => {
    // Your events handling code here
})
```

**N.B:** As stated in the official `React Native` documentation, remember to unsubscribe from events (you'd want to do this when the component is unmounted)
```js
this.eventsSubscription.remove()
```