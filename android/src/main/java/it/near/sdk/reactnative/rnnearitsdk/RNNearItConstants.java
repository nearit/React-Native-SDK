package it.near.sdk.reactnative.rnnearitsdk;

/**
 * @author Federico Boschini
 */
class RNNearItConstants {

    // Event topic (used by JS to subscribe to generated events)
    static final String NATIVE_EVENTS_TOPIC = "RNNearItEvent";
    static final String NATIVE_NOTIFICATION_HISTORY_TOPIC = "RNNearItNotificationHistory";

    // Module Constants
    // Events type
    static final String EVENT_TYPE_SIMPLE = "NearIt.Events.SimpleNotification";
    static final String EVENT_TYPE_CUSTOM_JSON = "NearIt.Events.CustomJSON";
    static final String EVENT_TYPE_COUPON = "NearIt.Events.Coupon";
    static final String EVENT_TYPE_CONTENT = "NearIt.Events.Content";
    static final String EVENT_TYPE_FEEDBACK = "NearIt.Events.Feedback";

    // Events content
    static final String EVENT_TYPE = "type";
    static final String EVENT_TRACKING_INFO = "trackingInfo";
    static final String EVENT_CONTENT_MESSAGE = "message";

    static final String EVENT_CONTENT = "content";

    static final String EVENT_FROM_USER_ACTION = "fromUserAction";
    static final String EVENT_STATUS = "status";

    // Common
    static final String EVENT_CONTENT_TITLE = "title";
    // Image
    static final String EVENT_IMAGE = "image";
    static final String EVENT_IMAGE_FULL_SIZE = "fullSize";
    static final String EVENT_IMAGE_SQUARE_SIZE = "squareSize";

    // Content
    static final String EVENT_CONTENT_TEXT = "text";
    static final String EVENT_CONTENT_CTA = "cta";
    static final String EVENT_CONTENT_CTA_LABEL = "label";
    static final String EVENT_CONTENT_CTA_URL = "url";

    // Coupon
    static final String EVENT_COUPON_DESCRIPTION = "description";
    static final String EVENT_COUPON_VALUE = "value";
    static final String EVENT_COUPON_EXPIRES_AT = "expiresAt";
    static final String EVENT_COUPON_REDEEMABLE_FROM = "redeemableFrom";
    static final String EVENT_COUPON_SERIAL = "serial";
    static final String EVENT_COUPON_CLAIMED_AT = "claimedAt";
    static final String EVENT_COUPON_REDEEMED_AT = "redeemedAt";

    // Feedback
    static final String EVENT_FEEDBACK_QUESTION = "feedbackQuestion";
    static final String EVENT_FEEDBACK_ID = "feedbackId";

    // Notification History
    static final String NOTIFICATION_HISTORY_READ = "read";
    static final String NOTIFICATION_HISTORY_TIMESTAMP = "timestamp";
    static final String NOTIFICATION_HISTORY_IS_NEW = "isNew";
    static final String NOTIFICATION_HISTORY_CONTENT = "notificationContent";

    // Permissions
    static final String PERMISSIONS_LOCATION_PERMISSION = "location";
    static final String PERMISSIONS_NOTIFICATIONS_PERMISSION = "notifications";
    static final String PERMISSIONS_BLUETOOTH = "bluetooth";
    static final String PERMISSIONS_LOCATION_SERVICES = "locationServices";
    static final String PERMISSIONS_LOCATION_ALWAYS = "always";
    static final String PERMISSIONS_LOCATION_DENIED = "denied";
    static final String PERMISSIONS_LOCATION_WHEN_IN_USE = "whenInUse";

    // Error codes
    static final String E_SEND_FEEDBACK_ERROR = "E_SEND_FEEDBACK_ERROR";
    static final String E_PROFILE_ID_GET_ERROR = "E_PROFILE_ID_GET_ERROR";
    static final String E_PROFILE_ID_RESET_ERROR = "E_PROFILE_ID_RESET_ERROR";
    static final String E_PROFILE_GET_USER_DATA_ERROR = "E_PROFILE_GET_USER_DATA_ERROR";
    static final String E_COUPONS_PARSING_ERROR = "E_COUPONS_PARSING_ERROR";
    static final String E_COUPONS_RETRIEVAL_ERROR = "E_COUPONS_RETRIEVAL_ERROR";
    static final String E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR = "E_NOTIFICATION_HISTORY_RETRIEVAL_ERROR";
    static final String E_OPT_OUT_ERROR = "E_OPT_OUT_ERROR";

}
