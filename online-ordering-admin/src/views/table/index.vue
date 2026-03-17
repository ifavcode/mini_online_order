<template>
  <div>
    <div class="mb-4">
      <p class="text-red-400">二维码生成简要说明</p>
      <p class="text-sm text-gray-800">先到微信公众平台设置相关内容——(开发管理-开发设置)下拉一直找到扫普通链接二维码打开小程序，配置你自己的服务器地址(需要可以通过网址直接访问到)，<a
          href="https://blog.csdn.net/zuowentao666/article/details/133903679" class="!text-primary !underline"
          target="blank">见说明</a></p>
      <p class="text-sm text-gray-600">完成配置后，用户可通过微信扫码直接跳转至点餐小程序，并自动关联对应的桌号</p>
    </div>
    <!-- 头部操作区 -->
    <div class="bg-white rounded-lg mb-4">
      <div class="flex justify-between items-center">
        <div class="flex gap-3">
          <el-input v-model="searchQuery.tableName" placeholder="搜索桌号名称" class="w-64" clearable
            @keyup.enter="handleSearch">
            <template #prefix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
          <el-input v-model="searchQuery.tableCode" placeholder="搜索桌号编码" class="w-64" clearable
            @keyup.enter="handleSearch">
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
          新增桌号
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="bg-white rounded-lg">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="桌号名称" min-width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span class="font-medium">{{ row.tableName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="桌号编码" min-width="150">
          <template #default="{ row }">
            <el-tag type="success" size="default">{{ row.tableCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="二维码" width="220">
          <template #default="{ row, $index: index }">
            <div v-if="row.qrCode">
              <img :src="row.qrCode" alt="二维码加载失败" />
              <div class="text-center">
                <el-button link @click="downloadImage(row.qrCode, row.tableCode + '.png')">
                  <el-icon>
                    <Download />
                  </el-icon>
                  下载
                </el-button>
                <el-button link @click="refreshQRCode(row.id, row.tableCode, index)" :loading="qrCodeLoading">
                  <el-icon>
                    <RefreshIcon />
                  </el-icon>
                  重新生成
                </el-button>
              </div>
            </div>
            <div v-else>
              <div
                class="w-[200px] h-[200px] flex items-center justify-center border border-dashed border-gray-300 rounded bg-gray-100 text-gray-400 text-sm">
                暂无二维码
              </div>
              <el-button link @click="generateQRCode(row.id, row.tableCode)" :loading="qrCodeLoading">生成</el-button>
            </div>
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
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增桌号' : '编辑桌号'" width="500px"
      :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="桌号名称" prop="tableName">
          <el-input v-model="formData.tableName" placeholder="请输入桌号名称（如：1号桌、VIP包厢）" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="桌号编码" prop="tableCode">
          <el-input v-model="formData.tableCode" placeholder="请输入桌号编码（如：X01、VIP01）" maxlength="20" show-word-limit />
          <div class="mt-2 text-sm text-gray-500">
            编码可自定义配置，推荐使用字母+数字组合
          </div>
        </el-form-item>
        <el-form-item label="二维码链接" prop="qrCode">
          <el-input v-model="formData.qrCode" type="textarea" placeholder="请输入图片链接" maxlength="256" show-word-limit />
          <div class="mt-2 text-sm text-gray-500">
            输入图片链接（可使用外部工具生成二维码产生URL放在此处）
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
import { Search, Plus, Download } from "@element-plus/icons-vue";
import {
  getTablePage,
  createTable,
  updateTable,
  deleteTable
} from "@/api/table";
import QRCode from 'qrcode'
import { message } from "@/utils/message";
import { uploadBase64 } from "@/api/upload";
import { downloadImage } from "@/utils";
import RefreshIcon from '@/assets/table-bar/refresh.svg?component';

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
  tableName: "",
  tableCode: ""
});

// 对话框
const dialogVisible = ref(false);
const dialogType = ref("add"); // 'add' | 'edit'
const submitLoading = ref(false);

// 表单
const formRef = ref();
const formData = reactive({
  id: null,
  tableName: "",
  tableCode: "",
  qrCode: ""
});

const formRules = {
  tableName: [
    { required: true, message: "请输入桌号名称", trigger: "blur" },
    { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" }
  ],
  tableCode: [
    { required: true, message: "请输入桌号编码", trigger: "blur" },
    { min: 1, max: 20, message: "长度在 1 到 20 个字符", trigger: "blur" },
    {
      pattern: /^[A-Za-z0-9]+$/,
      message: "编码只能包含字母和数字",
      trigger: "blur"
    }
  ]
};

// 加载桌号列表
const loadTables = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      ...searchQuery
    };
    const res = await getTablePage(params);
    tableData.value = res.content;
    pagination.total = res.totalElements;
  } catch (error) {
    ElMessage.error("加载失败");
    console.error("加载桌号失败:", error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadTables();
};

// 重置
const handleReset = () => {
  searchQuery.tableName = "";
  searchQuery.tableCode = "";
  pagination.page = 1;
  loadTables();
};

// 分页变化
const handlePageChange = () => {
  loadTables();
};

const handleSizeChange = () => {
  pagination.page = 1;
  loadTables();
};

// 新增
const handleAdd = () => {
  dialogType.value = "add";
  Object.assign(formData, {
    id: null,
    tableName: "",
    tableCode: "",
    qrCode: ""
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
    tableName: row.tableName,
    tableCode: row.tableCode,
    qrCode: row.qrCode
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
        await createTable({
          tableName: formData.tableName,
          tableCode: formData.tableCode,
          qrCode: formData.qrCode,
        });
        ElMessage.success("新增成功");
      } else {
        await updateTable(formData.id, {
          tableName: formData.tableName,
          tableCode: formData.tableCode,
          qrCode: formData.qrCode,
        });
        ElMessage.success("更新成功");
      }
      dialogVisible.value = false;
      loadTables();
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
      `确定要删除桌号"${row.tableName}"(${row.tableCode})吗？删除后将无法恢复。`,
      "警告",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    loading.value = true;
    await deleteTable(row.id);
    ElMessage.success("删除成功");

    // 如果当前页没有数据了,回到上一页
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--;
    }
    loadTables();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败");
      console.error("删除失败:", error);
    }
  } finally {
    loading.value = false;
  }
};

const qrCodeLoading = ref(false)
async function generateQRCode(id, tableCode) {
  const prefix = import.meta.env.VITE_QRCODE_URL
  try {
    qrCodeLoading.value = true
    const qrCode = await QRCode.toDataURL(prefix + tableCode)
    const qrCodeImgUrl = await uploadBase64(qrCode)
    const res = await updateTable(id, {
      qrCode: qrCodeImgUrl
    })
    if (res) {
      message('生成二维码成功', { type: 'success' })
      loadTables()
    }
  } catch (err) {
    console.error(err)
    message(err.message || '生成失败', { type: 'error' })
  } finally {
    qrCodeLoading.value = false
  }
}

function refreshQRCode(id, tableCode, index) {
  tableData.value[index].qrCode = ''
  generateQRCode(id, tableCode)
}

onMounted(() => {
  loadTables();
});
</script>

<style scoped></style>
