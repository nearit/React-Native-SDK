// Type definitions for react-native-nearit v1.0.0-alpha.1
// Project: https://github.com/nearit/React-Native-SDK
// Definitions by: Mattia Panzeri <mattia.panzeri93@gmail.com>

declare module 'react-native-nearit' {

    interface NearItConstants {
      Events: NearItEvents
      EventContent: NearItEventContent
      Statuses: NearItStatuses
      Permissions: NearItPermissions
    }

    interface NearItEvents {
      SimpleNotification: string
      Content: string
      Feedback: string
      Coupon: string
      CustomJson: string
      PermissionStatus: string
    }

    interface NearItEventContent {
      type: string
      content: string
      fromUserAction: string
      trackingInfo: string
    }

    interface NearItStatuses {
      notified: string
      engaged: string
    }

    interface NearItPermissions {
      LocationGranted: string
      LocationDenied: string
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
      name: string,
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

    interface EmitterSubscription {
      remove(): void
    }

    class NearItManager {

      static constants: NearItConstants

      static setContentsListener (listener: (event: NearItEvent) => void): EmitterSubscription

      static refreshConfig(): Promise<void>

      static startRadar(): Promise<void>

      static stopRadar(): Promise<void>

      static sendTracking(trackingInfo: string, status: string): Promise<void>
      
      static sendFeedback(feedbackId: string, rating: NearItRating, comment: string = ''): Promise<void>

      static getUserProfileId(): Promise<string>

      static setUserProfileId(profileId: string): Promise<string>

      static resetUserProfile(): Promise<void>

      static setUserData(userDataObject: Map<string, any>): Promise<void>

      static requestNotificationPermission(): Promise<boolean>

      static requestLocationPermission(): Promise<boolean | void>

      static getCoupons(): Promise<NearItCoupon[]>

    }

    export = NearItManager
  }
