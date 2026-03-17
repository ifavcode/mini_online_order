<template>
  <div class="">
    <!-- 头部操作区 -->
    <div class="bg-white rounded-lg p-4 mb-4">
      <div class="flex justify-between items-center">
        <div class="flex gap-3">
          <el-input v-model="searchQuery.name" placeholder="搜索商品名称" class="w-64" clearable @keyup.enter="handleSearch">
            <template #prefix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset" style="margin-left: 0px">重置</el-button>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon class="mr-1">
            <Plus />
          </el-icon>
          新增商品
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="bg-white rounded-lg">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div v-if="row.hasSpec === 1 && row.specGroups?.length" class="px-12 py-4">
              <div class="text-lg font-semibold mb-4 text-gray-700">
                规格信息
              </div>
              <div v-for="group in row.specGroups" :key="group.id" class="mb-6">
                <div class="flex items-center gap-3 mb-3">
                  <span class="text-base font-medium text-gray-800">{{
                    group.name
                  }}</span>
                  <el-tag :type="group.selectType === 'one' ? 'success' : 'primary'" size="small">
                    {{ group.selectType === "one" ? "单选" : "多选" }}
                  </el-tag>
                </div>
                <div class="grid grid-cols-4 gap-3">
                  <div v-for="item in group.specItems" :key="item.id"
                    class="border rounded-lg p-3 hover:shadow-md transition-shadow" :class="item.status === 1
                      ? 'bg-white border-blue-200'
                      : 'bg-gray-50 border-gray-200'
                      ">
                    <div class="flex justify-between items-start mb-2">
                      <span class="font-medium text-gray-800">{{
                        item.name
                      }}</span>
                      <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
                        {{ item.status === 1 ? "显示" : "隐藏" }}
                      </el-tag>
                    </div>
                    <div class="space-y-1 text-sm">
                      <div class="flex justify-between">
                        <span class="text-gray-500">加价:</span>
                        <span class="text-red-500 font-semibold">
                          {{
                            item.extraPrice > 0 ? `+¥${item.extraPrice}` : "¥0"
                          }}
                        </span>
                      </div>
                      <div class="flex justify-between">
                        <span class="text-gray-500">库存:</span>
                        <span :class="item.stock === -1
                          ? 'text-green-500'
                          : 'text-blue-500'
                          ">
                          {{ item.stock === -1 ? "不限" : item.stock }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="px-12 py-4 text-gray-400 text-center">
              该商品暂无规格信息
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.imgUrl" :src="row.imgUrl" :preview-src-list="[row.imgUrl]" class="w-16 h-16 rounded"
              fit="cover" preview-teleported :z-index="9999" />
            <el-image v-else src="public/no_img.jpg" class="w-16 rounded" fit="cover" preview-teleported
              :z-index="9999" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ row.category?.name || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <span class="text-red-500 font-semibold">¥{{ row.basePrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="规格" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.hasSpec === 1" type="success" size="small">
              有 ({{ row.specGroups?.length || 0 }})
            </el-tag>
            <el-tag v-else type="info" size="small">无</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)"
              active-text="显示" inactive-text="隐藏" inline-prompt />
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80">
          <template #default="{ row }">
            <p>{{ row.stock === -1 ? '无限' : row.stock }}</p>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-if="row.hasSpec === 1" link type="warning" size="small" @click="handleEditSpec(row)">
              规格
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="p-4 flex justify-end">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handlePageChange" />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增商品' : '编辑商品'" width="600px"
      :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品图片" prop="imgUrl">
          <div>
            <el-upload :action="uploadAction" :show-file-list="false" :on-success="handleFollowUploadSuccess"
              accept="image/*">
              <el-button type="primary" plain>点击上传</el-button>
            </el-upload>
            <view v-if="formData.imgUrl">
              <el-image :src="formData.imgUrl" class="size-32 rounded-md mt-2" :preview-src-list="[formData.imgUrl]"
                fit="cover" preview-teleported />
            </view>
          </div>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="formData.stock" :min="-1" class="w-full" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" class="w-full">
            <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="基础价格" prop="basePrice">
          <el-input-number v-model="formData.basePrice" :min="0" :precision="2" :step="1" class="w-full" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" class="w-full" />
        </el-form-item>
        <el-form-item label="是否有规格" prop="hasSpec">
          <el-radio-group v-model="formData.hasSpec">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 规格管理对话框 -->
    <el-dialog v-model="specDialogVisible" title="规格管理" width="1000px" :close-on-click-modal="false"
      custom-class="spec-management-dialog">
      <div class="flex flex-col gap-6 p-1">
        <!-- 顶部操作栏 -->
        <div class="flex justify-between items-end border-b pb-4">
          <div>
            <h3 class="text-sm font-medium text-gray-500 mb-1">当前编辑商品</h3>
            <div class="text-xl font-bold text-gray-800 flex items-center gap-2">
              <el-icon class="text-blue-500">
                <Goods />
              </el-icon>
              {{ currentGoodsName }}
            </div>
          </div>
          <el-button type="primary" @click="handleAddSpecGroup" class="shadow-sm">
            <el-icon class="mr-1">
              <Plus />
            </el-icon>
            添加规格组
          </el-button>
        </div>

        <!-- 规格组列表 -->
        <div class="space-y-6 max-h-[60vh] overflow-y-auto px-1">
          <div v-for="(group, groupIndex) in specGroups" :key="group.id || groupIndex"
            class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow">
            <!-- 规格组头部 -->
            <div class="bg-gray-50/80 px-4 py-3 border-b flex items-center justify-between">
              <div class="flex items-center gap-4">
                <div class="flex items-center gap-2">
                  <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">组名称</span>
                  <el-input v-model="group.name" placeholder="如：尺寸/颜色" class="w-40!" size="default" />
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">模式</span>
                  <el-select v-model="group.selectType" class="w-28!">
                    <el-option label="单选" value="one" />
                    <el-option label="多选" value="more" />
                  </el-select>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">排序</span>
                  <el-input-number v-model="group.sortOrder" :min="0" controls-position="right" class="w-24!" />
                </div>
              </div>

              <div class="flex gap-2">
                <el-button type="primary" plain size="small" @click="handleAddSpecItem(groupIndex)">
                  <el-icon class="mr-1">
                    <Plus />
                  </el-icon>添加选项
                </el-button>
                <el-popconfirm title="确定删除整个规格组吗？" @confirm="handleRemoveSpecGroup(groupIndex)">
                  <template #reference>
                    <el-button type="danger" plain size="small">
                      <el-icon>
                        <Delete />
                      </el-icon>
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>

            <!-- 规格项内容区 -->
            <div class="p-4 bg-white">
              <!-- 表头提示 -->
              <div v-if="group.specItems.length" class="flex items-center gap-3 px-2 mb-2">
                <div class="w-32 text-xs font-semibold text-gray-400">
                  选项名称
                </div>
                <div class="w-28 text-xs font-semibold text-gray-400">
                  附加价格 (¥)
                </div>
                <div class="w-32 text-xs font-semibold text-gray-400">
                  库存状况
                </div>
                <div class="w-24 text-xs font-semibold text-gray-400">
                  显示排序
                </div>
                <div class="w-24 text-xs font-semibold text-gray-400">状态</div>
                <div class="flex-1"></div>
              </div>

              <div class="space-y-3">
                <div v-for="(item, itemIndex) in group.specItems" :key="item.id || itemIndex"
                  class="group flex items-center gap-3 p-2 rounded-lg border border-transparent hover:border-blue-100 hover:bg-blue-50/30 transition-all">
                  <el-input v-model="item.name" placeholder="例如：红/大号" class="w-32!" />
                  <el-input-number v-model="item.extraPrice" :min="0" :precision="2" class="w-28!" placeholder="0.00" />
                  <el-input-number v-model="item.stock" :min="-1" class="w-32!" placeholder="库存" />
                  <el-input-number v-model="item.sortOrder" :min="0" controls-position="right" class="w-24!" />
                  <el-select v-model="item.status" class="w-24!">
                    <el-option label="显示" :value="1" />
                    <el-option label="隐藏" :value="0" />
                  </el-select>

                  <el-button type="danger" link @click="handleRemoveSpecItem(groupIndex, itemIndex)"
                    class="opacity-0 group-hover:opacity-100 transition-opacity">
                    <el-icon size="18">
                      <CircleCloseFilled />
                    </el-icon>
                  </el-button>
                </div>

                <!-- 空项提示 -->
                <div v-if="!group.specItems.length"
                  class="text-center py-6 border-2 border-dashed border-gray-100 rounded-lg">
                  <el-button type="primary" link @click="handleAddSpecItem(groupIndex)">
                    暂无选项，立即添加
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!specGroups.length"
            class="flex flex-col items-center justify-center py-16 bg-gray-50 rounded-2xl border-2 border-dashed border-gray-200">
            <el-icon size="48" class="text-gray-300 mb-4">
              <Memo />
            </el-icon>
            <p class="text-gray-400 mb-4">
              暂无规格组信息，建议先按“颜色”、“尺寸”等维度添加
            </p>
            <el-button type="primary" size="large" @click="handleAddSpecGroup">
              开始配置规格
            </el-button>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3 pt-2">
          <el-button @click="specDialogVisible = false" size="large">取 消</el-button>
          <el-button type="primary" size="large" class="px-8" @click="handleSaveSpec" :loading="specSubmitLoading">
            保存配置
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Search,
  Plus,
  Close,
  Goods,
  Delete,
  CircleCloseFilled,
  Memo
} from "@element-plus/icons-vue";
import {
  getGoodsPage,
  createGoods,
  updateGoods,
  deleteGoods
} from "@/api/goods";
import { getAllCategory } from "@/api/category";
import { createSpecGroup, deleteSpecGroup } from "@/api/specGroup";

const uploadAction = `${import.meta.env.VITE_APP_BASE_API}/upload/image`;
// 表格数据
const tableData = ref([]);
const loading = ref(false);

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

// 搜索
const searchQuery = reactive({
  name: ""
});

// 对话框
const dialogVisible = ref(false);
const dialogType = ref("add");
const submitLoading = ref(false);

// 规格对话框
const specDialogVisible = ref(false);
const specSubmitLoading = ref(false);
const currentGoodsId = ref(null);
const currentGoodsName = ref("");
const specGroups = ref([]);

// 表单
const formRef = ref();
const categoryList = ref([]);
const formData = reactive({
  id: null,
  name: "",
  imgUrl: "",
  categoryId: 1,
  basePrice: 0,
  description: "",
  sortOrder: 1,
  hasSpec: 0,
  status: 1
});

const formRules = {
  name: [{ required: true, message: "请输入商品名称", trigger: "blur" }],
  categoryId: [{ required: true, message: "请选择分类", trigger: "change" }],
  basePrice: [{ required: true, message: "请输入价格", trigger: "blur" }]
};

async function getAllCategoryFunc() {
  categoryList.value = await getAllCategory();
  console.log(categoryList.value);
}

// 加载商品列表
const loadGoods = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sortBy: "sortOrder",
      direction: "ASC",
      ...searchQuery
    };
    const res = await getGoodsPage(params);
    tableData.value = res.content;
    pagination.total = res.totalElements;
  } catch (error) {
    ElMessage.error("加载失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadGoods();
};

// 重置
const handleReset = () => {
  searchQuery.name = "";
  pagination.page = 1;
  loadGoods();
};

// 分页变化
const handlePageChange = () => {
  loadGoods();
};

const handleSizeChange = () => {
  pagination.page = 1;
  loadGoods();
};

// 新增
const handleAdd = () => {
  dialogType.value = "add";
  Object.assign(formData, {
    id: null,
    name: "",
    imgUrl: "",
    categoryId: 1,
    basePrice: 0,
    description: "",
    sortOrder: 1,
    hasSpec: 0,
    status: 1,
    stock: -1
  });
  dialogVisible.value = true;
};

// 编辑
const handleEdit = row => {
  dialogType.value = "edit";
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    imgUrl: row.imgUrl,
    categoryId: row.categoryId,
    basePrice: row.basePrice,
    description: row.description,
    sortOrder: row.sortOrder,
    hasSpec: row.hasSpec,
    status: row.status,
    stock: row.stock
  });
  dialogVisible.value = true;
};

