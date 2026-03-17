import App from "./App.vue";
import * as Pinia from 'pinia';
import './style.css'

// 仅 H5(浏览器) 环境引入微信 JSSDK，避免其它端报错
// #ifdef H5
import wx from 'weixin-js-sdk'
if (typeof window !== 'undefined') window.wx = wx
// #endif

// #ifndef VUE3
import Vue from "vue";
Vue.config.productionTip = false;
App.mpType = "app";
const app = new Vue({
  ...App,
});
app.$mount();
// #endif

// #ifdef VUE3
import { createSSRApp } from "vue";
export function createApp() {
  const app = createSSRApp(App);
  app.use(Pinia.createPinia());
  return {
    app,
    Pinia
  };
}
// #endif
