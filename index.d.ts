// Type definitions for react-native-nearit 2.12.0
// Project: https://github.com/nearit/React-Native-SDK
// Definitions by: Mattia Panzeri <mattia.panzeri93@gmail.com>
// Changes by Federico Boschini <federico@nearit.com>

declare module 'react-native-nearit' {

  interface NearItPermissions {
    location: string,
    notifications: string,
    bluetooth: string,
    locationServices: string,
    always: string,
    whenInUse: string,
    denied: string
  }

  interface NearItEvents {
    SimpleNotification: string,
    Content: string,
    Feedback: string,
    Coupon: string,
    CustomJson: string
  }

  interface NearItEventContent {
    type: string,
    trackingInfo: string,
    message: string,
    content: string,
    fromUserAction: string,
    status: string,
    title: string,
    image: string,
    fullSize: string,
    squareSize: string,
    text: string,
    cta: string,
    label: string,
    url: string,
    description: string,
    value: string,
    expiresAt: string,
    redeemableFrom: string,
    serial: string,
    claimedAt: string,
    redeemedAt: string,
    question: string,
    feedbackId: string,
    read: string,
    timestamp: string,
    isNew: string,
    notificationContent: string
  }

  interface NearItStatuses {
    received: string,
    opened: string
  }

  interface NearItConstants {
    Events: NearItEvents,
    EventContent: NearItEventContent,
    Statuses: NearItStatuses,
    Permissions: NearItPermissions
  }

  interface NearItEvent {
    type: string
  }

  interface NearItImage {
    fullSize?: string,
    squareSize?: string
  }

  interface NearItCoupon {
    title: string,
    description?: string,
    image?: NearItImage,
    value: string,
    expiresAt?: string,
    redeemableFrom?: string,
    serial: string,
    claimedAt?: string,
    redeemedAt?: string
  }

  interface NearItHistoryItem {
    read: boolean,
    timestamp: string,
    isNew: boolean,
    notificationContent: any
  }

  type LocationPermissionStatus = NearItPermissions.always | NearItPermissions.denied | NearItPermissions.whenInUse

  interface NearItPermissionsResult {
    bluetooth: boolean,
    location: LocationPermissionStatus,
    locationServices: boolean,
    notifications: boolean
  }

  type NearItRating = 0 | 1 | 2 | 3 | 4 | 5

  interface EmitterSubscription {}

  class NearItManager {

    static constants: NearItConstants

    static onDeviceReady(): void

    static addContentsListener (listener: (event: NearItEvent) => void): EmitterSubscription

    static removeContentsListener (subscription: EmitterSubscription): void

    // Radar related methods

    static startRadar(): void

    static stopRadar(): void

    // Trackings related methods

    static sendTracking(trackingInfo: string, status: string): void
    
    // Feedback related methods

    static sendFeedback(feedbackId: string, rating: NearItRating, comment?: string): Promise<void>

    // ProfileId related methods
    static getProfileId(): Promise<string>

    static setProfileId(profileId: string): void

    static resetProfileId(): Promise<string>

    // User data related methods

    static setUserData(key: string, value?: string): void

    static setMultiChoiceUserData(key: string, userDataObject: { [key: string]: boolean }): void

    static getUserData(): Promise<any>

    // Opt-out related methods

    static optOut(): Promise<void>

    // In-app events related methods

    static triggerInAppEvent(eventKey: string): void

    // Coupon related methods
    
    static getCoupons(): Promise<NearItCoupon[]>

    static showCouponList(title?: string): void

    // Notification history related methods

    static getNotificationHistory(): Promise<NearItHistoryItem[]>

    static showNotificationHistory(title?: string): void

    static addNotificationHistoryUpdateListener (listener: (event: NearItHistoryItem[]) => void): EmitterSubscription

    static removeNotificationHistoryUpdateListener (subscription: EmitterSubscription): void

    static markNotificationHistoryAsOld(): void

    // Permissions related methods

    static requestPermissions(expanation?: string): Promise<NearItPermissionsResult>

    static isBluetoothEnabled(): Promise<boolean>

    static areLocationServicesOn(): Promise<boolean>

    static isLocationGranted(): Promise<boolean>

    static isNotificationGranted(): Promise<boolean>

    // Content related methods

    static showContent(event: NearItEvent): void

    static disableDefaultRangingNotifications(): void

    static addProximityListener (listener: (event: NearItEvent) => void): EmitterSubscription

    static removeProximityListener (subscription: EmitterSubscription): void

  }

  export = NearItManager
}
