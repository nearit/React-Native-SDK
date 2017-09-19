# Handle In-app Content

NearIT takes care of delivering content at the right time, you will just need to handle content presentation. 


## Listen for Events

NearIT contents are delivered through React Native events, to listen to them just add a new `ContentsListener` to `NearIT`
```js
this.eventsSubscription = NearIT.addContentsListener(event => {
    // Your events handling code here
})
```
**N.B:** As stated in the official `React Native` documentation, remember to unsubscribe from events (you'd want to do this when the component is unmounted)
```js
this.eventsSubscription.remove()
```

<br>

## NearIT Content events

**N.B:** To access Events content we recommend to use the `NearITConstants` available for import from `react-native-nearit`
```js
import { NearItConstants } from 'react-native-nearit'

const { Events, EventContent, Permissions } = NearItConstants
```

<br>

Each event object (except for `PermissionStatus` ones) is composed as follow

| Field      | Description |
|------------|-------------|
| `EventContent.type` | A string event type (see below table) |
| `EventContent.content` | An object that contains an event payload (see below table for specific object structure) |
| `EventContent.fromUserAction` | A boolean indicating wheter the notification comes from an user actions (such as a tap on a background notification) |
| `EventContent.trackingInfo` | A string to use when sending trackings back to NearIT |

#### Event types

| Value     | Description |
|-----------|-------------|
| `Events.SimpleNotification` | A NearIT simple notification |
| `Events.Coupon` | A NearIT notification with Coupon |
| `Events.CustomJson` | A NearIT Custom JSON |
| `Events.PermissionStatus` | A message indicating the state (`Granted` or `Denied`) of a permission |

#### Events content

- For `Events.SimpleNotification`

| Field    | Description |
|----------|-------------|
| `EventContent.message` | The `body` of the NearIT simple notification |

- For `Events.Coupon`

| Field    | Description |
|----------|-------------|
| `EventContent.message` | The `body` of the NearIT simple notification |
| `EventContent.coupon` | The `coupon` object sent with the notification |

- For `Events.CustomJson`

| Field    | Description |
|----------|-------------|
| `EventContent.message` | The `body` of the NearIT simple notification |
| `EventContent.data` | The `json` of the NearIT custom JSON notification |

<br>

## Permission Status events

```js
import { NearItConstants } from 'react-native-nearit'

const { Permissions } = NearItConstants
```

<br>

A `PermissionStatus` event is composed as follow

| Field      | Description |
|------------|-------------|
| `EventContent.type` | A string that equals `Events.PermissionStatus` |
| `EventContent.status` | A string which represent a permission status (see below table for specific values) |

#### Permission Statuses

| Field      | Description |
|------------|-------------|
| `Permissions.LocationGranted` | This status indicates that the user has GRANTED the app the permission to access his location |
| `Permissions.LocationDenied` | This status indicates that the user has DENIED the app the permission to access his location |

<br>

# Trackings

NearIT analytics on recipes are built from trackings describing the status of user engagement with a recipe.

The two recipe states are **"Notified"** and **"Engaged"** to represent a recipe delivered to the user and a recipe that the user responded to.

**N.B:** Built-in background recipes (displayed when the app is not foreground) track themselves as notified and engaged.

Foreground recipes don't have automatic tracking. You need to track both the "Notified" and the "Engaged" statuses when it's the best appropriate for you scenario.

```js
import NearIT, { NearItConstants } from 'react-native-nearit'
const { Statuses } = NearItConstants

...

NearIT.sendTracking(trackingInfo, Statuses.notified); // Track recipe as 'Notified'

NearIT.sendTracking(trackingInfo, Statuses.engaged); // Track recipe as 'Engaged'

NearIT.sendTracking(trackingInfo, 'custom-event'); // Track a custom event to the recipe
```

**N.B:** The recipe cooldown feature uses tracking calls to hook its functionality, so failing to properly track user interactions will result in the cooldown not being applied.

<br>

# Fetch current user coupon

We handle the complete emission and redemption coupon cycle in our platform, and we deliver a coupon content only when a coupon is emitted (you will not be notified of recipes when a profile has already received the coupon, even if the coupon is still valid). 

You can ask the library to fetch the list of all the user current coupons with the method:

```js
import NearIT from 'react-native-nearit'

...

const coupons = await NearIT.getCoupons() // Will return an array of coupon objects
```

The method will also return already redeemed coupons so you get to decide to filter them if necessary.