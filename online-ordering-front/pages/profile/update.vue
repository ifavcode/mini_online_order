<template>
  <view class="page-container" :style="{ paddingTop: safeArea?.top + 'px' }">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="handleBack">
        <uni-icons type="left" size="22" color="#333"></uni-icons>
      </view>
      <text class="nav-title">编辑资料</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 头像区域 -->
    <view class="avatar-section">
      <button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
        <view class="avatar-wrapper">
          <image class="avatar-img" :src="formData.avatar || defaultAvatar" mode="aspectFill" />
          <view class="avatar-overlay">
            <uni-icons type="camera-filled" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </button>
      <text class="avatar-tip">点击更换头像</text>
    </view>

    <!-- 表单区域 -->
    <view class="form-section">
      <!-- 昵称 -->
      <view class="form-item">
        <view class="form-label">
          <uni-icons type="person" size="18" color="#6366f1"></uni-icons>
          <text class="label-text">昵称</text>
        </view>
        <view class="form-input-wrap">
          <input class="form-input" type="nickname" v-model="formData.nickname" placeholder="请输入昵称"
            placeholder-class="input-placeholder" maxlength="16" @blur="onNicknameBlur" />
        </view>
      </view>

      <!-- 分割线 -->
      <view class="divider"></view>

      <!-- 手机号 -->
      <view class="form-item">
        <view class="form-label">
          <uni-icons type="phone" size="18" color="#6366f1"></uni-icons>
          <text class="label-text">手机号</text>
        </view>
        <view class="form-input-wrap">
          <input class="form-input" type="number" v-model="formData.phoneNumber" placeholder="请输入手机号"
            placeholder-class="input-placeholder" maxlength="11" />
        </view>
      </view>
    </view>

    <!-- 提示文字 -->
    <view class="tips-section">
      <text class="tips-text">以上信息均为选填，修改后请点击保存</text>
    </view>

    <!-- 保存按钮 -->
    <view class="btn-section">
      <button class="save-btn" :loading="saving" @click="handleSave"
        :style="{ background: saving ? '#ccc' : 'linear-gradient(135deg, #818cf8, #6366f1)', boxShadow: saving ? 'none' : '0 8rpx 24rpx rgba(99, 102, 241, 0.35)' }">
        {{ saving ? '保存中' : '保存修改' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useSystemStore } from '../../stores/system'
import { BASE_URL } from '../../utils/http'
import { updateUser } from '../../api/user'
import { useUserStore } from '../../stores/user'

const defaultAvatar = 'https://wx.qlogo.cn/mmhead/zsUXYY6y4cKMtXcxs4PFicvriayD87E3XuX9RKtoibICHkP3IOzS910Uic86tEbL8jk5lZanJcJBZrs/0'
const { safeArea } = useSystemStore()
const userStore = useUserStore()

const formData = reactive({
  avatar: '',
  nickname: '',
  phoneNumber: ''
})

const saving = ref(false)

// 选择头像回调
const onChooseAvatar = (e) => {
  const { avatarUrl } = e.detail
  if (avatarUrl) {
    formData.avatar = avatarUrl
  }
}

// 昵称输入完成
const onNicknameBlur = (e) => {
  formData.nickname = e.detail.value || ''
}

// 返回上一页
const handleBack = () => {
  uni.navigateBack({ delta: 1 })
}

// 保存用户信息
const handleSave = async () => {
  if (saving.value) {
    return
  }
  // 手机号格式校验（非空时）
  if (formData.phoneNumber && !/^1[3-9]\d{9}$/.test(formData.phoneNumber)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  // 如果所有字段都为空
  if (!formData.avatar && !formData.nickname && !formData.phoneNumber) {
    uni.showToast({ title: '请至少填写一项信息', icon: 'none' })
    return
  }
  if (formData.nickname === '未命名名称') {
    uni.showToast({ title: '不可使用此昵称', icon: 'none' })
    return
  }
  saving.value = true
  try {
    if (formData.avatar) {
      uni.uploadFile({
        url: BASE_URL + '/upload/image', filePath: formData.avatar, name: 'file',
        success(res) {
          uni.showToast({ title: '头像已更新', icon: 'none', duration: 1500 })
          updateUser({
            avatar: res.data,
            nickname: formData.nickname,
            phoneNumber: formData.phoneNumber
          }).then(res2 => {
            uni.showToast({ title: '保存成功', icon: 'success' })
            setTimeout(() => {
              uni.navigateBack({ delta: 1 })
            }, 1000)
          }).catch(err2 => {
            uni.showToast({ title: '保存失败，请重试', icon: 'none' })
          })
        },
        fail(err) {
          console.log(err);
          uni.showToast({
            title: '头像上传失败，请重试'
          })
        },
        complete() {
          saving.value = false
        }
      })
    } else {
      updateUser({
        avatar: defaultAvatar,
        nickname: formData.nickname,
        phoneNumber: formData.phoneNumber
      }).then(res2 => {
        uni.showToast({ title: '保存成功', icon: 'success' })
        const accessToken = uni.getStorageSync("accessToken");
        userStore.setUser(res2, "", res2.openid, accessToken)
        setTimeout(() => {
          uni.navigateBack({ delta: 1 })
        }, 600)
      }).catch(err2 => {
        console.error(err2)
        uni.showToast({ title: '保存失败，请重试', icon: 'none' })
      }).finally(() => {
        saving.value = false
      })
    }
  } catch (error) {
    console.error(error)
    uni.showToast({ title: '保存失败，请重试', icon: 'none' })
  }
}
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #eef2ff 0%, #f9fafb 40%);
  padding-bottom: env(safe-area-inset-bottom);
}

/* ========== 导航栏 ========== */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  height: 88rpx;
  margin-top: env(safe-area-inset-top);
  /* 等同于 Tailwind: items-center justify-between px-4 h-11 */
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.8);
  /* Tailwind: w-8 h-8 flex items-center justify-center rounded-full bg-white/80 */
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1f2937;
  /* Tailwind: text-lg font-semibold text-gray-800 */
}

