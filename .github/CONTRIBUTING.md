# Contributing

When contributing to this repository, please first discuss the change you wish to make via issue,
email, or any other method with the owners of this repository before making a change. 

Please note we have a code of conduct, please follow it in all your interactions with the project.

## Development

While developing changes to this project make sure you test them against the `nearit-react-native-sample-app` project (available at [panz3r/nearit-react-native-sample-app](https://github.com/panz3r/nearit-react-native-sample-app)).

The quickest way to develop is to link the 2 projects directly using `yarn link`, like this:
```bash
# Inside react-native-nearit-sdk root folder
yarn link

...

# Inside nearit-react-native-sample-app root folder
yarn link react-native-nearit
```

## Pull Request Process

1. Ensure any install or build dependencies are removed when doing a build.
2. Update the README.md with details of changes to the interface, this includes exposed methods, useful file locations and parameters.
3. Increase the version numbers in any examples files and the README.md to the new version that this
   Pull Request would represent. The versioning scheme we use is [SemVer](http://semver.org/).
