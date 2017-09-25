import React from 'react'
import { Platform, StatusBar, StyleSheet, View } from 'react-native'
import { isIphoneX } from 'react-native-iphone-x-helper'

const APPBAR_HEIGHT = Platform.OS === 'ios' ? 44 : 56
const STATUS_BAR_HEIGHT = Platform.OS === 'ios' ? (isIphoneX() ? 20 : 0) : StatusBar.currentHeight

const AppBar = ({ backgroundColor, children, ...props }) => (
  <View style={[styles.appBar, { backgroundColor }]}>
    {children}
  </View>
);

const styles = StyleSheet.create({
  appBar: {
    height: APPBAR_HEIGHT + STATUS_BAR_HEIGHT,
    paddingTop: STATUS_BAR_HEIGHT,
    justifyContent: 'center',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: '#FFFFFF55'
  }
})

export default AppBar