module.exports = {
  testEnvironment: 'jsdom',
  setupFiles: ['<rootDir>/jestTextEncoderPolyfill.js'], 
  setupFilesAfterEnv: ['<rootDir>/setupTests.js'],   
  moduleNameMapper: {
    '\\.module\\.css$': 'identity-obj-proxy',
    '\\.css$': '<rootDir>/__mocks__/styleMock.js',  
  },
  collectCoverageFrom: [
    "src/**/*.{js,jsx,ts,tsx}", 
    "!src/**/*.test.{js,jsx,ts,tsx}", 
    "!src/**/index.{js,jsx,ts,tsx}", 
    "!**/node_modules/**", 
    "!src/config/testConfig.jsx",
    "!src/main.jsx",
  ],
  coverageDirectory: "coverage", 
};
