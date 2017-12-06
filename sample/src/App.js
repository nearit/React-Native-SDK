import React from "react";
import { SafeAreaView, StyleSheet, Text, View } from "react-native";
import NearIT, { NearItConstants } from "react-native-nearit";

import { BottomNavBar, BottomNavBarItem, Dialog, Header } from "./components";
import { Home, User } from "./pages";

import homeIcon from "./assets/icon-nearit.png";
import userIcon from "./assets/who.png";
import alertsIcon from "./assets/icon-alert.png";
import simpleNotificationIcon from "./assets/icona-notifica.png";
import contentNotificationIcon from "./assets/icona-notificaecontenuto.png";
import couponNotificationIcon from "./assets/icona-couponsconto.png";
import feedbackNotificationIcon from "./assets/icona-questionario.png";
import customJsonNotificationIcon from "./assets/icon-code.png";

const { Events, EventContent, Permissions, Statuses } = NearItConstants;

class App extends React.Component {
  constructor() {
    super();

    this.state = {
      isOpened: false,
      messageIcon: alertsIcon,
      messageText: "RNBanner",
      pageIndex: 0
    };

    this._startNearIt();
  }

  componentWillMount() {
    if (!this._nearItSubscription) {
      this._nearItSubscription = NearIT.addContentsListener(event => {
        console.log("Received a new event from NearIT", { event });
        const evtType = event[EventContent.type];
        if (evtType !== Events.PermissionStatus) {
          const evtContent = event[EventContent.content];
          const evtTracking = event[EventContent.trackingInfo];
          switch (evtType) {
            case Events.SimpleNotification:
              this._showMessage(
                evtContent[EventContent.message],
                simpleNotificationIcon
              );
              break;

            case Events.Content:
              this._showMessage(
                evtContent[EventContent.message],
                contentNotificationIcon
              );
              break;

            case Events.Feedback:
              this._showMessage(
                evtContent[EventContent.message],
                feedbackNotificationIcon
              );
              break;

            case Events.Coupon:
              this._showMessage(
                evtContent[EventContent.message],
                couponNotificationIcon
              );
              break;

            case Events.CustomJson:
              this._showMessage(
                evtContent[EventContent.message],
                customJsonNotificationIcon
              );
              break;

            default:
              this._showMessage("Received a NearIT event", alertsIcon);
          }

          NearIT.sendTracking(evtTracking, Statuses.notified);
        }
      });
    }
  }

  componentWillUnmount() {
    if (this._closeTimer) {
      clearTimeout(this._closeTimer);
    }

    if (this._nearItSubscription) {
      NearIT.removeContentsListener(this._nearItSubscription);
    }
  }

  _startNearIt = async () => {
    try {
      await NearIT.startRadar();
      console.log("NearIT Started!");
    } catch (err) {
      console.log("Could NOT start NearIT...");
    }
  };

  _showMessage = (messageText, messageIcon = alertsIcon) => {
    this.setState({
      messageIcon,
      messageText,
      isOpened: true
    });
  };

  _hideMessage = () => {
    this.setState({
      isOpened: false
    });
  };

  _onPageSelected = pageIndex => {
    this.setState({
      pageIndex
    });
  };

  render() {
    const { isOpened, messageIcon, messageText, pageIndex } = this.state;

    return (
      <SafeAreaView style={styles.container}>
        <Header>NearIT Sample</Header>

        <Dialog
          isOpened={isOpened}
          icon={messageIcon}
          onPress={this._hideMessage}
        >
          {messageText}
        </Dialog>

        <View style={styles.body}>
          {pageIndex === 0 && <Home showMessage={this._showMessage} />}
          {pageIndex === 1 && <User showMessage={this._showMessage} />}
        </View>

        <BottomNavBar>
          {["Home", "User"].map((v, i) => (
            <BottomNavBarItem
              key={i}
              icon={i === 0 ? homeIcon : userIcon}
              text={v}
              selected={pageIndex === i}
              onPress={() => this._onPageSelected(i)}
            />
          ))}
        </BottomNavBar>
      </SafeAreaView>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#9F92FF"
  },
  body: {
    flex: 1,
    flexDirection: "column",
    backgroundColor: "#A69FFF"
  }
});

export default App;
