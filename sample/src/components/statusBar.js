import React from 'react'
import { Platform, StatusBar, StyleSheet, View } from 'react-native'

const STATUSBAR_HEIGHT = Platform.OS === 'ios' ? 20 : 0

const MyStatusBar = ({ backgroundColor, ...props }) => (
  <View style={[styles.statusBar, { backgroundColor }]}>
    <StatusBar translucent backgroundColor={backgroundColor} {...props} />
  </View>
);

const styles = StyleSheet.create({
  statusBar: {
    height: STATUSBAR_HEIGHT
  }
})

export default MyStatusBar