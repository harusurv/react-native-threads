#! /bin/bash
react-native bundle \
  --platform android \
  --dev false \
  --minify false \
  --entry-file index.js \
  --bundle-output android/app/src/main/assets/index.android.jsbundle

react-native bundle \
  --platform android \
  --dev false \
  --minify false \
  --entry-file index.thread.js \
  --bundle-output android/app/src/main/assets/index.thread.jsbundle

./android/gradlew build -p android

adb install android/app/build/outputs/apk/release/app-release.apk
