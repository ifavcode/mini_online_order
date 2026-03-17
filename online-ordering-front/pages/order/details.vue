<script setup>
import { computed, onMounted, ref } from 'vue';
import { checkWxPay, getOrdersById, postWxPay } from '../../api/orders';
import { useSystemStore } from '../../stores/system';
import { storeToRefs } from 'pinia';
import dayjs from 'dayjs'
import OrderTipExplain from '../../utils/OrderTipExplain'

import { onLoad } from '@dcloudio/uni-app'
import Constant from '../../utils/Constant';
let orderId = null;

onLoad((option) => {
  orderId = option.id
  getOrdersByIdFunc();
})

const isPay = computed(() => {
  return orderDetails.value.status === Constant.ORDERS_ENUM.PAID || orderDetails.value.status === Constant.ORDERS_ENUM.COMPLETED
})

const isPendingPay = computed(() => {
  return orderDetails.value.status === Constant.ORDERS_ENUM.PENDING_PAYMENT
})

const isCancel = computed(() => {
  return orderDetails.value.status === Constant.ORDERS_ENUM.CANCELED
})

const orderDetails = ref({});
const systemStore = useSystemStore()
const activeTip = (systemStore.orderTip || 0) * 1

const { businessInfo } = storeToRefs(systemStore)
const remainTime = ref({
  hour: 0,
  minute: 0,
  second: 0
})

function calcRemainTime() {
  if (!orderDetails.value.createdAt) return { hour: 0, minute: 0, second: 0 };
  const createdTime = dayjs(orderDetails.value.createdAt);
  const expireTime = createdTime.add(1, 'hour');
  const now = dayjs();
  let diff = expireTime.diff(now, 'second');
  if (diff <= 0) {
    return { hour: 0, minute: 0, second: 0 };
  }
  const hour = Math.floor(diff / 3600);
  diff %= 3600;
  const minute = Math.floor(diff / 60);
  const second = diff % 60;
  return { hour, minute, second };
}

async function getOrdersByIdFunc() {
  orderDetails.value = await getOrdersById(orderId);
  const back = calcRemainTime()
  remainTime.value.hour = back.hour
  remainTime.value.minute = back.minute
  remainTime.value.second = back.second
  if (orderDetails.value.status !== Constant.ORDERS_ENUM.PAID) {
    check()
  }
}

// 解析规格JSON
function parseSpecGroup(specGroupJson) {
  try {
    return JSON.parse(specGroupJson);
  } catch (e) {
    return [];
  }
}

// 格式化规格显示
function formatSpecs(specGroupJson) {
  const specs = parseSpecGroup(specGroupJson);
  if (!specs || specs.length === 0) return '';

  return specs.map(group => {
    const selectedItems = group.specItems.map(item => item.name).join('、');
    return selectedItems;
  }).join(' | ');
}

// 计算订单总数量
function getTotalQuantity() {
  if (!orderDetails.value.orderItems) return 0;
  return orderDetails.value.orderItems.length;
}

function oneMoreOrder() {
  uni.navigateTo({
    url: '/pages/index/index'
  })
}

function callPhone() {
  const phoneNumber = businessInfo.value.shopPhone
  if (!phoneNumber) {
    uni.showToast({
      icon: 'none',
      title: '商家未设置联系方式',
    })
  }
  uni.makePhoneCall({
    phoneNumber
  })
}

const handleAddressClick = () => {
  uni.openLocation({
    latitude: businessInfo.value.chooseLngLat.lat,
    longitude: businessInfo.value.chooseLngLat.lng,
    name: businessInfo.value.shopName,
    address: businessInfo.value.shopLocation,
  })
}

function copyWxOrderNo() {
  if (!orderDetails.value.wxOrderNo) {
    uni.showToast({
      icon: 'none',
      title: '订单号不存在',
    });
    return;
  }
  uni.setClipboardData({
    data: String(orderDetails.value.wxOrderNo),
    success: () => {
      uni.showToast({
        icon: 'none',
        title: '订单号已复制',
      });
    }
  });
}

