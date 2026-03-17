import { defineStore } from "pinia";
import { ref } from "vue";
import { getProfile, webLogin, wxLogin } from "../api/user";
import { getEnvType } from "../utils";

export const useUserStore = defineStore("user", () => {
  const user = ref({});
  const sessionKey = ref("");
  const openid = ref("");
  const accessToken = ref("");
  const tableCode = ref("")
  const tableName = ref("")

  async function login() {
    const env = getEnvType()
    return new Promise((resolve, reject) => {
      if (env !== 'h5') {
        uni.login({
          provider: "weixin",
          success: async (res) => {
            if (res.code) {
              try {
                const loginResult = await wxLogin(res.code);
                resolve(loginResult);
              } catch (err) {
                reject(err);
              }
            } else {
              reject(new Error("未获取到登录code"));
            }
          },
          fail: (err) => {
            reject(err);
          },
        });
      } else {
        webLogin().then(res => {
          resolve(res)
        }).catch(err => {
          reject(err)
        })
      }
    });
  }

  async function initUser(count) {
    const env = getEnvType()
    if (count > 2) {
      console.error("登陆失败！");
      return;
    }
    const accessToken = uni.getStorageSync("accessToken");
    if (!accessToken) {
      console.log("未获取到accessToken,尝试自动登录");
      const res = await login();
      if (env !== 'h5') {
        setUser(res.data, res.sessionKey, res.openid, res.accessToken);
      } else {
        setUser(res.data, "", "", res.data.accessToken)
      }
      // if (res.data.nickname === "未命名用户") {
      //   uni.navigateTo({
      //     url: '/pages/profile/update'
      //   })
      // }
      console.log("自动登陆成功");
      return;
    }
    try {
      console.log("已获取到accessToken，尝试获取用户信息");
      const res = await getProfile();
      if (res != null && Object.keys(res).length > 0) {
        setUser(res, "", res.openid, accessToken);
        console.log("获取用户信息成功");
      } else {
        throw new Error();
      }
    } catch (error) {
      console.log("登陆失败，再次尝试", error);
      if (error.includes("token 无效")) {
        logout();
      }
      initUser((count ?? 0) + 1);
    }
  }

  function setUser(u, sk, oi, token) {
    user.value = u;
    sessionKey.value = sk;
    openid.value = oi;
    accessToken.value = token;
    uni.setStorageSync("user", u);
    uni.setStorageSync("sessionKey", sk);
    uni.setStorageSync("openid", oi);
    uni.setStorageSync("accessToken", token);
  }

  function logout() {
    user.value = {};
    sessionKey.value = "";
    openid.value = "";
    accessToken.value = "";
    uni.clearStorageSync("user");
    uni.clearStorageSync("sessionKey");
    uni.clearStorageSync("openid");
    uni.clearStorageSync("accessToken");
  }

  function checkLogin() {
    return (
      user.value != null &&
      user.value != undefined &&
      Object.keys(user.value).length > 0
    );
  }

  return {
    login,
    checkLogin,
    setUser,
    accessToken,
    logout,
    initUser,
    user,
    sessionKey,
    openid,
    accessToken,
    tableCode,
    tableName
  };
});
