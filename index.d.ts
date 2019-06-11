// Type definitions for react-native-nearit v1.0.0-alpha.1
// Project: https://github.com/nearit/React-Native-SDK
// Definitions by: Mattia Panzeri <mattia.panzeri93@gmail.com>

declare module 'react-native-nearit' {

    interface NearItConstants {
      Events: NearItEvents
      EventContent: NearItEventContent
      Statuses: NearItStatuses
    }

    interface NearItEvents {
      SimpleNotification: string
      Content: string
      Feedback: string
      Coupon: string
      CustomJson: string
    }

    interface NearItEventContent {
      type: string
      content: string
      fromUserAction: string
      trackingInfo: string
    }

    interface NearItStatuses {
      received: string
      opened: string
    }

    interface NearItEvent {
      type: string,
      content?: Map<string, any>,
      fromUserAction?: boolean,
      trackingInfo?: string,
      status?: string
    }

    interface NearItImage {
      fullSize?: string,
      squareSize?: string
    }

    interface NearItCoupon {
      title: string,
      description: string,
      image?: NearItImage,
      value: string,
      expiresAt: string,
      redeemableFrom: string,
      serial?: string,
      claimedAt?: string,
      redeemedAt?: string
    }

    type NearItRating = 0 | 1 | 2 | 3 | 4 | 5

    interface EmitterSubscription {}

    class NearItManager {

      static constants: NearItConstants

      static addContentsListener (listener: (event: NearItEvent) => void): EmitterSubscription

      static removeContentsListener (subscription: EmitterSubscription): void

      // Radar related methods

      static startRadar()

      static stopRadar()

      // Trackings related methods

      static sendTracking(trackingInfo: string, status: string)
      
      // Feedback related methods

      static sendFeedback(feedbackId: string, rating: NearItRating, comment?: string): Promise<void>

      // ProfileId related methods
      static getProfileId(): Promise<string>

      static setProfileId(profileId: string)

      static resetProfileId(): Promise<string>

      // User data related methods

      static setUserData(key: string, value?: string)

      static setMultiChoiceUserData(key: string, userDataObject: { [key: string]: boolean })

      static getUserData(): Promise<any>

      // Opt-out related methods

      static optOut(): Promise<void>

      // In-app events related methods

      static triggerEvent(eventKey: string)

      // Coupon related methods
      
      static getCoupons(): Promise<NearItCoupon[]>

      static showCouponList(): Promise<null>

      // Notification history related methods

      // TODO: return type
      static getNotificationHistory(): Promise<null>

      static showNotificationHistory(): Promise<null>

      static setNotificationHistoryUpdateListener (listener: (event: any) => void): EmitterSubscription

      static markNotificatinHistoryAsOld(): Promise<null>

      // Permissions related methods

      static requestPermissions(expanation?: string): Promise<null>

      static isBluetoothEnabled(): Promise<boolean>

      static areLocationServicesOn(): Promise<boolean>

      static isLocationGranted(): Promise<boolean>

      static isNotificationGranted(): Promise<boolean>

      // Content related methods

      // TODO: param type
      static showContent(content: any): Promise<null>

    }

    export = NearItManager
  }
