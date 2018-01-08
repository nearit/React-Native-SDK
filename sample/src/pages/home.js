import React, { Component } from "react";
import { StyleSheet, Text, View } from "react-native";
import NearIT from "react-native-nearit";

import { Button } from "../components";
import recipeIcon from "../assets/icon-recipe.png";
import couponNotificationIcon from "../assets/icona-couponsconto.png";

export default class Home extends Component {
  _onPressRequestNotificationPermissions = async () => {
    let permissionGranted = await NearIT.checkNotificationPermission();

    if (permissionGranted == null) {
      permissionGranted = await NearIT.requestNotificationPermission();
    }

    this.props.showMessage(
      permissionGranted
        ? "Notification Permission GRANTED"
        : "Notification Permission NOT GRANTED"
    );
  };

  _onPressRequestLocationPermissions = async () => {
    let locationGranted = await NearIT.checkLocationPermission();

    if (locationGranted == null) {
      locationGranted = await NearIT.requestLocationPermission();
    }

    this.props.showMessage(
      locationGranted
        ? "Location Permission GRANTED"
        : "Location Permission NOT GRANTED"
    );
  };

  _getCoupons = async () => {
    console.log("Get Coupons");
    try {
      const coupons = await NearIT.getCoupons();

      this.props.showMessage(
        `You have ${coupons.length} coupon${coupons.length > 1 ? "s" : ""}!`,
        couponNotificationIcon
      );
    } catch (err) {
      console.log("Error while retrieving coupons", err);
    }
  };

  _customTrigger = async () => {
    console.log("Launching Custom Trigger event");
    try {
      await NearIT.triggerEvent("button_pressed");
    } catch (err) {
      console.log("Error while launching custom event", err);
      this.props.showMessage(`Custom Trigger launch failed!`);
    }
  };

  _onPressRefreshCfg = async () => {
    console.log("Refreshing NearIT Config...");
    try {
      await NearIT.refreshConfig();
      console.log("NearIT Configuration refreshed!");
      this.props.showMessage("NearIT Configuration refreshed!", recipeIcon);
    } catch (err) {
      console.log("Error while refreshing configs", err);
    }
  };

  render() {
    return (
      <View style={styles.container}>
        <Button
          label="Notification Permission"
          accessibilityLabel="Open NearIT Permission request for Notifications"
          onPress={this._onPressRequestNotificationPermissions}
          style={styles.actionButton}
        />

        <Button
          label="Location Permission"
          accessibilityLabel="Open NearIT Permission request for Location"
          onPress={this._onPressRequestLocationPermissions}
          style={styles.actionButton}
        />

        <Button
          label="Refresh Configuration"
          accessibilityLabel="Refresh NearIT Configuration"
          onPress={this._onPressRefreshCfg}
          style={styles.actionButton}
        />

        <Button
          label="Get Coupons"
          accessibilityLabel="Get NearIT Coupons"
          onPress={this._getCoupons}
          style={styles.actionButton}
        />

        <Button
          label="Custom Trigger"
          accessibilityLabel="Launch Custom Trigger"
          onPress={this._customTrigger}
          style={styles.actionButton}
        />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center"
  },
  actionButton: {
    marginTop: 5,
    marginBottom: 5
  }
});
