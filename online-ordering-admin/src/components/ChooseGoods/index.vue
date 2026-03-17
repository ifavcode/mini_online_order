<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { getAllCategory } from '../../api/category';
import { Goods, MenuCategory } from 'types/entity';
import { ElMessage } from 'element-plus';
import { useGoodsStore } from '@/store/modules/goods';
import { storeToRefs } from 'pinia';

const menuType = ref<MenuCategory[]>([])
const activeCategory = ref<number>(1)
const goodsStore = useGoodsStore()
const { selectedGoods } = storeToRefs(goodsStore)
const dialogVisible = ref(false)
const currentGoods = ref<any>(null)
const count = ref(1)
const selectedSpecs = ref<Record<string, any>>({})

async function getAllCategoryFunc() {
  menuType.value = await getAllCategory()
  if (menuType.value.length > 0) {
    activeCategory.value = menuType.value[0].id
  }
}

// 当前分类的商品列表
const currentGoodsList = computed(() => {
  const category = menuType.value.find(c => c.id === activeCategory.value)
  return category?.goodsList || []
})

// 选择商品
function selectGoods(goods: Goods) {
  if (goods.stock === 0) {
    return
  }
  currentGoods.value = goods
  count.value = 1
  selectedSpecs.value = {}

  // 如果有规格，初始化选中的规格
  if (goods.hasSpec && goods.specGroups?.length > 0) {
    goods.specGroups.forEach((group: any) => {
      if (group.selectType === 'one' && group.specItems?.length > 0) {
        selectedSpecs.value[group.id] = group.specItems[0].id
      } else if (group.selectType === 'more') {
        selectedSpecs.value[group.id] = [group.specItems[0].id]
      }
    })
    dialogVisible.value = true
  } else {
    dialogVisible.value = true
  }
}

// 选择单选规格
function selectOneSpec(groupId: number, spec: any) {
  selectedSpecs.value[groupId] = spec.id
}

// 切换多选规格
function toggleMoreSpec(groupId: number, spec: any) {
  if (spec.stock === 0) return
  if (!selectedSpecs.value[groupId]) {
    selectedSpecs.value[groupId] = []
  }
  const index = selectedSpecs.value[groupId].findIndex((s: number) => s === spec.id)

  if (index > -1) {
    selectedSpecs.value[groupId] = selectedSpecs.value[groupId].filter(v => v !== spec.id)
  } else {
    selectedSpecs.value[groupId].push(spec.id)
  }
}

// 检查多选规格是否选中
function isSpecSelected(groupId: number, specId: number) {
  return selectedSpecs.value[groupId]?.some((s: any) => s === specId) || false
}

// 计算总价
const totalPrice = computed(() => {
  if (!currentGoods.value) return 0
  let price = currentGoods.value.basePrice

  currentGoods.value.specGroups.forEach(group => {
    if (group.specItems && group.specItems.length > 0) {
      group.specItems.forEach(spec => {
        if (spec.extraPrice && selectedSpecs.value[group.id].includes(spec.id)) {
          price += spec.extraPrice
        }
      })
    }
  })

  return price * count.value
})

