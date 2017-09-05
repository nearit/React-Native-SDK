import React from 'react'
import { Animated, Image, Platform, StatusBar, StyleSheet, Text, TouchableWithoutFeedback, View } from 'react-native'

const STATUSBAR_HEIGHT = Platform.OS === 'ios' ? 20 : StatusBar.currentHeight
const APPBAR_HEIGHT = Platform.OS === 'ios' ? 44 : 56
const BANNER_HEIGHT = STATUSBAR_HEIGHT + APPBAR_HEIGHT

export default class Banner extends React.Component {
  state = {
    heightAnim: new Animated.Value(0),
  }

  componentDidUpdate() {
    const { opened } = this.props

    Animated.timing(
      this.state.heightAnim,
      {
        toValue: opened ? BANNER_HEIGHT : 0,
        duration: 500,
      }
    ).start();
  }

  componentWillUnmount() {
    if (this.closeTimer) {
      clearTimeout(this.closeTimer)
    }
  }

  render() {
    const { heightAnim } = this.state
    const { children, color, icon, onPress } = this.props

    return (
      <Animated.View
        style={[
          styles.banner,
          {
            backgroundColor: color,
            height: heightAnim,
          }
        ]}
      >
        <TouchableWithoutFeedback
          onPress={onPress ? onPress : null}
        >
          <View style={styles.bannerContainer}>
            {icon ?
              <Image
                style={styles.bannerIcon}
                source={icon}
              />
              :
              null
            }
            <View style={styles.bannerContent}>
              {children}
            </View>
          </View>
        </TouchableWithoutFeedback>
      </Animated.View>
    );
  }
}

const styles = StyleSheet.create({
  banner: {
    position: 'absolute',
    width: '100%',
    overflow: 'hidden',
    backgroundColor: '#AAA',
  },
  bannerContainer: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: STATUSBAR_HEIGHT,
  },
  bannerIcon: {
    width: 35,
    height: 30,
    marginLeft: 10,
    padding: 5,
  },
  bannerContent: {
    marginLeft: 10,
  }
})