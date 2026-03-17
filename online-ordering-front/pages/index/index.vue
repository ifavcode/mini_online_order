<script setup lang="ts">
import { ref } from 'vue';
import BusinessesInfo from '../../views/businesses-info/index.vue'
import BusinessesMain from '../../views/businesses-main/index.vue'
import { onMounted } from 'vue';
import { useSystemStore } from '../../stores/system';
import { computed } from 'vue';
import { useGoodStore } from '../../stores/good';
import { watch } from 'vue'
import GoodPopDetails from '../../views/businesses-main/goodPopDetails.vue';
import BuyCartPop from '../../views/businesses-main/buyCartPop.vue';
import { useUserStore } from '../../stores/user';

//@ts-ignore
import { onLoad } from '@dcloudio/uni-app';
import { tableCode2Name } from '../../api/table';

const popup = ref()
const buyCartPopup = ref()
const { safeArea } = useSystemStore()
const goodStore = useGoodStore()
const userStore = useUserStore()

const popHeight = computed(() => {
  return (safeArea.height ?? 0) * 6 / 7
})

const buyCartHeight = computed(() => {
  return (safeArea.height ?? 0) * 1 / 2
})

watch(
  () => goodStore.popShow,
  (val: boolean) => {
    if (val) {
      popup.value?.open?.('bottom')
    } else {
      popup.value?.close?.()
      goodStore.choosedSpec = new Map()
      setTimeout(() => {
        goodStore.setGoodDetails({})
        goodStore.count = 1
      }, 200);
    }
  }
)

watch(
  () => goodStore.buyCartShow,
  (val: boolean) => {
    if (val) {
      buyCartPopup.value?.open?.('bottom')
    } else {
      buyCartPopup.value?.close?.()
    }
  }
)

function change(e) {
  goodStore.togglePopShow(e.show)
}

function changeBuyCart(e) {
  goodStore.toggleBuyCartShow(e.show)
}

async function tableCode2NameFunc() {
  console.log('获取桌名', userStore.tableCode && userStore.tableCode != '');
  if (userStore.tableCode && userStore.tableCode != '') {
    userStore.tableName = await tableCode2Name(userStore.tableCode)
    console.log(userStore.tableName);
  }
}

onLoad((option: any) => {
  if (option?.q) {
    console.log("扫码进入小程序");
    const url = decodeURIComponent(option.q)
    const regex = /[?&]([^=#]+)=([^&#]*)/g;
    const params: any = {};
    let match: any;
    while ((match = regex.exec(url))) {
      params[match[1]] = match[2];
    }
    if (params.tableCode) {
      userStore.tableCode = params.tableCode
      tableCode2NameFunc()
    }
    // 小程序分享跳转处理
    // uni.navigateTo({
    //   url: "/" + params.path + "?" + params.query,
    // });
  } else {
    console.log("正常进入小程序");
  }

})


onMounted(() => {
  goodStore.initChoosedGoods()
})
</script>

<template>
  <view>
    <BusinessesInfo />
    <BusinessesMain />
    <uni-popup ref="popup" :safe-area="false" @change="change">
      <view :style="{ height: popHeight + 'px' }" class="px-4 py-2 tl-tr-rounded-lg bg-white">
        <GoodPopDetails :height="popHeight" />
      </view>
    </uni-popup>
    <uni-popup ref="buyCartPopup" :safe-area="false" @change="changeBuyCart" :animation="false">
      <view :style="{ height: buyCartHeight + 'px' }" class="tl-tr-rounded-lg bg-white">
        <BuyCartPop :height="buyCartHeight" />
      </view>
    </uni-popup>
  </view>
</template>

<style lang="scss" scoped>
:deep(.uni-popup) {
  z-index: 1000;
}
</style>