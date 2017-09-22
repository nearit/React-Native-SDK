import React from 'react'
import { Platform, StatusBar, StyleSheet, View } from 'react-native'
import { ifIphoneX } from 'react-native-iphone-x-helper'

const APPBAR_HEIGHT = Platform.OS === 'ios' ? 44 : 56;

const AppBar = ({ backgroundColor, children, ...props }) => (
  <View style={[styles.appBar, { backgroundColor }]}>
    {children}
  </View>
);

const styles = StyleSheet.create({
  appBar: {
    ...ifIphoneX({
        height: APPBAR_HEIGHT + 20,
        paddingTop: 20
    }, {
        height: APPBAR_HEIGHT
    }),
    justifyContent: 'center',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: '#FFFFFF55'
  }
})

export default AppBar