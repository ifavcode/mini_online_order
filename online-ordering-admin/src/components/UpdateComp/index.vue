<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElDialog, ElButton } from 'element-plus';
import Cookies from 'js-cookie';
import { getRemoteVersion } from '@/api/system';
import Constant from '@/utils/Constant';

defineOptions({ name: "VersionUpdate" });

const showUpdateDialog = ref(false);
const newVersion = ref('');

// 版本比较函数
function compareVersion(v1: string, v2: string): number {
  const parts1 = v1.split('.').map(Number);
  const parts2 = v2.split('.').map(Number);

  for (let i = 0; i < Math.max(parts1.length, parts2.length); i++) {
    const num1 = parts1[i] || 0;
    const num2 = parts2[i] || 0;

    if (num1 < num2) return -1;
    if (num1 > num2) return 1;
  }

  return 0;
}

async function checkVersion() {
  try {
    // 检查用户是否在3天内选择了不再提示
    const dismissedVersion = Cookies.get('dismissed_version_update');
    const res = await getRemoteVersion();
    const curVersion: string = res.data;
    newVersion.value = curVersion;
    // 如果当前版本小于服务器版本，且用户没有在3天内忽略此版本
    if (compareVersion(Constant.VERSION, curVersion) < 0 && dismissedVersion !== curVersion) {
      showUpdateDialog.value = true;
    }
  } catch (error) {
    console.error('版本检查失败:', error);
  }
}

// 不再提示（3天后重新提示）
function dismissUpdate() {
  Cookies.set('dismissed_version_update', newVersion.value, { expires: 3 });
  showUpdateDialog.value = false;
}

// 关闭对话框
function closeDialog() {
  showUpdateDialog.value = false;
}

// 暴露给父组件使用
defineExpose({ showUpdateDialog, newVersion, compareVersion });

onMounted(() => {
  checkVersion();
});
</script>

<template>
  <!-- 更新提示角标，可插入到父组件标题旁 -->
  <span v-if="compareVersion(Constant.VERSION, newVersion) < 0"
    class="text-xs font-thin text-white rounded-2xl inline-block py-1 px-2 bg-red-500 cursor-pointer hover:bg-red-400"
    @click="showUpdateDialog = true">
    发现可用更新
  </span>
  <span v-else class="text-xs font-thin text-white rounded-2xl inline-block py-1 px-2 bg-green-500">
    最新
  </span>

  <!-- 更新弹窗 -->
  <ElDialog v-model="showUpdateDialog" title="发现新版本" width="450px" :close-on-click-modal="false" class="update-dialog">
    <div class="flex flex-col gap-4">
      <div class="flex items-center gap-3">
        <div class="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center">
          <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        <div>
          <p class="text-gray-700 font-medium">当前版本：{{ Constant.VERSION }}</p>
          <p class="text-blue-600 font-semibold">最新版本：{{ newVersion }}</p>
        </div>
      </div>

      <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
        <p class="text-sm text-gray-700">
          🎉 有可用更新！请联系作者获取最新版本，体验新功能和性能优化。
          <a href="https://www.guetzjb.cn/public/update-order.pdf" target="_blank">
            <span class="underline text-primary">查看更新日志</span>
          </a>
        </p>
      </div>
    </div>

    <template #footer>
      <div class="flex justify-between items-center">
        <ElButton text @click="dismissUpdate" class="text-gray-500 hover:text-gray-700">
          3天内不再提示
        </ElButton>
        <ElButton type="primary" @click="closeDialog">
          我知道了
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<style scoped>
.update-dialog :deep(.el-dialog__header) {
  padding: 20px 20px 10px;
}

.update-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.update-dialog :deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
}
</style>