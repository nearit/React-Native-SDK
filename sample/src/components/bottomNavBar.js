import React from 'react'
import { StyleSheet, View } from 'react-native'
import { ifIphoneX } from 'react-native-iphone-x-helper'

const BASE_HEIGHT = 56

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
    ...ifIphoneX({
      height: BASE_HEIGHT + 25,
      paddingBottom: 25
    }, {
      height: BASE_HEIGHT
    }),
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    borderTopWidth: 1,
    borderTopColor: '#00000055'
  }
})