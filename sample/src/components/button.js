import React from 'react'
import {
  Animated,
  Image,
  StyleSheet,
  Text,
  TouchableWithoutFeedback,
  View
} from 'react-native'

export default ({ accessibilityLabel, label, labelColor, onPress, style }) => (
  <TouchableWithoutFeedback
    accessibilityLabel={accessibilityLabel}
    accessibilityComponentType="button"
    onPress={onPress}
  >
    <View style={[styles.container, style]}>
      <Text style={styles.label}>{label.toUpperCase()}</Text>
    </View>
  </TouchableWithoutFeedback>
)

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#333',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingTop: 5,
    paddingBottom: 5,
    paddingLeft: 25,
    paddingRight: 25,
    borderRadius: 50
  },
  label: {
    color: '#FFF',
    fontWeight: 'bold'
  }
})