function collectChoosedIds() {
  const set = new Set()
  for (const val of Object.values(selectedSpecs.value)) {
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

// 添加到购物车
function addToCart() {
  const tmpDetails = JSON.parse(JSON.stringify(currentGoods.value))
  const set = collectChoosedIds()
  for (let cur of tmpDetails['specGroups']) {
    cur.specItems = cur.specItems.filter(v => set.has(v.id))
  }
  const item = {
    ...tmpDetails,
    count: count.value,
    totalPrice: totalPrice.value
  }
  selectedGoods.value.push(item)
  dialogVisible.value = false
  ElMessage.success('添加成功')
}

// 移除商品
function removeGoods(index: number) {
  selectedGoods.value.splice(index, 1)
}

function formatSpecs(item: Record<string, any>) {
  if (!item || !Array.isArray(item.specGroups)) return ''
  const specTexts: string[] = []
  for (const group of item.specGroups) {
    if (!group || !group.specItems || group.specItems.length === 0) continue
    const specNames = group.specItems.map((spec: any) => spec.name).join('/')
    specTexts.push(`${group.name}: ${specNames}`)
  }

  return specTexts.join(', ')
}

onMounted(() => {
  getAllCategoryFunc()
})
</script>

<template>
  <div class="goods-selector">
    <!-- 左侧分类 -->
    <div class="flex h-[600px] border rounded-lg overflow-hidden">
      <div class="w-32 bg-gray-50 border-r overflow-y-auto">
        <div v-for="category in menuType" :key="category.id" @click="activeCategory = category.id"
          class="px-4 py-3 cursor-pointer text-sm transition-colors"
          :class="activeCategory === category.id ? 'bg-primary text-white' : 'hover:bg-gray-100'">
          {{ category.name }}
        </div>
      </div>

      <!-- 右侧商品列表 -->
      <div class="flex-1 p-4 overflow-y-auto">
        <div class="grid grid-cols-3 gap-4 w-full">
          <div v-for="goods in currentGoodsList" :key="goods.id" @click="selectGoods(goods)"
            class="border rounded-lg p-3  hover:shadow-lg transition-shadow"
            :class="goods.stock === 0 ? 'cursor-not-allowed' : 'cursor-pointer'">
            <div class="w-full h-32 mb-2">
              <el-image fit="contain" :src="goods.imgUrl" :alt="goods.name"
                class="w-full h-32 object-cover rounded mb-2" />
            </div>
            <h3 class="font-medium text-sm mb-1 truncate">{{ goods.name }}</h3>
            <p class="text-xs text-gray-600 mb-2 truncate">{{ goods.description }}</p>
            <p class="text-xs text-gray-500 truncate">库存:{{ goods.stock }}</p>
            <div class="flex items-center justify-between">
              <span class="text-red-500 font-bold">¥{{ goods.basePrice }}</span>
              <el-button size="small" type="primary" :disabled="goods.stock === 0">
                {{ goods.stock !== 0 ? '选择' : '已售罄' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 已选商品列表 -->
    <div v-if="selectedGoods.length > 0" class="mt-4">
      <h3 class="font-bold mb-2">已选商品</h3>
      <div class="border rounded-lg p-4">
        <div v-for="(item, index) in selectedGoods" :key="index"
          class="flex items-center justify-between py-2 border-b last:border-b-0">
          <div class="flex items-center gap-3 flex-1">
            <img :src="item.imgUrl" class="w-12 h-12 object-cover rounded" />
            <div class="flex-1">
              <div class="font-medium text-sm">{{ item.name }}</div>
              <div class="text-xs text-gray-500">{{ formatSpecs(item) }}</div>
            </div>
          </div>
          <div class="flex items-center gap-4">
            <span class="text-sm">x{{ item.count }}</span>
            <span class="text-red-500 font-bold">¥{{ item.totalPrice }}</span>
            <el-button size="small" type="danger" text @click="removeGoods(index)">
              删除
            </el-button>
          </div>
        </div>
        <div class="mt-3 pt-3 border-t flex justify-between items-center">
          <span class="font-bold">总计：</span>
          <span class="text-xl text-red-500 font-bold">
            ¥{{selectedGoods.reduce((sum, item) => sum + item.totalPrice, 0).toFixed(2)}}
          </span>
        </div>
      </div>
    </div>

    <!-- 规格选择对话框 -->
    <el-dialog v-model="dialogVisible" :title="currentGoods?.name" width="500px">
      <div v-if="currentGoods">
        <img :src="currentGoods.imgUrl" class="w-full h-48 object-cover rounded mb-4" />

        <!-- 规格选择 -->
        <div v-for="group in currentGoods.specGroups" :key="group.id" class="mb-4">
          <h4 class="font-medium mb-2 text-sm">{{ group.name }}</h4>

          <!-- 单选规格 -->
          <div v-if="group.selectType === 'one'" class="flex flex-wrap gap-2">
            <div v-for="spec in group.specItems" :key="spec.id" @click="selectOneSpec(group.id, spec)"
              class="px-3 py-1.5 border rounded cursor-pointer text-sm transition-colors" :class="selectedSpecs[group.id] === spec.id
                ? 'border-blue-500 bg-blue-50 text-blue-600'
                : 'hover:border-gray-400'">
              {{ spec.name }}
              <span v-if="spec.extraPrice > 0" class="text-xs text-gray-500">
                +¥{{ spec.extraPrice }}
              </span>
              <span class="text-xs text-gray-400" v-if="spec.stock != -1">
                ({{ spec.stock > 0 ? `${spec.stock}个` : '已售罄' }})
              </span>
            </div>
          </div>

          <!-- 多选规格 -->
          <div v-else class="flex flex-wrap gap-2">
            <div v-for="spec in group.specItems" :key="spec.id" @click="toggleMoreSpec(group.id, spec)"
              class="px-3 py-1.5 border rounded cursor-pointer text-sm transition-colors" :class="[
                isSpecSelected(group.id, spec.id)
                  ? 'border-blue-500 bg-blue-50 text-blue-600'
                  : 'hover:border-gray-400',
                spec.stock === 0 ? 'bg-gray-100 cursor-not-allowed! hover:border-gray-200!' : ''
              ]">
              {{ spec.name }}
              <span v-if="spec.extraPrice > 0" class="text-xs text-gray-500">
                +¥{{ spec.extraPrice }}
              </span>
              <span class="text-xs text-gray-400" v-if="spec.stock != -1">
                ({{ spec.stock > 0 ? `${spec.stock}个` : '已售罄' }})
              </span>
            </div>
          </div>
        </div>

        <!-- 数量选择 -->
        <div class="flex items-center justify-between mt-6">
          <span class="font-medium">数量</span>
          <el-input-number v-model="count" :min="1" :max="99" size="small" />
        </div>

        <!-- 价格显示 -->
        <div class="mt-4 pt-4 border-t flex items-center justify-between">
          <span class="text-lg font-bold">总价：</span>
          <span class="text-2xl text-red-500 font-bold">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addToCart">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.goods-selector {
  width: 100%;
}
</style>