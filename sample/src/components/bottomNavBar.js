import React from "react";
import { StyleSheet, View } from "react-native";

export default ({ backgroundColor, children }) => (
  <View style={styles.bottomNavigationBarContainer}>{children}</View>
);

const styles = StyleSheet.create({
  bottomNavigationBarContainer: {
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-around",
    borderTopWidth: 1,
    borderTopColor: "#00000055",
    backgroundColor: "#9F92FF"
  }
});
