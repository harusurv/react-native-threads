module.exports = {
  presets: ['module:@react-native/babel-preset'],
  plugins: [
    [
      'module-resolver',
      {
        alias: {
          '~': './src',
          api: './src/api',
          assets: './src/assets',
          components: './src/components',
          constants: './src/constants',
          layouts: './src/layouts',
          locales: './src/locales',
          navigation: './src/navigation',
          screens: './src/screens',
          store: './src/store',
          styles: './src/styles',
          theme: './src/theme',
          utils: './src/utils',
          backend: './backend',
        },
        root: ['./src'],
      },
    ],
    '@babel/plugin-transform-class-static-block',  // Transforms static blocks
  ],
};