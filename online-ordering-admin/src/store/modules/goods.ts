import { defineStore } from "pinia";
import { ref } from "vue";


export const useGoodsStore = defineStore("goods_store", () => {

  const selectedGoods = ref<any[]>([])

  return {
    selectedGoods
  }
})