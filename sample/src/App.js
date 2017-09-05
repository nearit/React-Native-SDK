import React from 'react'
import { InteractionManager, StyleSheet, Text, View } from 'react-native'
import NearIT, { NearItConstants } from 'react-native-nearit'
import { Banner, Body, BottomNavBar, BottomNavBarItem, Button, Header, withTheme } from './components'
import simpleNotificationIcon from './assets/icona-notifica.png'
import homeIcon from './assets/icon-nearit.png'

class App extends React.Component {
  constructor () {
    super()

    this.state = {
      isOpened: false,
      pageIndex: 0
    }

    this._onPressRefreshCfg = this._onPressRefreshCfg.bind(this)
    this._toggleBanner = this._toggleBanner.bind(this)

    console.log({NearItConstants})

    NearIT.startRadar()
      .then(() => {
        console.log('NearIT Radar Started!')
      })
      .catch(() => {
        console.log('Could NOT start NearIT Radar...')
      })
  }

  componentWillUnmount () {
    if (this._closeTimer) {
      clearTimeout(this._closeTimer)
    }
  }

  _onPressRefreshCfg () {
    console.log('Button pressed')
    NearIT.refreshConfig()
        .then(() => {
          console.log('Cfg refreshed!')
        })
        .catch(err => {
          console.log('Error while refreshing configs', err)
        })
  }

  _toggleBanner () {
    const { isOpened } = this.state

    this.setState({
      isOpened: !isOpened
    })

    if (this._closeTimer) {
      clearTimeout(this._closeTimer)
    }

    if (!isOpened) {
      this._closeTimer = setTimeout(() => {
        InteractionManager.runAfterInteractions(this._toggleBanner)
      }, 5000)
    }
  }

  _onPageSelected (pageIndex) {
    this.setState({
      pageIndex
    })
  }

  render () {
    const { isOpened, pageIndex } = this.state
    const { theme: { primary, primaryDark, accent } } = this.props

    return (
      <View style={styles.container}>
        <Header
          statusBarColor={primaryDark}
          appBarColor={primaryDark}
        >
          <Text style={styles.headerText}>NearIT Sample</Text>
        </Header>
        <Banner
          color='#333'
          opened={isOpened}
          onPress={this._toggleBanner}
          icon={simpleNotificationIcon}
        >
          <Text style={styles.bannerText}>RNBanner</Text>
        </Banner>
        <Body
          backgroundColor={primary}
        >
          <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
            <Button
              label='Refresh Configurations'
              labelColor={accent}
              accessibilityLabel='Refresh NearIT Configurations'
              onPress={this._onPressRefreshCfg}
              style={{ marginBottom: 5 }}
            />
            <Button
              label='Test Banner'
              labelColor={accent}
              accessibilityLabel='Open NearIT Banner'
              onPress={this._toggleBanner}
              style={{ marginTop: 5 }}
            />
          </View>
        </Body>
        <BottomNavBar
          backgroundColor={primaryDark}
        >
          {
            ['Home'].map((v, i) => (
              <BottomNavBarItem
                key={i}
                icon={homeIcon}
                text={v}
                textColor='#000'
                selected={pageIndex === i}
                onPress={() => this._onPageSelected(i)}
              />
            ))
          }
        </BottomNavBar>
      </View>
    )
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  headerText: {
    color: '#FFF',
    fontSize: 15,
    fontWeight: 'bold'
  },
  bannerText: {
    color: '#FFF',
    fontWeight: 'bold'
  }
})

const nearItTheme = {
  primaryColor: '#A69FFF',
  primaryDarkColor: '#9F92FF',
  accentColor: '#FFFFFF'
}

export default withTheme(nearItTheme)(App)
