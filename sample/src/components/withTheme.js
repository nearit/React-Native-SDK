import React, { Component } from 'react'

export default ({ primaryColor, primaryDarkColor, accentColor }) =>
  BaseComponent =>
    class WithTheme extends Component {
      constructor() {
        super()
        
        this.theme = {
          primary: primaryColor,
          primaryDark: primaryDarkColor,
          accent: accentColor
        }
      }

      render() {
        return (
          <BaseComponent theme={this.theme} />
        )
      }
    }