function timeup() {
  if (orderDetails.value.status == Constant.ORDERS_ENUM.PENDING_PAYMENT) {
    orderDetails.value.status = Constant.ORDERS_ENUM.CANCELED
  }
}

async function handlePay() {
  const payResult = await postWxPay(orderDetails.value.wxOrderNo)
  uni.requestPayment({
    provider: 'wxpay',
    timeStamp: payResult.timeStamp,
    nonceStr: payResult.nonceStr,
    appId: payResult.appId,
    package: payResult.packageValue,
    signType: 'MD5',
    paySign: payResult.paySign,
    success: function (payRes) {
      uni.showToast({
        icon: 'none',
        title: '支付成功'
      })
      orderDetails.value.status = Constant.ORDERS_ENUM.PAID
    },
    fail: function (err) {
      uni.showToast({
        icon: 'none',
        title: '支付失败，请重试'
      })
      console.log('fail:' + JSON.stringify(err));
    }
  });
}

async function check() {
  const result = await checkWxPay(orderDetails.value.wxOrderNo)
  if (!result) {
    uni.showToast({ title: '未查询到支付结果', icon: 'none' })
  } else {
    getOrdersByIdFunc()
  }
}

</script>

<template>
  <view class="min-h-screen bg-gray-50">

    <view class="p-4 space-y-3">
      <!-- 取餐号 -->
      <view class="bg-white rounded-lg p-4 shadow-sm" v-if="isPay">
        <view class="flex items-center justify-between">
          <view>
            <text class="text-2xl font-bold">{{ OrderTipExplain[activeTip].orderNo }} {{ orderDetails.orderNo }}</text>
            <text class="text-sm text-gray-500 block mt-1">{{ OrderTipExplain[activeTip].orderTip }}</text>
          </view>
          <view class="text-right">
            <text class="text-blue-500 text-sm" @click="oneMoreOrder">再来一单</text>
          </view>
        </view>
      </view>

      <!-- 餐厅信息 -->
      <view class="bg-white rounded-lg p-4 shadow-sm">
        <view class="flex items-center justify-between">
          <view class="flex-1" v-if="isPay">
            <text class="text-lg font-semibold block" v-if="orderDetails.table">{{ orderDetails.table?.tableName
            }}</text>
            <text class="text-sm text-gray-500 block mt-1" v-if="orderDetails.table">桌号: {{
              orderDetails.table?.tableCode }}</text>
            <text class="text-lg font-semibold block" v-if="!orderDetails.table">商家联系方式</text>
          </view>
          <view class="flex-1" v-if="isPendingPay">
            <view class="flex items-end text-sm">
              <text>剩余时间：</text>
              <uni-countdown :show-day="false" :hour="remainTime.hour" :minute="remainTime.minute"
                :second="remainTime.second" :show-colon="false" @timeup="timeup"></uni-countdown>
            </view>
            <view class="flex gap-2 items-end">
              <text class="text-lg font-semibold block">等待付款</text>
              <text class="text-sm text-gray-400 block translate-y-[-2px]" @click="check">已支付?</text>
            </view>
          </view>
          <view class="flex-1" v-if="isCancel">
            <text class="text-lg font-semibold block">付款超时，已取消</text>
          </view>
          <view class="flex space-x-3">
            <view class="w-10 h-10 rounded-full flex items-center justify-center">
              <uni-icons type="phone" size="24" @click="callPhone"></uni-icons>
            </view>
            <view class="w-10 h-10 rounded-full flex items-center justify-center">
              <uni-icons type="location" size="24" @click="handleAddressClick"></uni-icons>
            </view>
          </view>
        </view>
        <button class="mt-2 bg-primary text-white rounded-full text-sm px-4 py-2" @click="handlePay"
          v-if="isPendingPay">
          立即支付
        </button>
      </view>

      <!-- 商品列表 -->
      <view class="bg-white rounded-lg shadow-sm">
        <view v-for="(item, index) in orderDetails.orderItems" :key="item.id" class="flex items-start p-4"
          :class="{ 'border-b border-gray-100': index < orderDetails.orderItems.length - 1 }">
          <image :src="item.goods.imgUrl || '/static/no_img.jpg'" class="w-16 h-16 rounded-lg mr-3" mode="aspectFill" />

          <view class="flex-1">
            <text class="text-base font-medium block">{{ item.goods.name }}</text>
            <text v-if="formatSpecs(item.specGroupJson)" class="text-xs text-gray-500 block mt-1">
              {{ formatSpecs(item.specGroupJson) }}
            </text>
          </view>

          <view class="text-right ml-3">
            <text class="text-base font-semibold text-black">¥{{ item.subtotal }}</text>
            <text class="text-xs text-gray-400 block mt-1">x{{ item.count }}份</text>
          </view>
        </view>

        <view class="p-4" v-if="!orderDetails.orderItems || orderDetails.orderItems.length === 0">
          <text class="text-gray-600">商品已被删除!</text>
        </view>

        <!-- 展开按钮 -->
        <view class="py-3 text-center border-t border-gray-100"
          v-if="orderDetails.orderItems && orderDetails.orderItems.length > 2">
          <text class="text-sm text-gray-500">展开 (共{{ getTotalQuantity() }}件) ▼</text>
        </view>

        <!-- 总计 -->
        <view class="p-4 border-t border-gray-100">
          <view class="flex justify-between items-center">
            <text class="text-base">共 {{ getTotalQuantity() }} 份</text>
            <view>
              <text class="text-sm text-gray-500 mr-2">总价</text>
              <text class="text-xl font-bold text-red-500">¥{{ orderDetails.totalAmount }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 就餐信息 -->
      <view class="bg-white rounded-lg p-4 shadow-sm">
        <text class="text-base font-semibold block mb-3"></text>

        <view class="space-y-3">
          <view class="flex justify-between items-center py-2">
            <text class="text-gray-600">{{ OrderTipExplain[activeTip].orderMethod }}</text>
            <text class="text-gray-900">{{ orderDetails.orderType === 'dinein' ?
              OrderTipExplain[activeTip].orderResultTypeOne : OrderTipExplain[activeTip].orderResultTypeTwo }}</text>
          </view>

          <view class="flex justify-between items-center py-2">
            <text class="text-gray-600">{{ OrderTipExplain[activeTip].orderTime }}</text>
            <text class="text-gray-900">{{ OrderTipExplain[activeTip].orderTimeParam }}</text>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="bg-white rounded-lg p-4 shadow-sm">
        <text class="text-base font-semibold block mb-3">订单信息</text>

        <view class="space-y-3">
          <view class="flex justify-between items-center py-2">
            <text class="text-gray-600">支付方式</text>
            <text class="text-gray-900">在线支付 ¥{{ orderDetails.totalAmount }}</text>
          </view>

          <view class="flex justify-between items-center py-2" v-if="isPay">
            <text class="text-gray-600">{{ OrderTipExplain[activeTip].orderNo }}</text>
            <text class="text-gray-900">{{ orderDetails.orderNo }}</text>
          </view>

          <view class="flex justify-between items-center py-2">
            <text class="text-gray-600">订单号</text>
            <view class="flex items-center">
              <text class="text-gray-900 text-xs mr-2">{{ orderDetails.wxOrderNo }}</text>
              <text class="text-blue-500 text-sm" @click="copyWxOrderNo">复制</text>
            </view>
          </view>

          <view class="flex justify-between items-center py-2">
            <text class="text-gray-600">下单时间</text>
            <text class="text-gray-900">{{ orderDetails.createdAt }}</text>
          </view>
        </view>
      </view>

      <!-- 底部提示 -->
      <view class="text-center py-6" v-if="isPay">
        <text class="text-sm text-gray-400">如需退款，请联系店员或致电门店</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.space-y-3>view:not(:last-child) {
  margin-bottom: 0.75rem;
}

.space-x-3>view:not(:last-child) {
  margin-right: 0.75rem;
}
</style>