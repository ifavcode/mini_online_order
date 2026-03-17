<template>
  <view class="min-h-screen bg-gray-50 pb-32">
    <!-- 外送/自提切换 -->
    <view class="bg-white flex">
      <view class="flex-1 text-center py-2 text-base"
        :class="orderType === 'dinein' ? 'text-primary font-medium' : 'text-gray-400'" @click="orderType = 'dinein'">
        {{ OrderTipExplain[activeTip].orderTypeOne }}
      </view>
      <view class="flex-1 text-center py-2 text-base"
        :class="orderType === 'pickup' ? 'text-primary font-medium' : 'text-gray-400'" @click="orderType = 'pickup'">
        {{ OrderTipExplain[activeTip].orderTypeTwo }}
      </view>
    </view>

    <!-- 商家信息卡片 -->
    <view class="bg-white mt-2 px-4">
      <!-- 商家地址 -->
      <view class="flex items-center justify-between py-4 border-b border-gray-100" @click="handleAddressClick">
        <view class="flex-1">
          <view class="text-sm text-gray-600 mb-1">商家地址</view>
          <view class="text-base text-gray-800">{{ businessInfo?.shopLocation }}</view>
        </view>
        <uni-icons type="right" size="16" color="#999"></uni-icons>
      </view>

      <!-- 制作时间 -->
      <view class="flex items-center justify-between py-4 border-b border-gray-100" @click="handleTimeClick">
        <view class="text-base text-gray-800">{{ OrderTipExplain[activeTip].orderTimeTip }}</view>
        <view class="flex items-center">
          <text class="text-sm text-gray-400 mr-2">{{ pickupTime || '请选择时间' }}</text>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
      </view>

      <!-- 联系方式 -->
      <view class="flex items-center justify-between py-4">
        <view class="text-base text-gray-800">联系方式</view>
        <input type="text"
          class="py-2 px-3 rounded border border-gray-200 text-base outline-none2 text-right placeholder:text-right placeholder:text-gray-400 placeholder:text-sm "
          placeholder-class="input-ph" style="width: 120px;" placeholder="选填" v-model="phone" />
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="bg-white mt-2 px-4 pb-4">
      <view class="text-lg font-medium text-gray-800 py-4">共{{ totalItems }}件商品</view>

      <view v-for="(item, index) in choosedGoods" :key="index" class="py-3"
        :class="{ 'border-b border-gray-100': index < choosedGoods.length - 1 }">
        <view class="flex items-start">
          <!-- 商品图片 -->
          <image :src="item.imgUrl || '/static/no_img.jpg'" class="w-16 h-16 rounded-lg mr-3 flex-shrink-0"
            mode="aspectFit"></image>

          <!-- 商品信息 -->
          <view class="flex-1 min-w-0">
            <view class="text-base text-gray-800 mb-1">{{ item.name }}</view>
            <view v-if="item.description" class="text-xs text-gray-400 mb-2">{{ item.description }}</view>

            <!-- 规格信息 -->
            <view v-if="item.hasSpec && item.specGroups && item.specGroups.length > 0"
              class="text-xs text-gray-500 mb-1">
              <text v-for="(group, gIndex) in item.specGroups" :key="gIndex">
                <text v-if="gIndex > 0">; </text>
                {{ group.name }}:
                <text v-for="(spec, sIndex) in group.specItems" :key="spec.id">
                  <text v-if="sIndex > 0">+</text>
                  {{ spec.name }}
                  <text v-if="spec.extraPrice > 0">(+¥{{ spec.extraPrice }})</text>
                </text>
              </text>
            </view>
          </view>

          <!-- 数量和价格 -->
          <view class="flex items-center ml-2 flex-shrink-0">
            <text class="text-sm text-gray-600 mr-4">x{{ item.count }}</text>
            <text class="text-lg text-gray-800 font-medium">¥{{ calculateItemPrice(item) }}</text>
          </view>
        </view>
      </view>

      <!-- 合计 -->
      <view class="flex justify-end items-center pt-3 border-t border-gray-100 mt-2">
        <text class="text-sm text-gray-600 mr-2">合计</text>
        <text class="text-xl text-gray-800 font-medium">¥{{ totalPrice }}</text>
      </view>
    </view>

    <!-- 备注 -->
    <view class="bg-white mt-2 px-4 py-4 flex items-center justify-between" @click="handleRemarkClick">
      <view class="text-base text-gray-800">备注</view>
      <view class="flex items-center">
        <text class="text-sm text-gray-400 mr-2">{{ remark || '(选填)' }}</text>
        <uni-icons type="right" size="16" color="#999"></uni-icons>
      </view>
    </view>

    <!-- 底部支付栏 -->
    <view
      class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-4 py-3 flex items-center justify-between safe-area-inset-bottom">
      <view class="flex items-center">
        <text class="text-sm text-gray-600 mr-2">合计</text>
        <text class="text-2xl text-gray-800 font-bold">
          <text class="text-sm text-red-500">¥</text>
          <text class="text-red-500">{{ totalPrice }}</text>
        </text>
      </view>
      <view class="w-48">
        <button class="bg-primary text-white px-12 py-3 rounded-full text-base " @click="handlePay">
          去支付
        </button>
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useGoodStore } from '../../stores/good'
import { useSystemStore } from '../../stores/system'
import { createOrders, postWxPay } from '../../api/orders'
import { calcGoodsTotalPrice } from '../../utils/index'
import { useUserStore } from '../../stores/user'
import { leftLikeDict } from '../../api/dict'
import OrderTipExplain from '../../utils/OrderTipExplain'

