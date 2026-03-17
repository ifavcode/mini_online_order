<script setup>
import { ref, onMounted, computed } from 'vue';
import { getBusinessDynamicPage } from '../../api/businessDynamic';
import { useSystemStore } from '../../stores/system';
import { formatDateWithRelativeTime } from '../../utils/index'

const systemStore = useSystemStore()
const { safeArea, systemInfo } = systemStore
const dynamicList = ref([]);
const loading = ref(false);
const page = ref(0);
const size = ref(10);
const hasMore = ref(true);

const remainContentHeight = computed(() => {
  //减去business-info的高度
  return safeArea.height - 206
})

async function getBusinessDynamicPageFunc() {
  if (loading.value || !hasMore.value) return;

  try {
    loading.value = true;
    const res = await getBusinessDynamicPage({
      page: page.value,
      size: size.value
    });

    dynamicList.value = [...dynamicList.value, ...res.content];
    hasMore.value = !res.last;
    page.value++;
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
}

// 预览图片
function previewImage(current, images) {
  uni.previewImage({
    current,
    urls: images
  });
}

// 下拉刷新
function onRefresh() {
  page.value = 0;
  dynamicList.value = [];
  hasMore.value = true;
  getBusinessDynamicPageFunc();
}

// 上拉加载更多
function onLoadMore() {
  getBusinessDynamicPageFunc();
}

onMounted(() => {
  getBusinessDynamicPageFunc();
});
</script>

<template>
  <view class="bg-gray-50">
    <!-- 动态列表 -->
    <scroll-view scroll-y :style="{ height: remainContentHeight + 'px' }" @scrolltolower="onLoadMore" refresher-enabled
      @refresherrefresh="onRefresh">
      <view class="">
        <view v-for="item in dynamicList" :key="item.id" class="bg-white mb-3 p-4 shadow-sm">
          <!-- 描述 -->
          <view class="mb-3">
            <text class="text-base text-gray-800">{{ item.description }}</text>
          </view>

          <!-- 图片网格 -->
          <view v-if="item.images && item.images.length > 0" class="grid gap-2" :class="{
            'grid-cols-1': item.images.length === 1,
            'grid-cols-2': item.images.length === 2 || item.images.length === 4,
            'grid-cols-3': item.images.length === 3 || item.images.length > 4
          }">
            <view v-for="(img, index) in item.images" :key="index"
              class="relative overflow-hidden rounded-lg bg-gray-100" :class="{
                'aspect-video': item.images.length === 1,
                'aspect-square': item.images.length > 1
              }" @tap="previewImage(img, item.images)">
              <image :src="img" mode="aspectFill" class="w-full h-full" lazy-load />
            </view>
          </view>

          <!-- 时间 -->
          <view class="mt-3 flex items-center">
            <text class="text-xs text-gray-400">{{ formatDateWithRelativeTime(item.createdAt) }}</text>
          </view>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="py-4 text-center">
          <uni-load-more status="loading" />
        </view>

        <!-- 没有更多 -->
        <view v-else-if="!hasMore && dynamicList.length > 0" class="py-4 text-center">
          <text class="text-sm text-gray-400">没有更多了</text>
        </view>

        <!-- 空状态 -->
        <view v-if="!loading && dynamicList.length === 0" class="py-20 text-center">
          <text class="text-sm text-gray-400">暂无动态</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.grid {
  display: grid;
}

.grid-cols-1 {
  grid-template-columns: repeat(1, minmax(0, 1fr));
}

.grid-cols-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.grid-cols-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.gap-2 {
  gap: 0.5rem;
}

.aspect-square {
  aspect-ratio: 1 / 1;
}

.aspect-video {
  aspect-ratio: 16 / 9;
}
</style>