# Handle In-app Content

NearIT takes care of delivering content at the right time, you will just need to handle content presentation. 


## Listen for Events

NearIT contents are delivered through React Native events, to listen to them just add a new `ContentsListener` to `NearIT`
```js
this.eventsSubscription = NearIT.addContentsListener(event => {
    // Your events handling code here
})
```
**N.B:** Remember to unsubscribe from events (you'd want to do this when the component is ***unmounted***)
```js
NearIT.removeContentsListener(this.eventsSubscription)
```

<br>

## NearIT Content events

**N.B:** To access Events content we recommend to use the `NearITConstants` available for import from `react-native-nearit`
```js
import { NearItConstants } from 'react-native-nearit'

const { Events, EventContent } = NearItConstants
```

<br>

Each event object is composed as follow

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
| `Events.Content` | A NearIT notification with Content |
| `Events.Feedback` | A NearIT notification with Feedback |
| `Events.Coupon` | A NearIT notification with Coupon |
| `Events.CustomJson` | A NearIT Custom JSON |

#### Events content

- For `Events.SimpleNotification`

| Field    | Description |
|----------|-------------|
| `EventContent.message` | The `body` of the NearIT simple notification |

- For `Events.Content`

| Field    | Description |
|----------|-------------|
| `EventContent.message` | The `body` of the NearIT simple notification |
| `EventContent.text`    | The `text` content (can be empty) |
| `EventContent.images`  | The array of `images` sent within the content (can be empty) |
| `EventContent.video`   | The url to the `video` content (can be empty) |
| `EventContent.upload`  | The url to the `pdf` content (can be empty) |
| `EventContent.audio`   | The url to the `audio` content (can be empty) |

- For `Events.Feedback`

| Field    | Description |
|----------|-------------|
| `EventContent.message` | The `body` of the NearIT simple notification |
| `EventContent.feedbackId` | The `feedbackId` required to send a Feedback answer |
| `EventContent.question` | The `question` to be displayed to the user  |

See below to learn how to send a `Feedback` answer to NearIT.

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

# Feedbacks

NearIT allow you to send questions and get feedback from your users.

NearIT Feedback are composed of 2 parts:

- A `rating`, represented as an integer from 0 to 5
- An optional `comment`, to allow your user to comment on a rating

After displaying the Feedback request to your user and receiving his answer, you should send this data to NearIT using the below method:
```js
import NearIT from 'react-native-nearit'

...
const rating = 5 // The rating index (0 to 5)
const comment = '' // The optional comment for the rating

const feedbackSent = await NearIT.sendFeedback(feedbackId, rating, comment) // Full method call
// or
const  feedbackSent = await NearIT.sendFeedback(feedbackId, rating) // Method call without comment string
```

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