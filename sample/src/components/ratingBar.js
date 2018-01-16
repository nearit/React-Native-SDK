import React from 'react'
import { Image, StyleSheet, TouchableOpacity, View } from 'react-native'

const starIcon = require('../assets/star.png')
const emptyStarIcon = require('../assets/emptyStar.png')

const RatingBar = ({ rating, onRating, style }) => (
  <View style={[styles.ratingContainer, style]}>
    {[1, 2, 3, 4, 5].map(value => (
      <TouchableOpacity key={value} onPress={() => onRating(value)}>
        <Image source={value <= rating ? starIcon : emptyStarIcon} />
      </TouchableOpacity>
    ))}
  </View>
)

const styles = StyleSheet.create({
  ratingContainer: {
    flexDirection: 'row'
  }
})

export default RatingBar
