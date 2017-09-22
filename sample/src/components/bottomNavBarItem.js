import React from 'react'
import { Animated, Image, StyleSheet, Text, TouchableWithoutFeedback, View } from 'react-native'

const SCALE_SELECTED = 1.0
const SCALE_DEFAULT = 0.8

export default class BottomNavBarItem extends React.Component {
  state = {
    scaleAnim: new Animated.Value(SCALE_DEFAULT),
  }

  componentDidMount() {
    const { selected } = this.props

    if (selected) {
      this.setState({
        scaleAnim: new Animated.Value(SCALE_SELECTED),
      })
    }
  }

  componentDidUpdate() {
    const { selected } = this.props

    Animated.timing(
      this.state.scaleAnim,
      {
        toValue: selected ? SCALE_SELECTED : SCALE_DEFAULT,
        duration: 250,
      }
    ).start();
  }

  render() {
    const { scaleAnim } = this.state
    const { children, color, icon, onPress, text, textColor } = this.props

    return (
      <Animated.View
        style={{
          transform: [{ scale: scaleAnim }],
        }}
      >
        <TouchableWithoutFeedback
          onPress={onPress ? onPress : null}
          accessibilityComponentType='button'
          hitSlop={{ top: 5, left: 5, bottom: 5, right: 5 }}
        >
          <View
            style={styles.container}
          >
            <Image
              source={icon}
              resizeMode='contain'
              style={styles.icon}
            />
            <Text style={[styles.text, { color: textColor }]}>{text}</Text>
          </View>
        </TouchableWithoutFeedback>
      </Animated.View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    height: 50,
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'space-around',
  },
  icon: {
    height: 30,
    width: 30
  },
  text: {
    fontWeight: 'bold'
  }
})