<script setup lang="ts">
import { leftLikeDicts, saveDict } from "@/api/dict";
import { ElMessage } from "element-plus";
import { ref } from "vue";
import ChoosePosition from "@/components/ChoosePosition/index.vue";

defineOptions({
  name: "MerchantInfo"
});

const uploadAction = `${import.meta.env.VITE_APP_BASE_API}/upload/image`;
const loading = ref(false);
const shopName = ref<Record<string, any>>({});
const shopDesc = ref<Record<string, any>>({});
const businessHours = ref<Record<string, any>>({});
const shopLocation = ref<Record<string, any>>({});
const chooseLngLat = ref<Record<string, any>>({});
const shopPhone = ref<Record<string, any>>({});
const paySwitch = ref<Record<string, any>>({});
const orderTip = ref<Record<string, any>>({});

// 地图选点对话框
const mapDialogVisible = ref(false);

// 营业时间数据结构
const weekDays = [
  { label: "周一", value: 1 },
  { label: "周二", value: 2 },
  { label: "周三", value: 3 },
  { label: "周四", value: 4 },
  { label: "周五", value: 5 },
  { label: "周六", value: 6 },
  { label: "周日", value: 0 }
];

const businessHoursData = ref<{
  weekDays: number[];
  startTime: string;
  endTime: string;
}>({
  weekDays: [],
  startTime: "",
  endTime: ""
});

const chooseLngLatData = ref<{
  lat?: number;
  lng?: number;
}>({});

const merchantImage = ref("");
const merchantBgImage = ref("");

// 加载商家信息配置
async function loadMerchantInfo() {
  const res = await leftLikeDicts([
    "shop_name",
    "shop_desc",
    "business_hours",
    "shop_location",
    "choose_lng_lat",
    "shop_image",
    "shop_phone",
    "shop_bg_image",
    "pay_switch",
    "order_tip",
  ]);
  shopName.value = res[0]?.[0] || {
    dictKey: "shop_name",
    dictValue: ""
  };
  shopDesc.value = res[1]?.[0] || {
    dictKey: "shop_desc",
    dictValue: ""
  };
  businessHours.value = res[2]?.[0] || {
    dictKey: "business_hours",
    dictValue: ""
  };
  shopLocation.value = res[3]?.[0] || {
    dictKey: "shop_location",
    dictValue: ""
  };
  chooseLngLat.value = res[4]?.[0] || {
    dictKey: "choose_lng_lat",
    dictValue: ""
  };
  merchantImage.value = res[5]?.[0].dictValue || "";
  shopPhone.value = res[6]?.[0] || {
    dictKey: "shop_phone",
    dictValue: ""
  };
  merchantBgImage.value = res[7]?.[0].dictValue || "";
  paySwitch.value = res[8]?.[0] || {
    dictKey: "pay_switch",
    dictValue: "0"
  };
  orderTip.value = res[9]?.[0] || {
    dictKey: "order_tip",
    dictValue: "0"
  };

  // 解析营业时间数据
  if (businessHours.value.dictValue) {
    try {
      const parsed = JSON.parse(businessHours.value.dictValue);
      businessHoursData.value = {
        weekDays: parsed.weekDays || [],
        startTime: parsed.startTime || "",
        endTime: parsed.endTime || ""
      };
    } catch (e) {
      console.error("解析营业时间失败", e);
    }
  }
  if (chooseLngLat.value.dictValue) {
    try {
      const parsed = JSON.parse(chooseLngLat.value.dictValue);
      chooseLngLatData.value = {
        lat: parsed.lat || null,
        lng: parsed.lng || null
      };
    } catch (e) {
      console.error("解析经纬度失败", e);
    }
  }
}

function chooseEnd(back) {
  const { chooseLngLat: v, queryDetails } = back;
  if (v && queryDetails) {
    chooseLngLatData.value = {
      lat: v.lat || null,
      lng: v.lng || null
    };
    shopLocation.value.dictValue = queryDetails;
  }
  mapDialogVisible.value = false;
}

// 保存商家信息
async function onSave() {
  try {
    loading.value = true;

    // 将营业时间数据序列化为 JSON
    const businessHoursJson = JSON.stringify(businessHoursData.value);
    const chooseLngLatDataJson = JSON.stringify(chooseLngLatData.value);

    await saveDict([
      {
        dictKey: shopName.value.dictKey,
        dictValue: shopName.value.dictValue
      },
      {
        dictKey: shopDesc.value.dictKey,
        dictValue: shopDesc.value.dictValue
      },
      {
        dictKey: businessHours.value.dictKey,
        dictValue: businessHoursJson
      },
      {
        dictKey: shopLocation.value.dictKey,
        dictValue: shopLocation.value.dictValue
      },
      {
        dictKey: chooseLngLat.value.dictKey,
        dictValue: chooseLngLatDataJson
      },
      {
        dictKey: "shop_image",
        dictValue: merchantImage.value
      },
      {
        dictKey: shopPhone.value.dictKey,
        dictValue: shopPhone.value.dictValue
      },
      {
        dictKey: "shop_bg_image",
        dictValue: merchantBgImage.value
      },
      {
        dictKey: paySwitch.value.dictKey,
        dictValue: paySwitch.value.dictValue
      },
      {
        dictKey: orderTip.value.dictKey,
        dictValue: orderTip.value.dictValue
      },
    ]);
    ElMessage.success("保存成功");
  } catch (error) {
    ElMessage.error("保存失败");
  } finally {
    loading.value = false;
  }
}

