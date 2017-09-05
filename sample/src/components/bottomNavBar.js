import React from 'react'
import { StyleSheet, View } from 'react-native'

export default ({ backgroundColor, children }) => (
  <View style={[
    styles.bottomNavigationBarContainer,
    { backgroundColor }
  ]}
  >
    {children}
  </View>
)

const styles = StyleSheet.create({
  bottomNavigationBarContainer: {
    height: 56,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around'
  }
})