#! /bin/bash
derived_data_dir=$(mktemp -d)
ios_app_path="$derived_data_dir/Build/Products/Release-iphonesimulator/example.app"

xcodebuild -workspace ios/example.xcworkspace \
  -scheme example \
  -configuration Release \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath "$derived_data_dir"

echo "$derived_data_dir"

react-native bundle --platform ios \
  --dev false \
  --minify false \
  --entry-file index.thread.js \
  --bundle-output "$ios_app_path/index.thread.jsbundle"

xcrun simctl install booted "$ios_app_path"
