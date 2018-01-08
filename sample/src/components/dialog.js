import React from "react";
import {
  Image,
  Modal,
  StyleSheet,
  Text,
  TouchableWithoutFeedback,
  View
} from "react-native";

export default ({ children, isOpened, icon, onPress }) => (
  <Modal visible={isOpened} transparent={true}>
    <TouchableWithoutFeedback onPress={onPress ? onPress : null}>
      <View style={styles.container}>
        <View style={styles.dialog}>
          {icon && <Image source={icon} style={styles.icon} />}
          <Text style={styles.text}>{children}</Text>
        </View>
      </View>
    </TouchableWithoutFeedback>
  </Modal>
);

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "#00000056"
  },
  dialog: {
    backgroundColor: "#333",
    borderRadius: 10,
    padding: 20,
    alignItems: "center"
  },
  icon: {
    width: 35,
    height: 30,
    resizeMode: "contain",
    marginBottom: 16
  },
  text: {
    color: "#FFF",
    fontWeight: "bold"
  }
});
