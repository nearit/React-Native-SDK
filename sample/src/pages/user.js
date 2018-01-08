import React, { Component } from "react";
import { StyleSheet, Text, View } from "react-native";
import NearIT from "react-native-nearit";

import { Button } from "../components";
import recipeIcon from "../assets/icon-recipe.png";
import whoIcon from "../assets/who.png";
import couponNotificationIcon from "../assets/icona-couponsconto.png";

export default class User extends Component {
  constructor() {
    super();

    this.state = {
      profileId: ""
    };
  }

  componentWillMount() {
    this._getProfileId();
  }

  _getProfileId = async () => {
    try {
      const profileId = await NearIT.getUserProfileId();
      console.log("Got UserProfileID!");
      this.setState({
        profileId
      });
    } catch (err) {
      console.log("Could NOT get UserProfileID...", err);
    }
  };

  _setUserData = async () => {
    console.log("Setting UserData...");
    try {
      await NearIT.setUserData({ gender: "M", age: 25 });
      console.log("Successfully set UserData!");
      this.props.showMessage(`UserData set!`, whoIcon);
    } catch (err) {
      console.log("Could NOT set UserData...", err);
    }
  };

  _optOut = async () => {
    console.log("OptingOut this device...");
    try {
      await NearIT.optOut();
      this.props.showMessage(`OptOut successfull!`);
    } catch (err) {
      console.log("Error while opting-out", err);
      this.props.showMessage(`OptOut failed!`);
    }
  };

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.text}>{this.state.profileId}</Text>

        <Button
          label="Set UserData"
          accessibilityLabel="Set UserData"
          onPress={this._setUserData}
          style={styles.actionButton}
        />

        <Button
          label="OptOut"
          accessibilityLabel="OptOut device from NearIT"
          onPress={this._optOut}
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
  text: {
    color: "#FFF"
  },
  actionButton: {
    marginTop: 5,
    marginBottom: 5
  }
});
