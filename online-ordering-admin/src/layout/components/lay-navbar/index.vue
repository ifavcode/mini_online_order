<script setup lang="ts">
import { useNav } from "@/layout/hooks/useNav";
import LaySearch from "../lay-search/index.vue";
import LayNotice from "../lay-notice/index.vue";
import LayNavMix from "../lay-sidebar/NavMix.vue";
import LaySidebarFullScreen from "../lay-sidebar/components/SidebarFullScreen.vue";
import LaySidebarBreadCrumb from "../lay-sidebar/components/SidebarBreadCrumb.vue";
import LaySidebarTopCollapse from "../lay-sidebar/components/SidebarTopCollapse.vue";

import LogoutCircleRLine from "~icons/ri/logout-circle-r-line";
import Setting from "~icons/ri/settings-3-line";
import { useSystemStore } from "@/store/modules/system";

const {
  layout,
  device,
  logout,
  onPanel,
  pureApp,
  username,
  userAvatar,
  avatarsStyle,
  toggleSideBar
} = useNav();

const systemStore = useSystemStore();

function getCode() {
  window.open('https://www.yuque.com/u25796691/kb/ra0vc15ixk0tvqca')
}
</script>

<template>
  <div class="navbar bg-[#fff] shadow-xs shadow-[rgba(0,21,41,0.08)]">
    <LaySidebarTopCollapse v-if="device === 'mobile'" class="hamburger-container" :is-active="pureApp.sidebar.opened"
      @toggleClick="toggleSideBar" />

    <LaySidebarBreadCrumb v-if="layout !== 'mix' && device !== 'mobile'" class="breadcrumb-container" />

    <LayNavMix v-if="layout === 'mix'" />

    <div v-if="layout === 'vertical'" class="vertical-header-right">
      <div v-if="systemStore.demoMode" class="flex items-center gap-2">
        <el-button type="primary" class="get-code-btn" @click="getCode">获取源码</el-button>
        <p class="text-sm text-green-500">演示模式</p>
      </div>
      <!-- 菜单搜索 -->
      <LaySearch id="header-search" />
      <!-- 全屏 -->
      <LaySidebarFullScreen id="full-screen" />
      <!-- 消息通知 -->
      <!-- <LayNotice id="header-notice" /> -->
      <!-- 退出登录 -->
      <el-dropdown trigger="click">
        <span class="el-dropdown-link navbar-bg-hover select-none">
          <img :src="userAvatar" :style="avatarsStyle" />
          <p v-if="username" class="dark:text-white">{{ username }}</p>
        </span>
        <template #dropdown>
          <el-dropdown-menu class="logout">
            <el-dropdown-item @click="logout">
              <IconifyIconOffline :icon="LogoutCircleRLine" style="margin: 5px" />
              退出系统
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <span class="set-icon navbar-bg-hover" title="打开系统配置" @click="onPanel">
        <IconifyIconOffline :icon="Setting" />
      </span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.navbar {
  width: 100%;
  height: 48px;
  overflow: hidden;

  .hamburger-container {
    float: left;
    height: 100%;
    line-height: 48px;
    cursor: pointer;
  }

  .vertical-header-right {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    min-width: 280px;
    height: 48px;
    color: #000000d9;

    .el-dropdown-link {
      display: flex;
      align-items: center;
      justify-content: space-around;
      height: 48px;
      padding: 10px;
      color: #000000d9;
      cursor: pointer;

      p {
        font-size: 14px;
      }

      img {
        width: 22px;
        height: 22px;
        border-radius: 50%;
      }
    }

    .get-code-btn {
      position: relative;
      overflow: hidden;
      font-weight: 600;
      letter-spacing: 1px;
      box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.45);
      animation: get-code-pulse 1.6s infinite;
      transition: transform 0.18s ease, box-shadow 0.18s ease;

      &::after {
        content: "";
        position: absolute;
        inset: 0;
        background: radial-gradient(circle at center, rgba(255, 255, 255, 0.55), transparent 60%);
        opacity: 0;
        transform: scale(0.9);
        transition: opacity 0.18s ease, transform 0.18s ease;
        pointer-events: none;
      }

      &:hover {
        transform: translateY(-1px) scale(1.04);
        box-shadow: 0 12px 24px -8px rgba(64, 158, 255, 0.75);
        animation-play-state: paused;

        &::after {
          opacity: 1;
          transform: scale(1);
        }
      }

      &:active {
        transform: translateY(0) scale(0.98);
        box-shadow: 0 4px 10px -4px rgba(64, 158, 255, 0.7);
      }
    }
  }

  .breadcrumb-container {
    float: left;
    margin-left: 16px;
  }
}

.logout {
  width: 120px;

  ::v-deep(.el-dropdown-menu__item) {
    display: inline-flex;
    flex-wrap: wrap;
    min-width: 100%;
  }
}

@keyframes get-code-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.55);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(64, 158, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0);
  }
}
</style>