const envTmplIdStr = import.meta.env.VITE_TMPL_ID
const tmplIds = envTmplIdStr.split(',')
console.log(tmplIds)

// 获取商品store
const goodStore = useGoodStore()
const { choosedGoods } = storeToRefs(goodStore)
const systemStore = useSystemStore()
const { businessInfo } = storeToRefs(systemStore)
const userStore = useUserStore()
const activeTip = (systemStore.orderTip || 0) * 1

// 响应式数据
const orderType = ref('dinein') // 'dinein' or 'pickup'
const pickupTime = ref(OrderTipExplain[activeTip].orderTimeParam || '立刻开始')
const phone = ref('')
const remark = ref('')

// 计算单个商品的总价（基础价格 + 规格加价）
const calculateItemPrice = (item) => {
  let price = item.basePrice

  // 如果有规格，计算规格加价
  if (item.hasSpec && item.specGroups && item.specGroups.length > 0) {
    item.specGroups.forEach(group => {
      if (group.specItems && group.specItems.length > 0) {
        group.specItems.forEach(spec => {
          if (spec.extraPrice) {
            price += spec.extraPrice
          }
        })
      }
    })
  }

  return (price * item.count).toFixed(2)
}

// 计算总商品数量
const totalItems = computed(() => {
  return choosedGoods.value.reduce((sum, item) => sum + item.count, 0)
})

// 计算总价
const totalPrice = computed(() => {
  return choosedGoods.value.reduce((sum, item) => {
    let itemPrice = item.basePrice
    // 计算规格加价
    if (item.hasSpec && item.specGroups && item.specGroups.length > 0) {
      item.specGroups.forEach(group => {
        if (group.specItems && group.specItems.length > 0) {
          group.specItems.forEach(spec => {
            if (spec.extraPrice) {
              itemPrice += spec.extraPrice
            }
          })
        }
      })
    }

    return sum + (itemPrice * item.count)
  }, 0).toFixed(2)
})

const handleAddressClick = () => {
  uni.openLocation({
    latitude: businessInfo.value.chooseLngLat.lat,
    longitude: businessInfo.value.chooseLngLat.lng,
    name: businessInfo.value.shopName,
    address: businessInfo.value.shopLocation,
  })
}

const handleTimeClick = () => {
  uni.showActionSheet({
    itemList: [OrderTipExplain[activeTip].orderTimeParam, '30分钟后', '1小时后', '2小时后'],
    success: (res) => {
      const times = [OrderTipExplain[activeTip].orderTimeParam, '30分钟后', '1小时后', '2小时后']
      pickupTime.value = times[res.tapIndex]
    }
  })
}

