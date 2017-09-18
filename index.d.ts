// Type definitions for react-native-nearit v1.0.0-alpha.1
// Project: https://github.com/nearit/React-Native-SDK
// Definitions by: Mattia Panzeri <mattia.panzeri93@gmail.com>

declare module 'react-native-nearit' {

    interface NearItConstants {
      Events: Map<string, string>
      EventContent: Map<string, string>
      Statuses: Map<string, string>
      Permissions: Map<string, string>
    }

    interface NearItEvent {
      type: string,
      content?: Map<string, any>,
      fromUserAction?: boolean,
      trackingInfo?: string,
      status?: string
    }

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

      static getUserProfileId(): Promise<string>

      static setUserProfileId(profileId: string): Promise<string>

      static resetUserProfile(): Promise<void>

      static setUserData(userDataObject: Map<string, any>): Promise<void>

      static requestNotificationPermission(): Promise<boolean>

      static requestLocationPermission(): Promise<boolean | void>

    }

    export = NearItManager
  }
