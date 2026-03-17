<script setup>
import { useGoodStore } from '../../stores/good';
import { storeToRefs } from 'pinia';
import Constant from '../../utils/Constant'
import { computed, ref } from 'vue';
import { useSystemStore } from '../../stores/system';

const props = defineProps({
  height: {
    type: Number,
    required: true
  }
})
const height = props.height


const goodStore = useGoodStore()
const { goodDetails, choosedSpec, count } = storeToRefs(goodStore)

const systemStore = useSystemStore()
const { safeArea, systemInfo } = systemStore

const remainContentHeight = computed(() => {
  return height - systemInfo.statusBarHeight * 2 - 20 - 116
})

function closePop() {
  goodStore.togglePopShow(false)
}

function chooseSpecification(item, faId, id, type) {
  if (item.stock === 0) return
  if (type === 'more') {
    if (choosedSpec.value.get(faId).includes(id)) {
      const arr = choosedSpec.value.get(faId).filter(v => v !== id)
      choosedSpec.value.set(faId, arr)
    } else {
      choosedSpec.value.get(faId).push(id)
    }
  } else if (type === 'one') {
    choosedSpec.value.set(faId, id)
  }
}

function collectChoosedIds() {
  const set = new Set()
  for (const val of choosedSpec.value.values()) {
    if (Array.isArray(val)) {
      for (const v of val) {
        set.add(v)
      }
    } else {
      set.add(val)
    }
  }
  return set
}

const totalPrice = computed(() => {
  if (Object.keys(goodDetails.value).length === 0) return 0;
  let back = goodDetails.value.basePrice;
  const set = collectChoosedIds();
  for (let cur of goodDetails.value[Constant.SPECIFICATION]) {
    for (let curTwo of cur.specItems) {
      if (set.has(curTwo.id)) {
        back += (curTwo.extraPrice || 0);
      }
    }
  }
  return (back * count.value).toFixed(2);
});

function confirm() {
  let tmpDetails = JSON.parse(JSON.stringify(goodDetails.value))
  const set = collectChoosedIds()
  for (let cur of tmpDetails[Constant.SPECIFICATION]) {
    cur.specItems = cur.specItems.filter(v => set.has(v.id))
  }
  tmpDetails['count'] = count.value
  goodStore.addChoosedGoods(tmpDetails)
  closePop()
}


</script>

<template>
  <view>
    <view class="flex justify-end">
      <uni-icons type="closeempty" size="20" color="#333" @click="closePop"></uni-icons>
    </view>
    <view class="flex gap-2 mt-2 border-b pb-4 border-gray-100">
      <view>
        <image :src="goodDetails.imgUrl || '/static/no_img.jpg'" class="size-24" mode="aspectFit"></image>
      </view>
      <view class="flex flex-col justify-between flex-1 pr-2">
        <view class="flex flex-col gap-2">
          <view>
            <view>{{ goodDetails.name }}</view>
            <view class="text-gray-500 text-sm">{{ goodDetails.description }}</view>
          </view>
          <text class="text-red-500 text-sm">
            <text class="text-xs">￥</text>
            <text>{{ totalPrice }}</text>
          </text>
        </view>
      </view>
      <!-- TODO 分享 -->
      <view></view>
    </view>
    <view v-if="goodDetails.hasSpec === 1">
      <scroll-view :scroll-y="true" :style="{ height: remainContentHeight + 'px' }">
        <view v-for="(spec, index) in goodDetails[Constant.SPECIFICATION]" :key="spec.id"
          class="py-4 border-b border-gray-100">
          <view class="mb-1">{{ spec.name }}</view>
          <view class="flex flex-wrap gap-4 items-center">
            <view v-for="(item, itemIndex) in spec.specItems" :key="item.id"
              @click="chooseSpecification(item, spec.id, item.id, spec.selectType)" :class="[
                spec.selectType === 'more' ? (choosedSpec.get(spec.id)?.includes(item.id) ? 'active' : '') :
                  (choosedSpec.get(spec.id) === item.id ? 'active' : ''),
                item.stock === 0 ? 'bg-gray-50' : ''
              ]"
              class="flex flex-wrap gap-2 items-center rounded-md px-2 py-1 bg-gray-100 text-black border border-gray-100">
              <view class="text-sm" :class="item.stock === 0 ? 'text-gray-500' : ''">{{ item.name }}</view>
              <!-- debug id -->
              <!-- <view>{{ item.id }}</view> -->
              <view v-if="item.extraPrice && item.extraPrice !== 0" class="bg-gray-200 w-[0.5px] h-4"></view>
              <view v-if="item.extraPrice && item.extraPrice !== 0" class="text-sm text-red-500"
                :class="item.stock === 0 ? 'text-red-300' : ''">
                <text class="text-xs">￥</text>
                <text>{{ item.extraPrice }}</text>
              </view>
              <view v-if="item.stock === 0">
                <text class="text-xs text-gray-400">(已售罄)</text>
              </view>
            </view>
          </view>
        </view>
        <view class="py-4 border-b border-gray-100">
          <view class="mb-1">数量</view>
          <uni-number-box v-model="count" :min="1" :max="99"></uni-number-box>
        </view>
      </scroll-view>
    </view>
    <view>
      <button class="clear-button bg-primary text-white flex items-center justify-center text-base"
        :style="{ height: (systemInfo.statusBarHeight - 10) + 'px' }" @click="confirm">确认</button>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.active {
  @apply text-primary border-primary;
}
</style>