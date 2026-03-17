<script setup lang="js">
import { onMounted, ref } from 'vue';
import { getOrdersPage } from '../../api/orders';

const orderList = ref([]);
const currentPage = ref(0);
const pageSize = ref(10);
const totalPages = ref(0);
const loading = ref(false);
const refreshing = ref(false);
const noMore = ref(false);

// 获取订单列表
async function getOrdersPageFunc(isRefresh = false) {
  if (loading.value) return;

  loading.value = true;

  try {
    console.log({
      page: isRefresh ? 0 : currentPage.value,
      size: pageSize.value
    });

    const res = await getOrdersPage({
      page: isRefresh ? 0 : currentPage.value,
      size: pageSize.value
    });

    if (isRefresh) {
      orderList.value = res.content;
      currentPage.value = 0;
      noMore.value = false;
    } else {
      orderList.value = [...orderList.value, ...res.content];
    }

    totalPages.value = res.totalPages;

    // 判断是否还有更多数据
    if (res.last) {
      noMore.value = true;
    }
  } catch (error) {
    console.error('获取订单列表失败:', error);
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
}

// 下拉刷新
function onRefresh() {
  refreshing.value = true;
  getOrdersPageFunc(true);
}

// 上拉加载更多
function onLoadMore() {
  if (noMore.value || loading.value) return;

  currentPage.value++;
  getOrdersPageFunc(false);
}

// 计算订单总数量
function getTotalQuantity(orderItems) {
  if (!orderItems) return 0;
  return orderItems.reduce((sum, item) => sum + item.count, 0);
}

// 获取订单状态文本
function getStatusText(status) {
  const statusMap = {
    1: '待支付',
    2: '已支付',
    3: '已完成',
    4: '已取消',
    5: '已退款'
  };
  return statusMap[status] || '未知';
}

// 获取订单状态颜色
function getStatusColor(status) {
  const colorMap = {
    1: 'text-blue-500',
    2: 'text-purple-500',
    3: 'text-green-500',
    4: 'text-gray-500',
  };
  return colorMap[status] || 'text-gray-500';
}

// 跳转到订单详情
function goToOrderDetail(orderId) {
  uni.navigateTo({
    url: `/pages/order/details?id=${orderId}`
  });
}

onMounted(() => {
  getOrdersPageFunc(true);
});
</script>

<template>
  <view class="min-h-screen bg-gray-50">
    <!-- 订单列表 -->
    <scroll-view scroll-y class="h-screen" @scrolltolower="onLoadMore" refresher-enabled
      :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="p-4 pb-20">
        <!-- 订单卡片 -->
        <view v-for="order in orderList" :key="order.id" class="bg-white rounded-lg shadow-sm mb-3 overflow-hidden">
          <!-- 订单头部 -->
          <view class="flex items-center justify-between px-4 py-3 border-b border-gray-100">
            <view class="flex items-center">
              <text class="text-sm text-gray-600 mr-2">{{ order.createdAt }}</text>
              <text class="text-xs px-2 py-1 rounded" :class="getStatusColor(order.status)">
                {{ getStatusText(order.status) }}
              </text>
            </view>
          </view>
          <!-- <view class="px-4 pt-2">
            <text class="text-sm text-gray-500">订单号: {{ order.wxOrderNo }}</text>
          </view> -->

          <!-- 商品列表 -->
          <view @click="goToOrderDetail(order.id)">
            <view v-for="(item, index) in order.orderItems.slice(0, 3)" :key="item.id"
              class="flex items-center px-4 py-3">
              <image v-if="item.goods.imgUrl" :src="item.goods.imgUrl" class="w-16 h-16 rounded-lg mr-3"
                mode="aspectFill" />
              <view v-else class="w-16 h-16 rounded-lg mr-3 bg-gray-100 flex items-center justify-center">
                <text class="text-2xl">🍜</text>
              </view>

              <view class="flex-1 min-w-0">
                <text class="text-base font-medium block truncate">{{ item.goods.name }}</text>
                <text class="text-xs text-gray-500 block mt-1">x{{ item.count }}</text>
              </view>

              <text class="text-base font-semibold text-gray-900 ml-3">¥{{ item.subtotal }}</text>
            </view>

            <view class="p-4" v-if="!order.orderItems || order.orderItems.length === 0">
              <text class="text-gray-600">商品已被删除!</text>
            </view>

            <!-- 更多商品提示 -->
            <view v-if="order.orderItems.length > 3" class="px-4 pb-2">
              <text class="text-xs text-gray-400">还有{{ order.orderItems.length - 3 }}件商品...</text>
            </view>
          </view>

          <!-- 订单底部 -->
          <view class="px-4 py-3 bg-gray-50 flex items-center justify-between bg-white border-t border-gray-100">
            <view>
              <text class="text-sm text-gray-600">共 {{ getTotalQuantity(order.orderItems) }} 件</text>
              <text class="text-base font-bold text-red-500 ml-3">¥{{ order.totalAmount }}</text>
            </view>

            <view class="flex space-x-2">
              <!-- TODO -->
              <!-- <button class="px-4 py-1 text-sm border border-gray-300 rounded-full bg-white">
                再来一单
              </button> -->
              <button class="py-1 text-sm border border-primary text-primary rounded-full bg-white"
                @click.stop="goToOrderDetail(order.id)">
                订单详情
              </button>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="orderList.length === 0 && !loading" class="flex flex-col items-center justify-center py-20">
          <text class="text-6xl mb-4">📋</text>
          <text class="text-gray-400 text-base">暂无订单</text>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading && !refreshing" class="flex justify-center py-4">
          <uni-load-more status="loading" />
        </view>

        <!-- 没有更多 -->
        <view v-if="noMore && orderList.length > 0" class="flex justify-center py-4">
          <text class="text-sm text-gray-400">没有更多订单了</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.space-x-2>button:not(:last-child) {
  margin-right: 0.5rem;
}

scroll-view {
  height: calc(100vh - 44px);
}

button {
  border: none;
  outline: none;
  background: transparent;
}

button::after {
  border: none;
}
</style>