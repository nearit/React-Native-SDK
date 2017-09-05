import React from 'react'
import { Platform, StatusBar, StyleSheet, View } from 'react-native'

const APPBAR_HEIGHT = Platform.OS === 'ios' ? 44 : 56;

const AppBar = ({ backgroundColor, children, ...props }) => (
  <View style={[styles.appBar, { backgroundColor }]}>
    {children}
  </View>
);

const styles = StyleSheet.create({
  appBar: {
    height: APPBAR_HEIGHT,
    justifyContent: 'center',
    alignItems: 'center'
  }
})

export default AppBar