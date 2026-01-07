<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <header class="admin-header">
      <div class="header-content">
        <div class="header-left">
          <div class="logo">
            <el-icon><User /></el-icon>
            <h1>游戏陪玩管理系统</h1>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand" placement="bottom-end">
            <div class="user-info">
              <el-avatar size="small" :icon="UserFilled" class="user-avatar"></el-avatar>
              <span class="user-name">管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <div class="admin-container">
      <!-- 侧边栏 -->
      <aside :class="isCollapse ? 'admin-aside collapsed' : 'admin-aside'">
        <div class="sidebar-content">
          <el-menu :default-active="$route.path" class="sidebar-menu" :router="true" :collapse="isCollapse" :collapse-transition="false">
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <template #title>首页</template>
            </el-menu-item>
            <el-menu-item index="/staff">
              <el-icon><User /></el-icon>
              <template #title>陪玩管理</template>
            </el-menu-item>
            <el-menu-item index="/customer">
              <el-icon><UserFilled /></el-icon>
              <template #title>客户管理</template>
            </el-menu-item>
            <el-menu-item index="/evaluation">
              <el-icon><ChatLineRound /></el-icon>
              <template #title>评价管理</template>
            </el-menu-item>
            <el-menu-item index="/complaint">
              <el-icon><ChatLineRound /></el-icon>
              <template #title>投诉管理</template>
            </el-menu-item>
            <el-menu-item index="/order">
            <el-icon><Document /></el-icon>
            <template #title>订单管理</template>
          </el-menu-item>
            <el-menu-item index="/finance">
              <el-icon><Money /></el-icon>
              <template #title>财务管理</template>
            </el-menu-item>
          </el-menu>
        </div>
        <div class="sidebar-toggle" @click="toggleSidebar">
          <el-icon v-if="isCollapse"><Expand /></el-icon>
          <el-icon v-else><Fold /></el-icon>
        </div>
      </aside>

      <!-- 主内容区域 -->
      <main class="admin-main">
        <div class="admin-content">
          <router-view v-slot="{ Component }">
            <keep-alive include="keepAlive">
              <component :is="Component" :key="$route.fullPath" />
            </keep-alive>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import {
  House,
  User,
  UserFilled,
  Document,
  ChatLineRound,
  Money,
  ArrowDown,
  SwitchButton,
  Expand,
  Fold
} from '@element-plus/icons-vue'

import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const isCollapse = ref(false)

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
      // 清除登录信息
      localStorage.removeItem('token')
      ElMessage.success('已退出登录')
      // 跳转到登录页
      router.push('/login')
    } catch {
      // 取消操作
    }
  } else if (command === 'profile') {
    // 跳转到个人资料页
    ElMessage.info('个人资料功能开发中...')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Helvetica Neue', Arial, sans-serif;
  background-attachment: fixed;
}

.admin-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.admin-header {
  height: var(--header-height);
  background: linear-gradient(135deg, var(--primary-color), #3498db);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  flex-shrink: 0;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.header-left .logo {
  display: flex;
  align-items: center;
  color: white;
  animation: slideInLeft 0.6s ease-out;
}

.header-left .logo .el-icon {
  font-size: 28px;
  margin-right: 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 6px;
  transition: all 0.3s ease;
}

.header-left .logo:hover .el-icon {
  transform: scale(1.1);
  background: rgba(255, 255, 255, 0.3);
}

.header-left .logo h1 {
  margin: 0;
  font-size: 20px;
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s;
  color: white;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-avatar {
  margin-right: 8px;
}

.user-name {
  margin-right: 6px;
  font-weight: var(--font-weight-medium);
}

.admin-aside {
  width: var(--sidebar-width);
  background: linear-gradient(to bottom, #1e3c72, #2a5298);
  transition: all 0.3s;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: 3px 0 16px rgba(0, 0, 0, 0.15);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.admin-aside.collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: var(--spacing-sm) 0;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
  overflow-y: auto;
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item) {
  margin: 4px 10px;
  border-radius: 10px;
  height: 46px;
  line-height: 46px;
  color: rgba(255, 255, 255, 0.7);
  margin: 6px 10px;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.05);
  border-left: 3px solid transparent;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  border-left: 3px solid var(--primary-color);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateX(4px);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border-left: 3px solid rgba(255, 255, 255, 0.3);
  transform: translateX(4px);
}

.sidebar-menu :deep(.el-menu-item i) {
  transition: all 0.3s;
}

.sidebar-menu :deep(.el-menu-item:hover) i {
  transform: scale(1.2);
  color: var(--primary-color-light);
}

.sidebar-toggle {
  height: 48px;
  line-height: 48px;
  text-align: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 8px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  transition: all 0.3s;
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  transform: scale(1.05);
}

.admin-main {
  flex: 1;
  overflow: auto;
  padding: var(--spacing-md);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
}

.admin-content {
  background: rgba(255, 255, 255, 0.85);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  min-height: calc(100% - 40px);
  height: 100%;
  transition: all 0.4s ease;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 滚动条样式 */
:deep(.el-main::-webkit-scrollbar) {
  width: 8px;
}

:deep(.el-main::-webkit-scrollbar-track) {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

:deep(.el-main::-webkit-scrollbar-thumb) {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  transition: all 0.3s;
}

:deep(.el-main::-webkit-scrollbar-thumb:hover) {
  background: rgba(0, 0, 0, 0.3);
}

:deep(.el-aside::-webkit-scrollbar) {
  width: 6px;
}

:deep(.el-aside::-webkit-scrollbar-track) {
  background: rgba(255, 255, 255, 0.05);
}

:deep(.el-aside::-webkit-scrollbar-thumb) {
  background: rgba(255, 255, 255, 0.2);
}

:deep(.el-aside::-webkit-scrollbar-thumb:hover) {
  background: rgba(255, 255, 255, 0.3);
}

:deep(.el-menu) {
  background-color: transparent;
  border-right: none;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.7);
}

:deep(.el-menu-item:hover) {
  color: #fff;
}

/* 动画定义 */
@keyframes slideInLeft {
  from {
    transform: translateX(-20px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .admin-header {
    padding: 0 var(--spacing-sm);
  }
  
  .header-left .logo h1 {
    font-size: 18px;
  }
  
  .admin-aside {
    width: var(--sidebar-collapsed-width);
  }
}
</style>
