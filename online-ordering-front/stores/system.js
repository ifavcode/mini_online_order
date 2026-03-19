import { defineStore } from "pinia";
import { ref } from "vue";
import { getBusinessInfo, leftLikeDicts } from "../api/dict";


export const useSystemStore = defineStore("system", () => {
  const systemInfo = ref({});
  const safeArea = ref({});
  const appId = ref("");
  const businessInfo = ref({});
  const paySwitch = ref(true);
  const orderTip = ref('0');

  function initSystem() {
    uni.getSystemInfo({
      success(res) {
        systemInfo.value = res;
        safeArea.value = res.safeArea;
      },
      fail() {
        console.error("初始化系统配置出错");
      },
    });
    const accountInfo = uni.getAccountInfoSync();
    appId.value = accountInfo.miniProgram.appId
    initBusiness();
    initOtherConfig()
  }

  async function initOtherConfig() {
    const res = await leftLikeDicts(['pay_switch', 'order_tip'])
    if (res && res[0]) {
      paySwitch.value = res[0][0].dictValue === "1"
    }
    if (res && res[1]) {
      orderTip.value = res[1][0]?.dictValue || "0"
    }
  }

  async function initBusiness() {
    businessInfo.value = await getBusinessInfo() || {}
  }

  return {
    systemInfo,
    initSystem,
    safeArea,
    appId,
    businessInfo,
    paySwitch,
    orderTip
  };
});