const handleRemarkClick = () => {
  console.log('添加备注')
  // 跳转到备注页面或打开输入框
  uni.showModal({
    title: '备注',
    editable: true,
    placeholderText: '请输入备注信息',
    content: remark.value,
    success: (res) => {
      if (res.confirm) {
        remark.value = res.content
      }
    }
  })
}

const requestSubscribeMessageFunc = async () => {
  return new Promise((resolve, reject) => {
    uni.requestSubscribeMessage({
      tmplIds,
      success() {
        resolve()
      },
      fail() {
        reject()
      }
    })
  })
}

const handlePay = async () => {
  if (!pickupTime.value) {
    uni.showToast({
      title: '请选择自提时间',
      icon: 'none'
    })
    return
  }

  if (choosedGoods.value.length === 0) {
    uni.showToast({
      title: '请先选择商品',
      icon: 'none'
    })
    return
  }

  const orderItems = choosedGoods.value.reduce((pre, goods) => {
    pre.push({
      unitPrice: goods.basePrice,
      subtotal: calcGoodsTotalPrice(goods),
      specGroupJson: JSON.stringify(goods.specGroups),
      count: goods.count * 1,
      goods: {
        id: goods.id
      }
    })
    return pre
  }, [])
  try {
    const res = await createOrders({
      totalAmount: totalPrice.value,
      orderType: orderType.value,
      pickupTime: pickupTime.value,
      phone: phone.value,
      remark: remark.value,
      orderItems,
      table: {
        tableCode: (userStore.tableCode && userStore.tableCode != '') ? userStore.tableCode : null
      }
    })
    if (res.success) {
      const paySwitchRes = await leftLikeDict('pay_switch')
      if (paySwitchRes && paySwitchRes[0]) {
        if (paySwitchRes[0].dictValue === "0") {
          // 小程序后台关闭了支付
          uni.redirectTo({
            url: '/pages/order/details?id=' + res.orderId
          })
          goodStore.clearChoosedGoods()
          return
        }
      }
      if (totalPrice.value > 0) {
        const payResult = await postWxPay(res.wxOrderNo)
        uni.requestPayment({
          provider: 'wxpay',
          timeStamp: payResult.timeStamp,
          nonceStr: payResult.nonceStr,
          appId: payResult.appId,
          package: payResult.packageValue,
          signType: 'MD5',
          paySign: payResult.paySign,
          success: async function (payRes) {
            uni.showToast({
              icon: 'none',
              title: '支付成功'
            })
            await requestSubscribeMessageFunc()
            setTimeout(() => {
              uni.redirectTo({
                url: '/pages/order/details?id=' + res.orderId
              })
              goodStore.clearChoosedGoods()
            }, 250);
            console.log('success:' + JSON.stringify(payRes));
          },
          fail: function (err) {
            if (res.orderId) {
              uni.redirectTo({
                url: '/pages/order/details?id=' + res.orderId
              })
              goodStore.clearChoosedGoods()
              return
            }
            uni.showToast({
              icon: 'none',
              title: '支付失败，请重试'
            })
            console.log('fail:' + JSON.stringify(err));
          }
        });
      } else {
        uni.showToast({
          icon: 'none',
          title: '支付成功'
        })
        await requestSubscribeMessageFunc()
        setTimeout(() => {
          uni.redirectTo({
            url: '/pages/order/details?id=' + res.orderId
          })
          goodStore.clearChoosedGoods()
        }, 250);
      }
    } else {
      uni.showToast({
        title: '支付失败! 请重试',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.showToast({
      title: error,
      icon: 'none'
    })
  }
}

onMounted(() => {
  if (userStore.user.phoneNumber) {
    phone.value = userStore.user.phoneNumber
  }
})
</script>

<style scoped>
/* 如果需要额外的样式可以在这里添加 */
.safe-area-inset-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>

<style>
.input-ph {
  @apply text-sm text-gray-400
}
</style>