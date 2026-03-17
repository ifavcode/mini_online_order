<script setup>
import { computed } from 'vue';
import BuyCart from '../../components/buyCart.vue';
import { useGoodStore } from '../../stores/good';
import { useSystemStore } from '../../stores/system';
import { storeToRefs } from 'pinia';
import Constant from '../../utils/Constant';

const props = defineProps({
  height: {
    type: Number,
    required: true
  }
})
const height = props.height
const goodStore = useGoodStore()
const { choosedGoods } = storeToRefs(goodStore)

const systemStore = useSystemStore()
const { systemInfo } = systemStore

const remainContentHeight = computed(() => {
  return height - systemInfo.statusBarHeight * 2 - 4
})

function closePop() {
  goodStore.toggleBuyCartShow(false)
}

function countChange(val, index) {
  if (val === 0) {
    choosedGoods.value.splice(index, 1)
    if (choosedGoods.value.length === 0) {
      goodStore.toggleBuyCartShow(false)
    }
  }
}

function calcFinalPrice(good) {
  let val = good.basePrice
  if (good[Constant.SPECIFICATION]) {
    for (let spec of good[Constant.SPECIFICATION]) {
      spec.specItems.forEach(v => {
        val += (v.extraPrice || 0)
      })
    }
  }
  return val
}

function clear() {
  choosedGoods.value = []
  goodStore.toggleBuyCartShow(false)
}

</script>

<template>
  <view>
    <view class="flex justify-end px-4 py-2">
      <view class="flex items-center" @click="clear" v-if="choosedGoods && choosedGoods.length > 0">
        <uni-icons type="trash-filled" size="20" color="#6B7280" @click="closePop"></uni-icons>
        <text class="text-sm text-gray-500">清空</text>
      </view>
    </view>
    <scroll-view :scroll-y="true" :style="{ height: remainContentHeight + 'px' }">
      <view v-if="!choosedGoods || choosedGoods.length === 0"
        class="flex flex-col items-center justify-center text-gray-400 h-full">
        <image src="/static/empty_cart.png" class="size-12 mb-4 opacity-80"/>
        <text class="text-base mb-1">购物车空空如也</text>
        <text class="text-xs">快去添加商品吧～</text>
      </view>
      <view v-for="(good, index) in choosedGoods" :key="good.id" class="flex px-4 gap-2 mb-1">
        <view>
          <image :src="good.imgUrl || '/static/no_img.jpg'" class="size-16 rounded-md" mode="aspectFit"/>
        </view>
        <view class="flex flex-col flex-1">
          <view>{{ good.name }}</view>
          <view class="text-sm text-gray-500">
            <text v-for="(spec, specIndex) in good[Constant.SPECIFICATION]" :key="spec.id">
              <text v-for="(item, index) in spec.specItems" :key="item.id">
                <text>{{ item.name }}</text>
                <text>
                  {{ (specIndex + 1) !== good[Constant.SPECIFICATION].length ? '、' : '' }}
                </text>
              </text>
            </text>
          </view>
          <view class="flex justify-between">
            <view>
              <text class="text-xs text-red-500">￥</text>
              <text class="text-sm text-red-500">{{ calcFinalPrice(good) }}</text>
            </view>
            <view>
              <uni-number-box v-model="good.count" :min="0" :max="99"
                @change="(val) => countChange(val, index)"></uni-number-box>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    <BuyCart />
  </view>
</template>