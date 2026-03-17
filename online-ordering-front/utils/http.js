// http.js - UniApp 请求封装类

import { useUserStore } from "../stores/user";

// export const BASE_URL = "http://localhost:9988";
// export const BASE_URL = "http://192.168.0.108:9988";
// export const BASE_URL = "http://192.168.1.110:9988";
export const BASE_URL = "https://www.guetzjb.cn/mini_order";

class UniHttp {
  constructor(config = {}) {
    this.config = {
      timeout: 60000,
      header: {
        "Content-Type": "application/json",
      },
      ...config,
    };
  }

  /**
   * 通用请求方法
   * @param {string} method - 请求方法 GET/POST/PUT/DELETE 等
   * @param {string} url - 请求地址
   * @param {object} param - 请求参数
   * @param {object} customConfig - 自定义配置
   * @returns {Promise}
   */
  request(method, url, param = {}, customConfig = {}) {
    const config = {
      method,
      url,
      ...this.config,
      ...param,
      ...customConfig,
    };

    // 请求前钩子
    if (config.beforeRequest) {
      config.beforeRequest(config);
    }

    return new Promise((resolve, reject) => {
      uni.request({
        url: config.url,
        method: config.method,
        data: config.data,
        header: config.header,
        timeout: config.timeout,
        dataType: config.dataType || "json",
        responseType: config.responseType || "text",
        success: (response) => {
          // 响应后钩子
          const result = config.afterResponse
            ? config.afterResponse(response)
            : response.data;
          resolve(result);
        },
        fail: (error) => {
          reject(error);
        },
      });
    });
  }

  /**
   * GET 请求
   */
  get(url, data, config) {
    return this.request("GET", url, { data }, config);
  }

  /**
   * POST 请求
   */
  post(url, data, config) {
    return this.request("POST", url, { data }, config);
  }

  /**
   * PUT 请求
   */
  put(url, data, config) {
    return this.request("PUT", url, { data }, config);
  }

  /**
   * DELETE 请求
   */
  delete(url, data, config) {
    return this.request("DELETE", url, { data }, config);
  }
}

// 创建实例并配置拦截器
const http = new UniHttp({
  timeout: 30000,
  header: {
    "Content-Type": "application/json",
  },
  // 请求拦截器
  beforeRequest: (config) => {
    // 添加 token
    const accessToken = uni.getStorageSync("accessToken") || useUserStore().accessToken;
    if (accessToken) {
      config.header = {
        ...config.header,
        Authorization: `Bearer ${accessToken}`,
      };
    }
    config.url = BASE_URL + config.url;

    // 显示加载提示
    uni.showLoading({
      title: "加载中...",
      mask: true,
    });

    return config;
  },
  // 响应拦截器
  afterResponse: (response) => {
    // 隐藏加载提示
    uni.hideLoading();

    const { statusCode, data } = response;

    // 根据状态码处理
    if (statusCode === 200) {
      return data;
    } else if (statusCode === 401) {
      // token 过期，跳转登录
      uni.showToast({
        title: "登录已过期",
        icon: "none",
      });
      uni.navigateTo({
        url: "/pages/login/login",
      });
      return Promise.reject(new Error("未授权"));
    } else {
      // uni.showToast({
      //   title: data.message || "请求失败",
      //   icon: "none",
      // });
      return Promise.reject(data);
    }
  },
});

export default http;