// 编辑规格
const handleEditSpec = row => {
  currentGoodsId.value = row.id;
  currentGoodsName.value = row.name;
  specGroups.value = JSON.parse(JSON.stringify(row.specGroups || []));
  specDialogVisible.value = true;
};

// 添加规格组
const handleAddSpecGroup = () => {
  specGroups.value.push({
    name: "",
    selectType: "one",
    sortOrder: specGroups.value.length + 1,
    specItems: [],
    goodsId: currentGoodsId.value
  });
};

// 删除规格组
const handleRemoveSpecGroup = index => {
  specGroups.value.splice(index, 1);
};

// 添加规格选项
const handleAddSpecItem = groupIndex => {
  const group = specGroups.value[groupIndex];
  group.specItems.push({
    name: "",
    extraPrice: 0,
    stock: -1,
    sortOrder: group.specItems.length + 1,
    status: 1
  });
};

// 删除规格选项
const handleRemoveSpecItem = (groupIndex, itemIndex) => {
  specGroups.value[groupIndex].specItems.splice(itemIndex, 1);
};

// 保存规格
const handleSaveSpec = async () => {
  try {
    specSubmitLoading.value = true;
    if (specGroups.value.length === 0) {
      await deleteSpecGroup(currentGoodsId.value);
    } else {
      await createSpecGroup(specGroups.value);
    }
    specDialogVisible.value = false;
    loadGoods();
  } catch (error) {
    console.error(error);
  } finally {
    specSubmitLoading.value = false;
  }
};

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async valid => {
    if (!valid) return;

    submitLoading.value = true;
    try {
      if (dialogType.value === "add") {
        await createGoods(formData);
        ElMessage.success("新增成功");
      } else {
        await updateGoods(formData.id, formData);
        ElMessage.success("更新成功");
      }
      dialogVisible.value = false;
      loadGoods();
    } catch (error) {
      ElMessage.error(dialogType.value === "add" ? "新增失败" : "更新失败");
    } finally {
      submitLoading.value = false;
    }
  });
};

// 删除
const handleDelete = async row => {
  try {
    await ElMessageBox.confirm(`确定要删除商品"${row.name}"吗？`, "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });

    await deleteGoods(row.id);
    ElMessage.success("删除成功");
    loadGoods();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败");
    }
  }
};

// 状态变化
const handleStatusChange = async row => {
  try {
    await updateGoods(row.id, { status: row.status, hasSpec: row.hasSpec, sortOrder: row.sortOrder });
    ElMessage.success("状态更新成功");
  } catch (error) {
    ElMessage.error("状态更新失败");
    row.status = row.status === 1 ? 0 : 1;
  }
};

function handleFollowUploadSuccess(response) {
  if (response) {
    formData.imgUrl = response;
    ElMessage.success("图片上传成功");
  } else {
    ElMessage.error("图片上传失败");
  }
}

onMounted(() => {
  loadGoods();
  getAllCategoryFunc();
});
</script>

<style scoped>
:deep(.el-table__expand-icon) {
  font-size: 14px;
}
</style>
