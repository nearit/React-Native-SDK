import React from 'react'
import { InteractionManager, StyleSheet, Text, View } from 'react-native'
import NearIT, { NearItConstants } from 'react-native-nearit'
import { Banner, Body, BottomNavBar, BottomNavBarItem, Button, Header, withTheme } from './components'

import alertsIcon from './assets/icon-alert.png'
import recipeIcon from './assets/icon-recipe.png'
import simpleNotificationIcon from './assets/icona-notifica.png'
import contentNotificationIcon from './assets/icona-notificaecontenuto.png'
import couponNotificationIcon from './assets/icona-couponsconto.png'
import feedbackNotificationIcon from './assets/icona-questionario.png'
import customJsonNotificationIcon from './assets/icon-code.png'
import homeIcon from './assets/icon-nearit.png'

const { Events, EventContent, Permissions, Statuses } = NearItConstants

class App extends React.Component {
  constructor () {
    super()

    this.state = {
      isOpened: false,
      profileId: '',
      bannerIcon: alertsIcon,
      bannerMessage: 'RNBanner',
      pageIndex: 0
    }

    this._toggleBanner = this._toggleBanner.bind(this)
    this._showBanner = this._showBanner.bind(this)
    this._hideBanner = this._hideBanner.bind(this)
    
    this._onPressRequestNotificationPermissions = this._onPressRequestNotificationPermissions.bind(this)
    this._onPressRequestLocationPermissions = this._onPressRequestLocationPermissions.bind(this)
    this._onPressRefreshCfg = this._onPressRefreshCfg.bind(this)
    this._getCoupons = this._getCoupons.bind(this)
    this._testBanner = this._testBanner.bind(this)

    console.log({NearItConstants})

    this._nearItSubscription = NearIT.setContentsListener(event => {
      console.log('Received a new event from NearIT', { event })
      const evtType = event[EventContent.type]
      if (evtType !== Events.PermissionStatus) {
        const evtContent = event[EventContent.content]
        const evtTracking = event[EventContent.trackingInfo]
        switch (evtType) {
          case Events.SimpleNotification:
              this._showBanner(evtContent[EventContent.message], simpleNotificationIcon)
            break

          case Events.Content:
              this._showBanner(evtContent[EventContent.message], contentNotificationIcon)
            break

          case Events.Feedback:
            this._showBanner(evtContent[EventContent.message], feedbackNotificationIcon)
          break
                        
          case Events.Coupon:
            this._showBanner(evtContent[EventContent.message], couponNotificationIcon)
          break

          case Events.CustomJson:
            this._showBanner(evtContent[EventContent.message], customJsonNotificationIcon)
          break

          default:
            this._showBanner('Received a NearIT event', alertsIcon)
        }
        
        NearIT.sendTracking(evtTracking, Statuses.notified)
      }
    })

    this._startNearIt()
  }

  componentWillUnmount () {
    if (this._closeTimer) {
      clearTimeout(this._closeTimer)
    }

    if (this._nearItSubscription) {
      this._nearItSubscription.remove()
    }
  }

  async _startNearIt () {
    try {
      await NearIT.startRadar()
      console.log('NearIT Radar Started!')
    } catch (err) {
      console.log('Could NOT start NearIT Radar...')
    }

    try {
      const profileId = await NearIT.getUserProfileId()
      console.log('Got UserProfileID!')
      this.setState({
        profileId
      })
    } catch (err) {
      console.log('Could NOT get UserProfileID...', err)
    }
  }

  async _onPressRequestNotificationPermissions () {
    const permissionGranted = await NearIT.requestNotificationPermission()
    
    this._showBanner(permissionGranted ? 'Notification Permission GRANTED' : 'Notification Permission NOT GRANTED')
  }
  
  async _onPressRequestLocationPermissions () {
    this.locationSubscription = NearIT.setContentsListener(event => {
      if (event[EventContent.type] === Events.PermissionStatus){
        if (event[EventContent.status] === Permissions.LocationGranted) {
          this._showBanner('Location Permission GRANTED')
        } else if (event[EventContent.status] === Permissions.LocationDenied) {
          this._showBanner('Location Permission NOT GRANTED')
        }

        this.locationSubscription.remove()
      }
    })

    const locationGranted = await NearIT.requestLocationPermission()
    console.log({locationGranted})
    if (typeof(locationGranted) !== 'undefined' && locationGranted != null) {
        this.locationSubscription.remove()
        this._showBanner(locationGranted ? 'Location Permission GRANTED' : 'Location Permission NOT GRANTED')
    }
  }

  async _onPressRefreshCfg () {
    console.log('Button pressed')
    try {
      await NearIT.refreshConfig()
      console.log('Cfg refreshed!')
      this._showBanner('Cfg refreshed!', recipeIcon)
    } catch (err) {
      console.log('Error while refreshing configs', err)
    }
  }

  async _getCoupons () {
    console.log('Get Coupons')
    try {
      const coupons = await NearIT.getCoupons()
      console.log({ coupons })
      this._showBanner(`You have ${coupons.length} coupon${coupons.length > 1 ? 's' : ''}!`, couponNotificationIcon)
    } catch (err) {
      console.log('Error while retrieving coupons', err)
    }
  }

  _testBanner () {
    this._showBanner('Here I am!')
  }

  _showBanner (bannerMessage, bannerIcon = alertsIcon) {
    this.setState({
        bannerIcon, 
        bannerMessage,
        isOpened: true
      },
      this._toggleBanner
    )
  }

  _hideBanner () {
    this.setState({
      isOpened: false
    })
  }

  _toggleBanner () {
    if (this._closeTimer) {
      clearTimeout(this._closeTimer)
    }

    this._closeTimer = setTimeout(() => {
        InteractionManager.runAfterInteractions(this._hideBanner)
    }, 5000)
  }

  _onPageSelected (pageIndex) {
    this.setState({
      pageIndex
    })
  }

  render () {
    const { profileId, isOpened, bannerIcon, bannerMessage, pageIndex } = this.state
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
          icon={bannerIcon}
        >
          <Text style={styles.bannerText}>{bannerMessage}</Text>
        </Banner>
        <Body
          backgroundColor={primary}
        >
          <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
            <Text>{ profileId }</Text>

            <Button
              label='Notification Permission'
              labelColor={accent}
              accessibilityLabel='Open NearIT Permission request for Notifications'
              onPress={this._onPressRequestNotificationPermissions}
              style={styles.actionButton}
            />

            <Button
              label='Location Permission'
              labelColor={accent}
              accessibilityLabel='Open NearIT Permission request for Location'
              onPress={this._onPressRequestLocationPermissions}
              style={styles.actionButton}
            />

            <Button
              label='Refresh Configurations'
              labelColor={accent}
              accessibilityLabel='Refresh NearIT Configurations'
              onPress={this._onPressRefreshCfg}
              style={styles.actionButton}
            />
            
            <Button
              label='Get Coupons'
              labelColor={accent}
              accessibilityLabel='Get NearIT Coupons'
              onPress={this._getCoupons}
              style={styles.actionButton}
            />
            
            <Button
              label='Test Banner'
              labelColor={accent}
              accessibilityLabel='Open NearIT Banner'
              onPress={this._testBanner}
              style={styles.actionButton}
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
                textColor='#FFF'
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
  },
  actionButton: {
    marginTop: 5,
    marginBottom: 5
  }
})

const nearItTheme = {
  primaryColor: '#A69FFF',
  primaryDarkColor: '#9F92FF',
  accentColor: '#FFFFFF'
}

export default withTheme(nearItTheme)(App)
