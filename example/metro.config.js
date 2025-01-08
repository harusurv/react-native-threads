const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');
const path = require('path')
/**
 * Metro configuration
 * https://metrobundler.dev/docs/configuration
 *
 * @type {import('metro-config').MetroConfig}
 */
const defaultConfig = getDefaultConfig(__dirname);

const config = {
  transformer: {
    getTransformOptions: async () => ({
      transform: {
        experimentalImportSupport: false,
        inlineRequires: true,
      },
    }),
  },
  watchFolders: [
    path.resolve(__dirname, 'node_modules'), // Ensure it watches `node_modules`
  ],
};

module.exports = mergeConfig(defaultConfig, config);
