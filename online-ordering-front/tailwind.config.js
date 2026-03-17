const { resolve, isMp } = require("./shared");
const cssMacro = require("weapp-tailwindcss/css-macro");
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "pages/**/*.{vue,ts,js,wxml}",
    "components/**/*.{vue,ts,js,wxml}",
    "views/**/*.{vue,ts,js,wxml}",
  ].map(resolve),
  theme: {
    extend: {
      colors: {
        primary: "#3B82F6",
        second: "#00ff00",
      },
    },
  },
  plugins: [
    // https://weapp-tw.icebreaker.top/docs/quick-start/uni-app-css-macro
    cssMacro({
      variantsMap: {
        wx: "MP-WEIXIN",
        "-wx": {
          value: "MP-WEIXIN",
          negative: true,
        },
      },
    }),
  ],
  corePlugins: {
    preflight: !isMp,
  },
};
