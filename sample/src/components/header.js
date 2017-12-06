import React from "react";
import { Platform, StatusBar, StyleSheet, Text, View } from "react-native";

export default ({ statusBarColor, appBarColor, children }) => (
  <View>
    <StatusBar backgroundColor="#9F92FF" barStyle="light-content" />

    <View style={styles.appBar}>
      <Text style={styles.headerText}>{children}</Text>
    </View>
  </View>
);

const styles = StyleSheet.create({
  appBar: {
    height: Platform.OS === "ios" ? 44 : 56,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#9F92FF",
    borderBottomWidth: 1,
    borderBottomColor: "#FFFFFF55"
  },
  headerText: {
    color: "#FFF",
    fontSize: 15,
    fontWeight: "bold"
  }
});
