<script setup>
import { computed, ref } from 'vue';
import { useSystemStore } from '../stores/system';
import { useGoodStore } from '../stores/good';
import Constant from '../utils/Constant';
import { storeToRefs } from 'pinia';
import { useUserStore } from '../stores/user';
import { isBusinessHours, formatBusinessHours } from '../utils/index'

const systemStore = useSystemStore()
const goodStore = useGoodStore()
const extraBottomHeight = 11
const { systemInfo } = systemStore
const { choosedGoods } = storeToRefs(goodStore)
const userStore = useUserStore()
const loading = ref(false)

const statusBarHeight = computed(() => {
  return systemInfo.statusBarHeight + extraBottomHeight
})

const totalPrice = computed(() => {
  if (Object.keys(goodStore.choosedGoods).length === 0) {
    return "0.00"
  }
  let back = 0
  for (let good of goodStore.choosedGoods) {
    let innerTotal = good.basePrice
    if (good[Constant.SPECIFICATION]) {
      for (let cur of good[Constant.SPECIFICATION]) {
        cur.specItems.forEach(v => {
          innerTotal += (v.extraPrice || 0)
        })
      }
    }
    back += innerTotal * good.count
  }
  return back.toFixed(2)
})

function handleClickIcon() {
  goodStore.toggleBuyCartShow(!goodStore.buyCartShow)
}

async function doLoginAndGoConfirm() {
  try {
    loading.value = true
    if (!userStore.checkLogin()) {
      const res = await userStore.login()
      userStore.setUser(res.data, res.sessionKey, res.openid)
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false
  }
  uni.navigateTo({
    url: '/pages/confirm/index'
  })
}

async function chooseOver() {
  if (userStore.user.nickname === "未命名用户") {
    uni.showModal({
      title: '提示',
      content: '检测到您还未完善个人信息，是否前往填写？',
      confirmText: '去填写',
      cancelText: '暂不',
      success: async (res) => {
        if (res.confirm) {
          uni.navigateTo({
            url: '/pages/profile/update'
          })
        } else {
          await doLoginAndGoConfirm()
        }
      }
    })
    return
  }
  await doLoginAndGoConfirm()
}

</script>

<template>
  <view :style="{ height: statusBarHeight + 'px' }" class="">
    <view class="border-t h-full border-gray-100 flex justify-between px-4 py-6 items-center" v-if="isBusinessHours()">
      <view class="flex gap-2 items-center">
        <uni-badge size="small" :text="goodStore.choosedGoods.length" absolute="rightTop" type="primary">
          <view class="bg-gray-200 px-2 rounded-md" @click="handleClickIcon">
            <text class="iconfont text-xl text-primary">&#xe600;</text>
          </view>
        </uni-badge>
        <view>
          <text class="text-sm">合计</text>
          <text class="font-bold text-red-500">
            <text class="text-xs">￥</text>
            <text>{{ totalPrice }}</text>
          </text>
        </view>
      </view>
      <view>
        <button class="clear-button text-sm bg-primary text-white py-1 h-8 flex items-center"
          :disabled="choosedGoods.length === 0" @click="chooseOver" :loading="loading">选好了</button>
      </view>
    </view>
    <view v-else class="border-t border-gray-100 px-4 py-2">
      <view class="text-sm text-primary text-center w-full flex items-center justify-center gap-4">
        <i class="iconfont text-5xl">&#xe660;</i>
        营业时间：{{
          formatBusinessHours() }}
      </view>
    </view>
  </view>
</template>