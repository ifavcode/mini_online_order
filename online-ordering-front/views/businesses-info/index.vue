<script setup>
import kefuButton from '../../components/kefuButton.vue';
import profileButton from '../../components/profileButton.vue';
import { useSystemStore } from '../../stores/system';
import { storeToRefs } from 'pinia';

const systemStore = useSystemStore()

const { businessInfo } = storeToRefs(systemStore)

</script>

<template>
  <view class="w-full h-52" :style="{ paddingTop: systemStore.safeArea.top + 'px' }">
    <view class="w-full h-52 absolute top-0 left-0 z-[-1]">
      <image :src="businessInfo.shopBgImage" mode="aspectFill" class="w-full h-52 blur-sm"></image>
      <view class="absolute top-0 left-0 w-full h-52 bg-black opacity-50">
      </view>
    </view>
    <view class="h-10 w-full flex items-center px-2">
      <kefu-button color="white"></kefu-button>
      <!-- <profile-button color="white"></profile-button> -->
    </view>
    <view class="w-full h-28 mt-1 px-4 flex gap-4 items-center ">
      <view class="">
        <image class="size-16 rounded-full" :src="businessInfo.shopImage" mode="widthFix" />
      </view>
      <view class="">
        <view>
          <text class="text-white text-lg">{{ businessInfo.shopName }}</text>
        </view>
        <view>
          <text class="text-white text-xs">{{ businessInfo.shopDesc }}</text>
        </view>
      </view>
      <view class="w-[0.5px] h-8 bg-white mx-1">
      </view>
      <view class="flex flex-col items-center w-10">
        <text class="iconfont text-white text-2xl">&#xe614;</text>
        <text class="text-xs text-white">付款</text>
      </view>
    </view>
  </view>
</template>