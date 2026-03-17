<script setup>
import { computed, nextTick, ref } from 'vue';
import { useSystemStore } from '../../stores/system';
import { useGoodStore } from '../../stores/good';
import Constant from '../../utils/Constant';
import BuyCart from '../../components/buyCart.vue';
import { getCurrentInstance } from 'vue';
import { onMounted } from 'vue';
import { getAllCategory, getCategoryPage } from '../../api/category';
import { storeToRefs } from 'pinia';
import { useUserStore } from '../../stores/user';
import { getEnvType } from '../../utils';

const instance = getCurrentInstance();
const query = uni.createSelectorQuery().in(instance.proxy);
const systemStore = useSystemStore()
const { safeArea, systemInfo } = systemStore
const goodStore = useGoodStore()
const { choosedGoods } = storeToRefs(goodStore)
const extraBottomHeight = 20
const activeMenuIndex = ref(0)
const activeDisplayMenuIndex = ref(0)
const loading = ref(false)
const userStore = useUserStore()
const env = getEnvType()

const remainHeight = computed(() => {
  //减去business-info的高度
  return safeArea.height - 206 -(env === 'h5' ? 60 : 0)
})

const remainContentHeight = computed(() => {
  //减去business-info的高度
  return safeArea.height - 206 - 36 - 37 - extraBottomHeight - (env === 'h5' ? 60 : 0)
})

const menuType = ref([])

async function getAllCategoryFunc() {
  try {
    loading.value = true
    let res
    if (goodStore.menuTypeStorage.length === 0) {
      res = await getAllCategory()
    } else {
      res = goodStore.menuTypeStorage
    }
    initGoodsBound()
    goodStore.menuTypeStorage = res
    menuType.value = res
  } catch (error) {
    console.log(error)
  } finally {
    loading.value = false
  }
}

function chooseGoodDetails(goodDetails) {
  goodStore.setGoodDetails(goodDetails)
  goodStore.togglePopShow(true)
  if (goodDetails.hasSpec === 1) {
    const map = new Map()
    for (let cur of goodDetails[Constant.SPECIFICATION]) {
      if (cur.selectType === 'more') {
        // TODO 默认选第一个
        if (!map.has(cur.specItems[0].id)) {
          // map.set(cur.id, [cur.specItems[0].id])
          map.set(cur.id, [])
        }
      } else if (cur.selectType === 'one') {
        // TODO 默认选第一个
        if (!map.has(cur.specItems[0].id)) {
          map.set(cur.id, cur.specItems[0].id)
        }
      }
    }
    goodStore.choosedSpec = map

  }
}

function chooseGoodOne(goodDetails) {
  const tmpDetails = JSON.parse(JSON.stringify(goodDetails))
  tmpDetails['count'] = 1
  tmpDetails[Constant.SPECIFICATION] = [] // 无规格商品直接清空，防止数据库中仍存在规格
  goodStore.addChoosedGoods(tmpDetails)
}

// 改变active索引，scroll-into-view自动监听，并跳转👌
function scrollToMenuGood(index) {
  activeMenuIndex.value = index
  activeDisplayMenuIndex.value = index
}

function getClientRectById(id) {
  if (env !== 'h5') {
    return new Promise((resolve, reject) => {
      query
        .select(`#${id}`)
        .boundingClientRect((data) => {
          resolve(data) // left top right bottom
        })
        .exec();
    })
  } else {
    return new Promise((resolve, reject) => {
      const el = document.getElementById(id)
      if (!el) {
        reject(new Error(`Web端未找到ID为「${id}」的元素`));
        return;
      }
      const rect = el.getBoundingClientRect();
      resolve({
        top: rect.top || 0,
        left: rect.left || 0,
        right: rect.right || 0,
        bottom: rect.bottom || 0
      });
    })
  }
}

const menuScrollTopArr = []
async function initGoodsBound() {
  if (env === 'h5') {
    await nextTick()
  }
  const { top: scrollTop } = await getClientRectById("contentScrollView")
  for (let i = 0; i < menuType.value.length; i++) {
    const { top } = await getClientRectById(Constant.MENU_TYPE_ID + i)
    menuScrollTopArr.push(top - scrollTop)
  }
}

function scroll(e) {
  // 不加节流更灵敏
  let idx = 0
  for (let i = 0; i < menuScrollTopArr.length; i++) {
    if (e.target.scrollTop >= (menuScrollTopArr[i] - 20)) {
      idx = i
    } else {
      break
    }
  }
  activeDisplayMenuIndex.value = idx
}

function countChange(val, id) {
  if (val === 0) {
    choosedGoods.value = choosedGoods.value.filter(v => v.id !== id)
    if (choosedGoods.value.length === 0) {
      goodStore.toggleBuyCartShow(false)
    }
  }
}

function gotoMyOrder() {
  uni.navigateTo({
    url: '/pages/order/profile'
  })
}

function previewImage(url) {
  uni.previewImage({
    urls: [url]
  })
}

