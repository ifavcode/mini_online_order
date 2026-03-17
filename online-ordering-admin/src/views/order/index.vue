<template>
  <div>
    <audio src="https://www.guetzjb.cn/assets_other/tip.mp3" ref="audioRef"></audio>
    <div class="bg-white rounded-lg mb-4">
      <div class="flex justify-between items-center">
        <div class="flex gap-3 flex-wrap">
          <el-input v-model="searchQuery.orderNo" placeholder="搜索取餐号" style="width: 150px;" clearable
            @keyup.enter="handleSearch">
            <template #prefix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
          <el-input v-model="searchQuery.phone" placeholder="搜索手机号" style="width: 150px;" clearable
            @keyup.enter="handleSearch">
            <template #prefix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
          <el-select v-model="searchQuery.status" placeholder="订单状态" clearable style="width: 150px;">
            <el-option label="待支付" :value="1" />
            <el-option label="已支付" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="已退款" :value="5" />
          </el-select>
          <el-select v-model="searchQuery.orderType" placeholder="订单类型" clearable style="width: 150px;">
            <el-option label="堂食" value="dinein" />
            <el-option label="外带" value="pickup" />
          </el-select>
          <el-input v-model="searchQuery.openid" placeholder="openid" style="width: 300px;" clearable
            @keyup.enter="handleSearch">
            <template #prefix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
          <el-date-picker v-model="datetimerange" type="datetimerange" style="max-width: 400px;" range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期" :disabled-date="disabledDate" :shortcuts="shortcuts"
            format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" @change="handleDateChange" clearable />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset" style="margin-left: 0px">
            重置 & 刷新
          </el-button>
        </div>
      </div>
      <div class="mt-4">
        <el-button type="primary" @click="handleAdd">
          <el-icon class="mr-1">
            <Plus />
          </el-icon>
          新增订单
        </el-button>
        <el-button type="danger" @click="handledeleteBatch" :disabled="multipleSelection.length === 0">
          <el-icon class="mr-1">
            <Plus />
          </el-icon>
          删除订单
        </el-button>
        <div class="my-4 text-sm flex gap-2 items-center">
          <p v-if="!systemStore.es" class="text-red-500 select-none">未连接到服务器</p>
          <p v-else class="text-green-500 select-none">已连接到服务器，收到新订单自动刷新</p>
          <el-button type="primary" @click="systemStore.initWs()" v-if="systemStore.es === null">重新连接</el-button>
        </div>
      </div>
      <div class="mt-4">
        <p class="text-red-500 text-sm">取餐号默认每日刷新，从0001开始递增</p>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="bg-white rounded-lg">
      <el-table :data="tableData" v-loading="loading" border :row-style="getRowStyle"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="取餐号" width="120">
          <template #default="{ row }">
            <span class="font-medium">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单号">
          <template #default="{ row }">
            <span class="font-medium">{{ row.wxOrderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="桌号" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.table" type="success">
              {{ row.table.tableName }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="订单类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.orderType === 'dinein' ? 'success' : 'warning'">
              {{ row.orderType === 'dinein' ? '堂食' : '外带' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            <span class="text-red-500 font-medium">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商品数量" width="100">
          <template #default="{ row }">
            <p>{{ row.orderItems?.length || 0 }} 项</p>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <div class="flex items-center">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
              <p class="pl-2" v-if="row.status === Constant.ORDERS_ENUM.PAID">
                <el-popconfirm title="确定要将该订单标记为完成吗？" confirm-button-text="确认" cancel-button-text="取消"
                  @confirm="handleFinish(row)">
                  <template #reference>
                    <el-button link size="small">
                      <span class="hover:text-primary">完成</span>
                    </el-button>
                  </template>
                </el-popconfirm>
              </p>
              <p class="text-gray-500 text-xs pl-2" v-if="row.status === Constant.ORDERS_ENUM.PENDING_PAYMENT">等待支付</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="取餐时间" width="120">
          <template #default="{ row }">
            <p>{{ row.pickupTime }}</p>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            <p>{{ formatDateWithRelativeTime(row.createdAt) }}</p>
          </template>
        </el-table-column>
        <el-table-column label="手机号 & 备注" min-width="120" fixed="right">
          <template #default="{ row }">
            <div>
              <p>{{ row.phone || '-' }}</p>
              <p>{{ row.remark || '-' }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-1">
              <div class="pr-2">
                <el-tooltip content="查看详情" placement="top">
                  <el-button circle size="small" type="primary" plain @click="handleView(row)">
                    <el-icon>
                      <View />
                    </el-icon>
                  </el-button>
                </el-tooltip>
              </div>

              <div class="pr-2">
                <el-tooltip content="编辑订单" placement="top">
                  <el-button circle size="small" type="info" plain @click="handleEdit(row)">
                    <el-icon>
                      <EditPen />
                    </el-icon>
                  </el-button>
                </el-tooltip>
              </div>

              <el-popconfirm title="确定要删除该订单吗？" confirm-button-text="删除" cancel-button-text="取消"
                confirm-button-type="danger" @confirm="handleDelete(row)">
                <template #reference>
                  <div class="pr-2">
                    <el-tooltip content="删除订单" placement="top">
                      <el-button circle size="small" type="danger" plain>
                        <el-icon>
                          <Delete />
                        </el-icon>
                      </el-button>
                    </el-tooltip>
                  </div>
                </template>
              </el-popconfirm>

              <div
                v-if="(row.status === Constant.ORDERS_ENUM.PAID || row.status === Constant.ORDERS_ENUM.COMPLETED) && row.wxOrderNo"
                class="pr-2">
                <el-tooltip content="申请退款" placement="top">
                  <el-button circle size="small" type="warning" plain @click="handleRefund(row)">
                    <el-icon>
                      <Money />
                    </el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
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
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增订单' : '编辑订单'" width="900px"
      :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="订单类型" prop="orderType">
          <el-radio-group v-model="formData.orderType">
            <el-radio value="dinein">堂食</el-radio>
            <el-radio value="pickup">外带</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="订单状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择订单状态">
            <el-option v-for="(text, value) in statusMap" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="取餐时间" prop="pickupTime">
          <el-input v-model="formData.pickupTime" placeholder="如：立刻制作、30分钟后" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注信息" maxlength="200"
            show-word-limit />
        </el-form-item>
        <el-form-item label="选择商品">
          <ChooseGoods />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="800px" :close-on-click-modal="false">
      <div v-if="currentOrder" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <span class="text-gray-500">取餐号：</span>
            <span class="font-medium">{{ currentOrder.orderNo }}</span>
          </div>
          <div>
            <span class="text-gray-500">订单状态：</span>
            <el-tag :type="getStatusType(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </div>
          <div>
            <span class="text-gray-500">订单类型：</span>
            <el-tag :type="currentOrder.orderType === 'dinein' ? 'success' : 'warning'">
              {{ currentOrder.orderType === 'dinein' ? '堂食' : '外带' }}
            </el-tag>
          </div>
          <div>
            <span class="text-gray-500">桌号：</span>
            <span>{{ currentOrder.table?.tableName || '-' }}</span>
          </div>
          <div>
            <span class="text-gray-500">取餐时间：</span>
            <span>{{ currentOrder.pickupTime }}</span>
          </div>
          <div>
            <span class="text-gray-500">手机号：</span>
            <span>{{ currentOrder.phone || '-' }}</span>
          </div>
          <div>
            <span class="text-gray-500">总金额：</span>
            <span class="text-red-500 font-medium text-lg">
              ¥{{ currentOrder.totalAmount }}
            </span>
          </div>
          <div>
            <span class="text-gray-500">创建时间：</span>
            <span>{{ currentOrder.createdAt }}</span>
          </div>
        </div>
        <div v-if="currentOrder.remark">
          <span class="text-gray-500">备注：</span>
          <p class="mt-1 text-gray-700">{{ currentOrder.remark }}</p>
        </div>
        <el-divider />
        <div>
          <h4 class="font-medium text-base">下单人信息</h4>
          <p class="pt-1">
            <span>名称：</span>
            <span>{{ currentOrder.sysUser?.nickname }}</span>
          </p>
          <p>
            <span>openid：</span>
            <span>{{ currentOrder.sysUser?.openid }}</span>
          </p>
        </div>
        <el-divider />
        <div>
          <h4 class="font-medium text-base pb-1">商品明细</h4>
          <el-table :data="currentOrder.orderItems" border stripe>
            <el-table-column label="商品图" min-width="100">
              <template #default="{ row }">
                <el-image :src="row.goods?.imgUrl" alt="" :preview-src-list="[row.goods?.imgUrl]" fit="cover"
                  preview-teleported />
              </template>
            </el-table-column>
            <el-table-column label="商品名称" min-width="150">
              <template #default="{ row }">
                {{ row.goods?.name }}
              </template>
            </el-table-column>
            <el-table-column label="单价" width="100">
              <template #default="{ row }">
                ¥{{ row.unitPrice }}
              </template>
            </el-table-column>
            <el-table-column label="数量" width="80">
              <template #default="{ row }">
                {{ row.count }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100">
              <template #default="{ row }">
                <span class="text-red-500">¥{{ row.subtotal }}</span>
              </template>
            </el-table-column>
            <el-table-column label="规格" min-width="200">
              <template #default="{ row }">
                <div v-if="row.specGroupJson && row.specGroupJson !== '[]'">
                  <el-tag v-for="(spec, idx) in parseSpecs(row.specGroupJson)" :key="idx" size="small"
                    class="mr-1 mb-1">
                    {{ spec }}
                  </el-tag>
                </div>
                <span v-else class="text-gray-400">无规格</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeDetails">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 退款对话框 -->
    <el-dialog v-model="refundDialogVisible" title="申请退款" width="460px" :close-on-click-modal="false">
      <el-form ref="refundFormRef" :model="refundForm" :rules="refundRules" label-width="90px">
        <el-form-item label="订单金额">
          <span class="text-red-500 font-medium">
            ¥{{ currentRefundOrder?.totalAmount || 0 }}
          </span>
        </el-form-item>
        <el-form-item label="退款金额" prop="amount">
          <el-input v-model="refundForm.amount" placeholder="请输入退款金额" type="number" :min="0" step="0.01">
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" placeholder="非必填信息，若填写将展示在下发给用户的退款消息中"
            maxlength="100" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="refundLoading" @click="handleRefundSubmit">
          确认退款
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, Plus, View, EditPen, Delete, Money } from "@element-plus/icons-vue";
import { calcGoodsTotalPrice, formatDateWithRelativeTime, shortcuts } from '../../utils/index';
import {
  getOrdersPageAdmin,
  createOrdersAdmin,
  updateOrdersAdmin,
  deleteOrdersAdmin,
  deleteOrdersAdminBatch,
  updateOrdersStatusAdmin,
  refundOrderAdmin
} from "@/api/orders";
import ChooseGoods from '@/components/ChooseGoods/index.vue'
import { useGoodsStore } from "@/store/modules/goods";
import { storeToRefs } from "pinia";
import { useSystemStoreHook } from "@/store/modules/system";
import { emitter } from "@/utils/mitt";
import Constant from '@/utils/Constant'

const goodsStore = useGoodsStore()
const { selectedGoods } = storeToRefs(goodsStore)

// 表格数据
const tableData = ref([]);
const loading = ref(false);

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
});

// 搜索
const searchQuery = reactive({
  orderNo: "",
  phone: "",
  status: null,
  orderType: "",
  startTime: "",
  endTime: "",
  openid: ""
});

// 对话框
const dialogVisible = ref(false);
const detailVisible = ref(false);
const refundDialogVisible = ref(false);
const dialogType = ref("add"); // 'add' | 'edit'
const submitLoading = ref(false);
const currentOrder = ref(null);
const currentRefundOrder = ref(null);
const datetimerange = ref([])
const multipleSelection = ref([])
const systemStore = useSystemStoreHook()
const highlightOrderId = ref(null)
const audioRef = ref()

const refundFormRef = ref()
const refundForm = reactive({
  amount: "",
  reason: ""
})
const refundLoading = ref(false)

const handleSelectionChange = (val) => {
  multipleSelection.value = val.map(order => {
    return order.id
  })
}

// 表单
const formRef = ref();
const formData = reactive({
  id: null,
  orderType: "dinein",
  status: 1,
  pickupTime: "立刻制作",
  phone: "",
  remark: ""
});

const formRules = {
  orderType: [
    { required: true, message: "请选择订单类型", trigger: "change" }
  ],
  status: [{ required: true, message: "请选择订单状态", trigger: "change" }],
  pickupTime: [
    { required: true, message: "请输入取餐时间", trigger: "blur" }
  ]
};

const validateRefundAmount = (rule, value, callback) => {
  if (value === "" || value === null || value === undefined) {
    return callback(new Error("请输入退款金额"));
  }
  const num = Number(value);
  if (Number.isNaN(num) || num <= 0) {
    return callback(new Error("退款金额必须大于 0"));
  }
  const max = currentRefundOrder.value ? Number(currentRefundOrder.value.totalAmount) : 0;
  if (num > max) {
    return callback(new Error(`退款金额不能大于支付金额（¥${max}）`));
  }
  callback();
};

const refundRules = {
  amount: [
    { required: true, message: "请输入退款金额", trigger: "blur" },
    { validator: validateRefundAmount, trigger: "blur" }
  ],
  reason: [
    { required: false, message: "请输入退款原因", trigger: "blur" }
  ]
};

const statusMap = {
  1: "待支付",
  2: "已支付",
  3: "已完成",
  4: "已取消",
  5: "已退款"
};

// 状态映射
const getStatusText = status => {
  return statusMap[status] || "未知";
};

const getStatusType = status => {
  const typeMap = {
    1: "warning",
    2: "primary",
    3: "success",
    4: "info"
  };
  return typeMap[status] || "info";
};

const handleDateChange = (val) => {
  datetimerange.value = val
};

const disabledDate = (time) => {
  return time.getTime() > Date.now();
};

// 解析规格JSON
const parseSpecs = specGroupJson => {
  try {
    const specs = JSON.parse(specGroupJson);
    return specs
      .map(group => {
        const items = group.specItems
          ?.map(item => item.name)
          .filter(Boolean)
          .join("、");
        return items ? `${group.name}: ${items}` : "";
      })
      .filter(Boolean);
  } catch {
    return [];
  }
};

function closeDetails() {
  detailVisible.value = false
  selectedGoods.value = []
}
// 加载订单列表
const loadOrders = async (flag = false) => {
  if (!flag) {
    loading.value = true;
  }
  searchQuery.startTime = datetimerange.value[0] || ""
  searchQuery.endTime = datetimerange.value[0] ? (datetimerange.value[1] || "") : ""
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...Object.fromEntries(
        Object.entries(searchQuery).filter(([_, v]) => v !== "" && v !== null)
      )
    };
    const res = await getOrdersPageAdmin(params);
    tableData.value = res.content;
    pagination.total = res.totalElements;
  } catch (error) {
    ElMessage.error("加载订单失败");
    console.error("加载订单失败:", error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  loadOrders();
};

// 重置
const handleReset = () => {
  searchQuery.orderNo = "";
  searchQuery.phone = "";
  searchQuery.status = null;
  searchQuery.orderType = "";
  searchQuery.startTime = ""
  searchQuery.endTime = ""
  searchQuery.openid = ""
  pagination.page = 1;
  loadOrders();
};

// 分页变化
const handlePageChange = () => {
  loadOrders();
};

const handleSizeChange = () => {
  pagination.page = 1;
  loadOrders();
};

// 新增
const handleAdd = () => {
  selectedGoods.value = []
  dialogType.value = "add";
  Object.assign(formData, {
    id: null,
    orderType: "dinein",
    status: 1,
    pickupTime: "立刻制作",
    phone: "",
    remark: ""
  });
  dialogVisible.value = true;
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 编辑
const handleEdit = row => {
  dialogType.value = "edit";
  Object.assign(formData, {
    id: row.id,
    orderType: row.orderType,
    status: row.status,
    pickupTime: row.pickupTime,
    phone: row.phone,
    remark: row.remark,
    orderItems: row.orderItems
  });
  dialogVisible.value = true;
  if (formRef.value) {
    formRef.value.clearValidate();
  }
  selectedGoods.value = row.orderItems.reduce((pre, orderItem) => {
    pre.push({
      orderItemId: orderItem.id,
      count: orderItem.count,
      ...orderItem.goods,
      specGroups: JSON.parse(orderItem.specGroupJson),
      totalPrice: orderItem.subtotal * orderItem.count
    })
    return pre
  }, [])
};

// 订单状态更新
async function handleFinish(row) {
  await updateOrdersStatusAdmin(row.id, {
    status: Constant.ORDERS_ENUM.COMPLETED,
  });
  loadOrders();
  ElMessage.success("更新成功");
}

// 查看详情
const handleView = row => {
  currentOrder.value = row;
  detailVisible.value = true;
};

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async valid => {
    if (!valid) return;

    submitLoading.value = true;
    try {
      if (dialogType.value === "add") {
        const orderItems = selectedGoods.value.reduce((pre, goods) => {
          pre.push({
            unitPrice: goods.basePrice,
            subtotal: calcGoodsTotalPrice(goods),
            specGroupJson: JSON.stringify(goods.specGroups),
            count: goods.count * 1,
            goods: {
              id: goods.id
            }
          })
          return pre
        }, [])
        const res = await createOrdersAdmin({
          orderType: formData.orderType,
          status: formData.status,
          pickupTime: formData.pickupTime,
          phone: formData.phone,
          remark: formData.remark,
          totalAmount: selectedGoods.value.reduce((sum, item) => sum + item.totalPrice, 0),
          orderItems,
        });
        if (res.success) {
          ElMessage.success("新增成功");
        } else {
          ElMessage.error("新增失败")
        }
      } else {
        let orderItems = selectedGoods.value.reduce((pre, goods) => {
          pre.push({
            id: goods.orderItemId || null,
            unitPrice: goods.basePrice,
            subtotal: calcGoodsTotalPrice(goods),
            specGroupJson: JSON.stringify(goods.specGroups),
            count: goods.count * 1,
            goods: {
              id: goods.id
            }
          })
          return pre
        }, [])
        await updateOrdersAdmin(formData.id, {
          orderType: formData.orderType,
          status: formData.status,
          pickupTime: formData.pickupTime,
          phone: formData.phone,
          remark: formData.remark,
          totalAmount: selectedGoods.value.reduce((sum, item) => sum + item.totalPrice, 0),
          orderItems
        });
        ElMessage.success("更新成功");
      }
      dialogVisible.value = false;
      selectedGoods.value = []
      loadOrders();
    } catch (error) {
      ElMessage.error(dialogType.value === "add" ? (error.response.data || "新增失败") : "更新失败");
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
      `确定要删除订单"${row.orderNo}"吗？删除后将无法恢复。`,
      "警告",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    loading.value = true;
    await deleteOrdersAdmin(row.id);
    ElMessage.success("删除成功");

    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--;
    }
    loadOrders();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败");
      console.error("删除失败:", error);
    }
  } finally {
    loading.value = false;
  }
};

async function handleRefund(row) {
  currentRefundOrder.value = row;
  refundForm.amount = row.totalAmount;
  refundForm.reason = "";
  refundDialogVisible.value = true;
  await nextTick();
  if (refundFormRef.value) {
    refundFormRef.value.clearValidate();
  }
}

const handleRefundSubmit = async () => {
  if (!refundFormRef.value || !currentRefundOrder.value) return;

  refundFormRef.value.validate(async valid => {
    if (!valid) return;

    refundLoading.value = true;
    try {
      await refundOrderAdmin(currentRefundOrder.value.wxOrderNo, refundForm.amount, refundForm.reason)
      ElMessage.success("已提交退款申请");
      refundDialogVisible.value = false;
    } catch (error) {
      ElMessage.error(error.response.data || "退款失败");
      console.error("退款失败:", error);
    } finally {
      refundLoading.value = false;
    }
  });
};

async function handledeleteBatch() {
  try {
    await ElMessageBox.confirm(
      `确定要删除订单选中订单？删除后将无法恢复。`,
      "警告",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }
    );

    loading.value = true;
    await deleteOrdersAdminBatch(multipleSelection.value.join(","))
    ElMessage.success("删除成功");
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--;
    }
    multipleSelection.value = []
    loadOrders();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败");
      console.error("删除失败:", error);
    }
  } finally {
    loading.value = false;
  }
}

const getRowStyle = ({ row }) => {
  if (row.id === highlightOrderId.value) {
    return {
      animation: 'flash-animation 1s ease-in-out 6'
    };
  }
  return {};
};

let timer
function autoRefresh() {
  clearInterval(timer)
  timer = setInterval(() => {
    loadOrders(true)
  }, 1000 * 30);
}

onMounted(() => {
  loadOrders();
  autoRefresh()
  emitter.on('refreshOrders', async (ordersId) => {
    pagination.page = 1
    clearInterval(timer)
    await loadOrders()
    await nextTick()
    highlightOrderId.value = ordersId
    audioRef.value.play()
    autoRefresh()
    setTimeout(() => {
      if (highlightOrderId.value === ordersId) {
        highlightOrderId.value = null
      }
    }, 6000);
  })
});

onBeforeUnmount(() => {
  emitter.off('refreshOrders')
  clearInterval(timer)
})
</script>

<style lang="scss">
.highlight-row {
  animation: flash-animation 1s ease-in-out 5 !important;
  /* 闪烁2次，每次1秒 */
}

@keyframes flash-animation {

  0%,
  100% {
    background-color: #e6f7ff;
  }

  50% {
    background-color: #bae6fd;
  }
}
</style>
