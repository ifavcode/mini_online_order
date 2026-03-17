<template>
  <div class="">
    <!-- 头部操作区 -->
    <div class="bg-white rounded-lg p-4 mb-4">
      <div class="flex justify-between items-center">
        <div class="flex gap-3">
          <el-input v-model="searchQuery.wxOrderNo" placeholder="搜索微信订单号（支持模糊）" class="w-80" clearable
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
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="bg-white rounded-lg">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="wxOrderNo" label="微信订单号" min-width="220" show-overflow-tooltip />
        <el-table-column prop="refundId" label="退款单号" min-width="220" show-overflow-tooltip />
        <el-table-column label="订单金额" width="120">
          <template #default="{ row }">
            <span class="text-gray-700">¥{{ formatFee(row.totalFee) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="退款金额" width="120">
          <template #default="{ row }">
            <span class="text-red-500 font-semibold">¥{{ formatFee(row.refundFee) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="退款状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getRefundStatusType(row.refundStatus)" size="small">
              {{ getRefundStatusText(row.refundStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成功时间" width="180">
          <template #default="{ row }">
            {{ row.successTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="refundRecvAccout" label="入账账户" min-width="180" show-overflow-tooltip />
        <el-table-column prop="refundAccount" label="退款资金来源" min-width="160" show-overflow-tooltip />
        <el-table-column prop="refundRequestSource" label="退款发起来源" min-width="160" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <div class="p-4 flex justify-end">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handlePageChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import { getRefundRecordPage } from "@/api/refund";

// 表格数据
const tableData = ref([]);
const loading = ref(false);

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

// 搜索，仅支持 wxOrderNo 模糊查询（后续你可自行扩展）
const searchQuery = reactive({
  wxOrderNo: ""
});

// 金额为分，转元并保留 2 位小数
const formatFee = (fee) => {
  if (fee == null || isNaN(Number(fee))) return "0.00";
  return (Number(fee) / 100).toFixed(2);
};

// 退款状态展示
const getRefundStatusText = (status) => {
  if (!status) return "未知";
  const map = {
    SUCCESS: "退款成功",
    CLOSED: "退款关闭",
    PROCESSING: "退款处理中",
    ABNORMAL: "退款异常"
  };
  return map[status] || status;
};

const getRefundStatusType = (status) => {
  const map = {
    SUCCESS: "success",
    CLOSED: "info",
    PROCESSING: "warning",
    ABNORMAL: "danger"
  };
  return map[status] || "info";
};

// 加载退款记录
const loadRefunds = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      wxOrderNo: searchQuery.wxOrderNo || ""
    };
    const res = await getRefundRecordPage(params);
    tableData.value = res.content;
    pagination.total = res.totalElements;
  } catch (error) {
    ElMessage.error("加载退款记录失败");
    console.error("加载退款记录失败:", error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadRefunds();
};

// 重置
const handleReset = () => {
  searchQuery.wxOrderNo = "";
  pagination.page = 1;
  loadRefunds();
};

// 分页变化
const handlePageChange = () => {
  loadRefunds();
};

const handleSizeChange = () => {
  pagination.page = 1;
  loadRefunds();
};

onMounted(() => {
  loadRefunds();
});
</script>

<style scoped></style>
