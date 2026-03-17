<script setup lang="js">
import { ref } from 'vue';
import Dynamic from './dynamic.vue';
import Order from './order.vue';

const current = ref(1)
const items = ref([
  ' 动态 ', ' 下单 ',
])

function toggleItem(e) {
  if (current.value !== e.currentIndex) {
    current.value = e.currentIndex
  }
}
</script>

<template>
  <view class="rounded-tl-xl rounded-tr-xl w-full h-10 translate-y-[-16rpx] bg-white">
    <uni-segmented-control :current="current" :values="items" style-type="text" active-color="#000"
      @clickItem="toggleItem" />
    <dynamic v-if="current === 0" />
    <order v-if="current === 1" />
  </view>
</template>