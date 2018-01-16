import React, { Component } from 'react'
import { StyleSheet, Text, TextInput, View } from 'react-native'

import Button from './button'
import Card from './card'
import RatingBar from './ratingBar'

class FeedbackCard extends Component {
  state = {
    rating: 1,
    comment: ''
  }

  _sendFeedback = () => {
    const { onSendRating } = this.props
    const { comment, rating } = this.state
    if (onSendRating) {
      onSendRating(rating, comment)
    }
  }

  render() {
    const { text } = this.props
    const { comment, rating } = this.state
    return (
      <Card>
        <Text style={styles.feedbackQuestion}>{text}</Text>

        <RatingBar
          rating={rating}
          onRating={rating => this.setState({ rating })}
          style={styles.ratingBar}
        />

        <Text style={styles.commentHint}>Leave a comment (optional):</Text>

        <TextInput
          editable={true}
          multiline={true}
          numberOfLines={4}
          onChangeText={comment => this.setState({ comment })}
          value={comment}
          style={styles.textArea}
        />

        <Button
          label="Send"
          accessibilityLabel="Send Feedback result"
          onPress={this._sendFeedback}
          style={styles.actionButton}
        />
      </Card>
    )
  }
}

const styles = StyleSheet.create({
  feedbackQuestion: {
    color: 'rgb(51, 51, 51)',
    fontSize: 15
  },
  ratingBar: {
    marginTop: 30,
    marginBottom: 30
  },
  commentHint: {
    color: 'rgb(119, 119, 119)',
    fontSize: 13,
    marginBottom: 10
  },
  textArea: {
    width: 250,
    height: 70,
    borderColor: 'rgb(225, 225, 225)',
    borderWidth: 1,
    borderRadius: 5,
    padding: 2
  },
  actionButton: {
    width: 250,
    height: 45,
    marginTop: 30
  }
})

export default FeedbackCard