.nav-placeholder {
  width: 64rpx;
}

/* ========== 头像区域 ========== */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 0 32rpx;
  /* Tailwind: flex flex-col items-center pt-6 pb-4 */
}

.avatar-btn {
  background: none !important;
  border: none;
  padding: 0;
  margin: 0;
  line-height: normal;
  /* 重置 button 默认样式 */
}

.avatar-btn::after {
  border: none;
}

.avatar-wrapper {
  position: relative;
  width: 176rpx;
  height: 176rpx;
  border-radius: 50%;
  /* Tailwind: relative w-22 h-22 rounded-full */
}

.avatar-img {
  width: 156rpx;
  height: 156rpx;
  border-radius: 50%;
  border: 6rpx solid #fff;
  /* Tailwind: w-22 h-22 rounded-full border-[3px] border-white shadow-lg */
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #818cf8, #6366f1);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid #fff;
  /* Tailwind: absolute bottom-0 right-0 w-[26px] h-[26px] rounded-full bg-indigo-500 flex items-center justify-center border-2 border-white */
}

.avatar-tip {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #9ca3af;
  /* Tailwind: mt-2 text-xs text-gray-400 */
}

/* ========== 表单区域 ========== */
.form-section {
  margin: 24rpx 32rpx 0;
  background-color: #fff;
  border-radius: 24rpx;
  padding: 0 32rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04);
  /* Tailwind: mx-4 mt-3 bg-white rounded-xl px-4 shadow-sm */
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 0;
  /* Tailwind: flex items-center justify-between py-4 */
}

.form-label {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  /* Tailwind: flex items-center shrink-0 */
}

.label-text {
  margin-left: 14rpx;
  font-size: 30rpx;
  font-weight: 500;
  color: #374151;
  /* Tailwind: ml-[7px] text-[15px] font-medium text-gray-700 */
}

.form-input-wrap {
  flex: 1;
  margin-left: 24rpx;
  /* Tailwind: flex-1 ml-3 */
}

.form-input {
  width: 100%;
  text-align: right;
  font-size: 30rpx;
  color: #1f2937;
  /* Tailwind: w-full text-right text-[15px] text-gray-800 */
}

.input-placeholder {
  color: #d1d5db;
  font-size: 28rpx;
  /* Tailwind: text-gray-300 text-sm */
}

.divider {
  height: 1rpx;
  background-color: #f3f4f6;
  margin: 0;
  /* Tailwind: h-px bg-gray-100 */
}

/* ========== 提示文字 ========== */
.tips-section {
  display: flex;
  justify-content: center;
  padding: 28rpx 32rpx 0;
  /* Tailwind: flex justify-center pt-[14px] px-4 */
}

.tips-text {
  font-size: 24rpx;
  color: #9ca3af;
  /* Tailwind: text-xs text-gray-400 */
}

/* ========== 保存按钮 ========== */
.btn-section {
  padding: 56rpx 64rpx 0;
  /* Tailwind: pt-7 px-8 */
}

.save-btn {
  width: 100%;
  height: 92rpx;
  line-height: 92rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
  border: none;
  border-radius: 46rpx;
  letter-spacing: 4rpx;
  /* Tailwind: w-full h-[46px] leading-[46px] text-center text-base font-semibold text-white bg-indigo-500 rounded-full shadow-lg tracking-wider */
}

.save-btn::after {
  border: none;
}

.save-btn:active {
  opacity: 0.85;
  transform: scale(0.98);
}
</style>
