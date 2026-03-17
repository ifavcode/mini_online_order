import { defineStore } from "pinia";
import { ref, watch } from "vue";
import Constant from "../utils/Constant";

export const useGoodStore = defineStore("good", () => {
  const popShow = ref(false);
  const buyCartShow = ref(false);
  const choosedSpec = ref([]); //当前浏览的商品选择的规则id集合
  const count = ref(1);
  const choosedGoods = ref([]); //已经选择的商品集合
  const menuTypeStorage = ref([]) //菜单缓存

  function togglePopShow(type) {
    popShow.value = type;
  }

  function toggleBuyCartShow(type) {
    buyCartShow.value = type;
  }

  const goodDetails = ref({});

  function setGoodDetails(data) {
    goodDetails.value = data;
  }

  const curGoodDetails = ref({});

  function setCurGoodDetails(data) {
    curGoodDetails.value = data;
  }

  function addChoosedGoods(data) {
    choosedGoods.value.push(data);
  }

  function clearChoosedGoods() {
    choosedGoods.value = [];
    uni.clearStorageSync(Constant.CHOOSED_GOODS)
  }

  watch(
    choosedGoods,
    (val) => {
      uni.setStorageSync(Constant.CHOOSED_GOODS, JSON.stringify(val));
    },
    { deep: true }
  );

  function initChoosedGoods() {
    const localStr = uni.getStorageSync(Constant.CHOOSED_GOODS);
    if (localStr) {
      choosedGoods.value = JSON.parse(localStr);
    }
  }

  return {
    popShow,
    togglePopShow,
    goodDetails,
    setGoodDetails,
    curGoodDetails,
    setCurGoodDetails,
    choosedSpec,
    choosedGoods,
    addChoosedGoods,
    count,
    buyCartShow,
    toggleBuyCartShow,
    initChoosedGoods,
    clearChoosedGoods,
    menuTypeStorage
  };
});
