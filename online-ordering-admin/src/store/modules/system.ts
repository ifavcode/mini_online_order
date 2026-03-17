import { defineStore } from "pinia";
import { ref } from "vue";
import { store } from "..";
import { useUserStoreHook } from "./user";
import { emitter } from "@/utils/mitt";
import { getToken } from "@/utils/auth";
import { getDemoMode } from "@/api/system";


export const useSystemStore = defineStore("system_store", () => {

  const es = ref<WebSocket | null>(null)
  const demoMode = ref<boolean>(false)
  const wsUrl = import.meta.env.VITE_APP_WS_BASE_API

  function initWs(count = 1) {
    if (count > 3) {
      console.error('已重试' + (count - 1) + '次，websocket连接失败!!!')
      return
    }
    const userId = useUserStoreHook().id
    if (!userId) {
      console.log('userId不存在，1s后重试');
      setTimeout(() => {
        initWs(count + 1)
      }, 1000);
      return
    }
    es.value = new WebSocket(wsUrl + getToken().accessToken)
    es.value.onopen = function (e) {
      console.log('连接成功');
    }
    es.value.onerror = function (e) {
      console.log('连接失败1s后重试', e);
      es.value.close()
      es.value = null
      setTimeout(() => {
        initWs(count + 1)
      }, 1000);
    }
    es.value.onclose = function (e) {
      console.log('连接关闭', e);
      es.value.close()
      es.value = null
    }
    es.value.onmessage = function (e) {
      if (e.data == 'ping') {
        es.value.send('pong')
        return
      }
      const data = JSON.parse(e.data)
      emitter.emit('refreshOrders', parseInt(data.content))
    }
  }

  async function initDemoMode() {
    demoMode.value = await getDemoMode()
  }

  return {
    es,
    initWs,
    initDemoMode,
    demoMode
  }
})


export function useSystemStoreHook() {
  return useSystemStore(store);
}
