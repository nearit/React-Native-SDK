import React from 'react'
import { Platform, StyleSheet, Text, View } from 'react-native'
import StatusBar from './statusBar'
import AppBar from './appBar'

export default ({ statusBarColor, appBarColor, children }) => (
  <View>
    <StatusBar
      backgroundColor={Platform.OS === 'ios' ? statusBarColor : '#00000000'}
      barStyle="light-content"
    />
    <AppBar
      backgroundColor={appBarColor}
    >
      {children}
    </AppBar>
  </View>
)