function handleFollowUploadSuccess(response: any) {
  if (response) {
    merchantImage.value = response;
    ElMessage.success("图片上传成功");
  } else {
    ElMessage.error("图片上传失败");
  }
}
function handleFollowUploadSuccessBg(response: any) {
  if (response) {
    merchantBgImage.value = response;
    ElMessage.success("图片上传成功");
  } else {
    ElMessage.error("图片上传失败");
  }
}

function onProgress(evt, uploadFile, uploadFiles) {
  console.log(evt, uploadFile, uploadFiles);

}

// 初始化加载数据
loadMerchantInfo();
</script>

<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <p class="text-primary">商家信息配置</p>
        </div>
      </template>
      <el-form label-width="120px">
        <el-form-item label="店铺名称">
          <el-input v-model="shopName.dictValue" placeholder="请输入店铺名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="shopPhone.dictValue" placeholder="请输入联系电话" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="支付开关">
          <div>
            <el-switch v-model="paySwitch.dictValue" active-value="1" inactive-value="0" active-text="开启支付"
              inactive-text="关闭支付" />
            <p class="text-red-400">关闭支付功能后，所有商品将开启免支付下单模式。</p>
            <p class="text-red-400">用户提交订单后，系统会自动标记为已支付状态，且不支持发起退款。该功能仅适用于 DEMO 演示，生产环境中请谨慎操作。</p>
          </div>
        </el-form-item>
        <el-form-item label="下单文字说明">
          <div>
            <el-select v-model="orderTip.dictValue" placeholder="请选择文字说明方式" style="width: 160px;">
              <el-option label="通俗说法" value="0"></el-option>
              <el-option label="餐饮说法" value="1"></el-option>
            </el-select>
            <p class="text-red-400">通俗说法——下单号、店内下单等</p>
            <p class="text-red-400">餐饮说法——取餐号、店内就餐等</p>
          </div>
        </el-form-item>
        <el-form-item label="店铺头像">
          <div class="flex flex-col gap-4">
            <el-upload :action="uploadAction" :show-file-list="false" :on-success="handleFollowUploadSuccess"
              :on-progress="onProgress" accept="image/*">
              <el-button type="primary" plain>点击上传</el-button>
            </el-upload>
            <view v-if="merchantImage">
              <el-image :src="merchantImage" class="size-32 rounded-md" :preview-src-list="[merchantImage]" fit="cover"
                preview-teleported />
            </view>
          </div>
        </el-form-item>
        <el-form-item label="店铺背景图片">
          <div class="flex flex-col gap-4">
            <el-upload :action="uploadAction" :show-file-list="false" :on-success="handleFollowUploadSuccessBg"
              accept="image/*">
              <el-button type="primary" plain>点击上传</el-button>
            </el-upload>
            <view v-if="merchantBgImage">
              <el-image :src="merchantBgImage" class="w-64 rounded-md" :preview-src-list="[merchantBgImage]" fit="cover"
                preview-teleported />
            </view>
          </div>
        </el-form-item>

        <el-form-item label="店铺描述">
          <el-input v-model="shopDesc.dictValue" type="textarea" :rows="4" placeholder="请输入店铺描述，简要介绍您的店铺特色和服务"
            maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="营业时间">
          <div class="space-y-3">
            <div>
              <div class="mb-2 text-sm text-gray-600">营业日期</div>
              <el-checkbox-group v-model="businessHoursData.weekDays">
                <el-checkbox v-for="day in weekDays" :key="day.value" :value="day.value">
                  {{ day.label }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
            <div>
              <div class="mb-2 text-sm text-gray-600">营业时段</div>
              <div class="flex items-center gap-2">
                <el-time-picker v-model="businessHoursData.startTime" placeholder="开始时间" format="HH:mm"
                  value-format="HH:mm" />
                <span>至</span>
                <el-time-picker v-model="businessHoursData.endTime" placeholder="结束时间" format="HH:mm"
                  value-format="HH:mm" />
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="商家定位">
          <div class="w-full">
            <el-input v-model="shopLocation.dictValue" type="textarea" :rows="3"
              placeholder="点击下方按钮在地图上选择位置（定位后可再修改详细地址）" :readonly="!(chooseLngLatData.lng && chooseLngLatData.lat)" />
            <div v-if="chooseLngLatData.lng && chooseLngLatData.lat" class="flex gap-2 mt-2">
              <el-tag>
                <span>经度：</span>
                <span>{{ chooseLngLatData.lng }}</span>
              </el-tag>
              <el-tag>
                <span>纬度：</span>
                <span>{{ chooseLngLatData.lat }}</span>
              </el-tag>
            </div>
            <div class="mt-2">
              <ChoosePosition @chooseEnd="chooseEnd" />
            </div>
          </div>
        </el-form-item>

        <!-- <el-form-item label="距离"> </el-form-item> -->
      </el-form>

      <div class="text-right">
        <el-button type="primary" :loading="loading" @click="onSave">
          保存
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.text-primary {
  color: #409eff;
  font-weight: 600;
}
</style>
