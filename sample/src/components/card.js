import React from 'react'
import { StyleSheet, View } from 'react-native'

const Card = ({ children }) => <View style={styles.card}>{children}</View>

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#FFF',
    flexDirection: 'column',
    width: 300,
    padding: 25,
    borderRadius: 5,
    justifyContent: 'center',
    alignItems: 'center'
  }
})

export default Card