const refresherLoading = ref(false)
async function onRefresh() {
  goodStore.menuTypeStorage = []
  try {
    refresherLoading.value = true
    await getAllCategoryFunc()
  } catch (error) {
    console.error(error);
  } finally {
    refresherLoading.value = false
  }
}

function updateUser() {
  uni.navigateTo({
    url: '/pages/profile/update'
  })
}

onMounted(() => {
  getAllCategoryFunc()
})

</script>

<template>
  <view :style="{ height: remainHeight + 'px' }">
    <view class="flex justify-between w-full px-8 py-2">
      <text class="text-gray-500">
        <text v-if="userStore.tableName && userStore.tableName != ''">{{ userStore.tableName + ',' }}</text>
        <text v-if="userStore.user.nickname && userStore.user.nickname != '' && userStore.user.nickname != '未命名用户'"
          @click="updateUser">
          <text class="text-primary">{{ userStore.user.nickname }}</text>
          <text>,</text>
        </text>
        欢迎下单哦
      </text>
      <view class="text-primary flex items-center gap-1" @click="gotoMyOrder">
        <text class="iconfont">&#xe8ae;</text>
        <text>我的订单</text>
      </view>
    </view>
    <view class="w-full flex border-t border-t-gray-100">
      <scroll-view :scroll-y="true" class="w-24" :style="{ height: remainContentHeight + 'px' }">
        <view class="w-full border-r border-gray-100" :style="{ height: remainContentHeight + 'px' }">
          <!-- <view v-if="loading" class="flex flex-col items-center justify-center h-full">
            <view class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-primary mb-2"></view>
            <text class="text-gray-500">加载中...</text>
          </view> -->
          <view v-for="(type, index) in menuType" :key="index"
            class="h-10 flex items-center justify-center text-gray-600"
            :class="index === activeDisplayMenuIndex ? 'menu-active' : ''" @click="scrollToMenuGood(index)">
            <text class="">{{ type.name }}</text>
          </view>
        </view>
      </scroll-view>
      <scroll-view id="contentScrollView" :scroll-y="true" class="flex-1 p-2"
        :style="{ height: remainContentHeight + 'px' }" :scroll-into-view="`${Constant.MENU_TYPE_ID}${activeMenuIndex}`"
        :scroll-with-animation="true" @scroll="scroll" refresher-enabled @refresherrefresh="onRefresh"
        :refresher-triggered="refresherLoading">
        <view v-if="loading" class="flex flex-col items-center justify-center h-full">
          <view class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-primary mb-2"></view>
          <text class="text-gray-500">加载中...</text>
        </view>
        <view v-for="(type, index) in menuType" :key="index" :class="index > 0 ? 'mt-4' : ''">
          <text class="text-gray-600" :id="`${Constant.MENU_TYPE_ID}${index}`">{{ type.name }}</text>
          <view class="">
            <view v-for="good in type.goodsList" :key="good.id" class="flex gap-2 mt-2">
              <view @click="previewImage(good.imgUrl)" v-if="good.imgUrl">
                <image :src="good.imgUrl" mode="aspectFit" class="size-24"></image>
              </view>
              <image v-else src="../../static/no_img.jpg" mode="aspectFit" class="size-24" />
              <view class="flex flex-col justify-between flex-1 pr-2">
                <view class="flex flex-col gap-2">
                  <view>
                    <view>{{ good.name }}</view>
                    <view class="text-gray-500 text-sm">{{ good.description }}</view>
                    <view class="text-gray-500 text-sm" v-if="good.stock >= 0">库存:{{ good.stock }}</view>
                  </view>
                  <text class="text-red-500 text-sm">
                    <text class="text-xs">￥</text>
                    <text>{{ good.basePrice }}</text>
                    <text class="ml-1">{{ (good.hasSpec === 1 ? '起' : '') }}</text>
                  </text>
                </view>
                <view class="flex justify-end">
                  <view v-if="good.stock === 0">
                    <view class="text-sm bg-gray-400 text-white w-fit px-2  rounded-md">
                      已售罄
                    </view>
                  </view>
                  <view v-else>
                    <view v-if="good.hasSpec === 0">
                      <view v-if="!choosedGoods.find(v => v.id === good.id)"
                        class="text-sm bg-primary text-white w-fit px-1 rounded-md" @click="chooseGoodOne(good)">
                        <uni-icons type="plusempty" size="14" color="white"></uni-icons>
                      </view>
                      <view v-else>
                        <uni-number-box v-model="choosedGoods.find(v => v.id === good.id).count" :min="0" :max="99"
                          @change="(val) => countChange(val, good.id)"></uni-number-box>
                      </view>
                    </view>
                    <view v-else-if="good.hasSpec === 1" class="text-sm bg-primary text-white w-fit px-2  rounded-md"
                      @click="chooseGoodDetails(good)">
                      选规格
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    <buy-cart />
  </view>
</template>

<style lang="css" scoped>
.menu-active {
  @apply bg-primary text-white shadow-md;
}
</style>