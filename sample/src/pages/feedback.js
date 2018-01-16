import React from 'react'
import { StyleSheet, Text, View } from 'react-native'
import NearIT from 'react-native-nearit'

import { FeedbackCard } from '../components'

const FeedbackPage = ({ feedbackId, feedbackQuestion, onFeedbackSent }) => (
  <View style={styles.container}>
    <FeedbackCard
      text={feedbackQuestion}
      onSendRating={async (rating, comment) => {
        try {
          await NearIT.sendFeedback(feedbackId, rating, comment)
          onFeedbackSent(true)
        } catch (err) {
          onFeedbackSent(false)
        }
      }}
    />
  </View>
)

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  }
})

export default FeedbackPage
