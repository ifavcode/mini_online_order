<template>
  <div class="">
    <div class="mx-auto">
      <div class="flex justify-between items-center mb-6">
        <el-button type="primary" @click="handleCreate">
          <el-icon class="mr-1">
            <Plus />
          </el-icon>
          发布动态
        </el-button>
      </div>

      <!-- 动态列表 -->
      <div class="bg-white rounded-lg">
        <el-table :data="dynamicList" v-loading="loading" class="w-full" stripe border>
          <el-table-column prop="id" label="ID" width="80" />

          <el-table-column label="描述" min-width="200">
            <template #default="{ row }">
              <div class="line-clamp-2">{{ row.description }}</div>
            </template>
          </el-table-column>

          <el-table-column label="图片" min-width="200">
            <template #default="{ row }">
              <div v-if="row.images && row.images.length > 0" class="flex gap-2 flex-wrap">
                <el-image v-for="(img, index) in row.images" :key="index" :src="img" :preview-src-list="row.images"
                  :initial-index="index" preview-teleported class="w-24 h-24 rounded object-cover cursor-pointer"
                  fit="contain" />
              </div>
              <span v-else class="text-gray-400">暂无图片</span>
            </template>
          </el-table-column>

          <el-table-column prop="createdAt" label="发布时间" width="180" />

          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="flex justify-end p-4">
          <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange" @current-change="handlePageChange" />
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入动态描述" maxlength="1024"
            show-word-limit />
        </el-form-item>

        <el-form-item label="图片" prop="images">
          <el-upload v-model:file-list="fileList" :action="uploadAction" list-type="picture-card"
            :on-preview="handlePictureCardPreview" :multiple="true" :limit="9">
            <el-icon>
              <Plus />
            </el-icon>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogImageVisible">
      <img w-full :src="dialogImageUrl" alt="Preview Image" />
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete } from '@element-plus/icons-vue';
import {
  getBusinessDynamicPage,
  createBusinessDynamic,
  updateBusinessDynamic,
  deleteBusinessDynamic
} from '@/api/businessDynamic';


const uploadAction = `${import.meta.env.VITE_APP_BASE_API}/upload/image`;
// 数据状态
const loading = ref(false);
const submitLoading = ref(false);
const dynamicList = ref([]);
const dialogVisible = ref(false);
const formRef = ref(null);
const fileList = ref([])
const dialogImageUrl = ref('')
const dialogImageVisible = ref(false)

// 分页参数
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

// 表单数据
const form = reactive({
  id: null,
  description: '',
  images: []
});

// 表单验证规则
const rules = {
  description: [
    { required: true, message: '请输入动态描述', trigger: 'blur' },
    { max: 1024, message: '描述不能超过1024个字符', trigger: 'blur' }
  ]
};

// 弹窗标题
const dialogTitle = computed(() => {
  return form.id ? '编辑动态' : '发布动态';
});

// 加载动态列表
const loadDynamics = async () => {
  loading.value = true;
  try {
    const query = {
      page: pagination.page - 1, // 后端页码从0开始
      size: pagination.size,
      sortBy: 'createdAt',
      direction: 'DESC'
    };

    const response = await getBusinessDynamicPage(query);
    dynamicList.value = response.content;
    pagination.total = response.totalElements;
  } catch (error) {
    ElMessage.error('加载动态列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.page = 1;
  loadDynamics();
};

// 处理页码变化
const handlePageChange = (page) => {
  pagination.page = page;
  loadDynamics();
};

// 打开新增对话框
const handleCreate = () => {
  dialogVisible.value = true;
};

// 打开编辑对话框
const handleEdit = (row) => {
  form.id = row.id;
  form.description = row.description;
  form.images = row.images ? [...row.images] : [];
  fileList.value = row.images.reduce((pre, cur) => {
    pre.push({
      name: '',
      url: cur,
      response: cur
    })
    return pre
  }, [])
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    submitLoading.value = true;
    form.images = fileList.value.reduce((pre, cur) => {
      pre.push(cur.response)
      return pre
    }, [])
    try {
      const data = {
        description: form.description,
        images: form.images
      };

      if (form.id) {
        await updateBusinessDynamic(form.id, data);
        ElMessage.success('更新成功');
      } else {
        await createBusinessDynamic(data);
        ElMessage.success('发布成功');
      }

      dialogVisible.value = false;
      fileList.value = []
      loadDynamics();
    } catch (error) {
      ElMessage.error(form.id ? '更新失败' : '发布失败');
      console.error(error);
    } finally {
      submitLoading.value = false;
    }
  });
};

// 删除动态
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除这条动态吗?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteBusinessDynamic(row.id);
      ElMessage.success('删除成功');
      loadDynamics();
    } catch (error) {
      ElMessage.error('删除失败');
      console.error(error);
    }
  }).catch(() => {
    // 取消删除
  });
};

// 重置表单
const resetForm = () => {
  form.id = null;
  form.description = '';
  form.images = [];
  formRef.value?.resetFields();
};

const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url
  dialogImageVisible.value = true
}

// 页面加载时获取数据
onMounted(() => {
  loadDynamics();
});
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>