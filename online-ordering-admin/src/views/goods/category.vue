<template>
  <div>
    <!-- 头部操作区 -->
    <div class="bg-white rounded-lg mb-4">
      <div class="flex justify-between items-center">
        <div class="flex gap-3">
          <el-input v-model="searchQuery.name" placeholder="搜索类别名称" class="w-64" clearable @keyup.enter="handleSearch">
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
          新增类别
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="bg-white rounded-lg">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="类别名称" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span class="">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.sortOrder }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createdAt }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
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
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增类别' : '编辑类别'" width="500px"
      :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="类别名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入类别名称（可使用 emoji，如：🏆 推荐）" maxlength="50" show-word-limit />
          <div class="mt-2 text-sm text-gray-500">
            提示：可以在名称前添加 emoji 图标，如：🏆 推荐、☕ 咖啡、🧋 奶茶
          </div>
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <div>
            <el-input-number v-model="formData.sortOrder" :min="0" class="w-full" placeholder="数值越小越靠前" />
            <div class="mt-2 text-sm text-gray-500">
              数值越小，显示顺序越靠前
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, Plus } from "@element-plus/icons-vue";
import {
  getCategoryPage,
  createCategory,
  updateCategory,
  deleteCategory
} from "@/api/category";

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
const dialogType = ref("add"); // 'add' | 'edit'
const submitLoading = ref(false);

// 表单
const formRef = ref();
const formData = reactive({
  id: null,
  name: "",
  sortOrder: 1
});

const formRules = {
  name: [
    { required: true, message: "请输入类别名称", trigger: "blur" },
    { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" }
  ],
  sortOrder: [{ required: true, message: "请输入排序值", trigger: "blur" }]
};

// 加载类别列表
const loadCategories = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      sortBy: "sortOrder",
      direction: "ASC",
      ...searchQuery
    };
    const res = await getCategoryPage(params);
    tableData.value = res.content;
    pagination.total = res.totalElements;
  } catch (error) {
    ElMessage.error("加载失败");
    console.error("加载类别失败:", error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadCategories();
};

// 重置
const handleReset = () => {
  searchQuery.name = "";
  pagination.page = 1;
  loadCategories();
};

// 分页变化
const handlePageChange = () => {
  loadCategories();
};

const handleSizeChange = () => {
  pagination.page = 1;
  loadCategories();
};

// 新增
const handleAdd = () => {
  dialogType.value = "add";
  Object.assign(formData, {
    id: null,
    name: "",
    sortOrder: tableData.value.length + 1
  });
  dialogVisible.value = true;
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 编辑
const handleEdit = row => {
  dialogType.value = "edit";
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    sortOrder: row.sortOrder
  });
  dialogVisible.value = true;
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate();
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
        await createCategory({
          name: formData.name,
          sortOrder: formData.sortOrder
        });
        ElMessage.success("新增成功");
      } else {
        await updateCategory(formData.id, {
          name: formData.name,
          sortOrder: formData.sortOrder
        });
        ElMessage.success("更新成功");
      }
      dialogVisible.value = false;
      loadCategories();
    } catch (error) {
      ElMessage.error(dialogType.value === "add" ? "新增失败" : "更新失败");
      console.error("提交失败:", error);
    } finally {
      submitLoading.value = false;
    }
  });
};

// 删除
const handleDelete = async row => {
  try {
    await ElMessageBox.confirm(
      `确定要删除类别"${row.name}"吗？此操作不可逆，删除后该类别及所属商品将全部清除且无法恢复。`,
      "警告",
      { 
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    loading.value = true;
    await deleteCategory(row.id);
    ElMessage.success("删除成功");

    // 如果当前页没有数据了，回到上一页
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--;
    }
    loadCategories();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败");
      console.error("删除失败:", error);
    }
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadCategories();
});
</script>

<style scoped></style>
