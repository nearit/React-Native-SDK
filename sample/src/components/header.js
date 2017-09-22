import React from 'react'
import { StyleSheet, Text, View } from 'react-native'
import StatusBar from './statusBar'
import AppBar from './appBar'

export default ({ statusBarColor, appBarColor, children }) => (
  <View>
    <StatusBar
      backgroundColor={statusBarColor}
      barStyle="light-content"
    />
    <AppBar
      backgroundColor={appBarColor}
    >
      {children}
    </AppBar>
  </View>
)