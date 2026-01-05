<template>
  <div class="management-container">
    <!-- 搜索栏 -->
    <el-card class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input 
            v-model="searchForm.orderNo" 
            placeholder="请输入订单号" 
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="客户">
          <el-input 
            v-model="searchForm.customerName" 
            placeholder="请输入客户名" 
            clearable
            style="width: 150px;"
          />
        </el-form-item>
        <el-form-item label="陪玩">
          <el-input 
            v-model="searchForm.staffName" 
            placeholder="请输入陪玩昵称" 
            clearable
            style="width: 150px;"
          />
        </el-form-item>
        <el-form-item label="游戏类型">
          <el-select v-model="searchForm.gameType" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="全部" value="" />
            <el-option label="英雄联盟" value="英雄联盟" />
            <el-option label="王者荣耀" value="王者荣耀" />
            <el-option label="和平精英" value="和平精英" />
            <el-option label="绝地求生" value="绝地求生" />
            <el-option label="原神" value="原神" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.orderStatus" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="全部" value="" />
            <el-option label="待支付" value="待支付" />
            <el-option label="已支付" value="已支付" />
            <el-option label="服务中" value="服务中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已取消" value="已取消" />
            <el-option label="退款中" value="退款中" />
            <el-option label="已退款" value="已退款" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.createTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="operation-bar">
      <div class="operation-header">
        <div class="operation-left">
          <el-button type="primary" @click="handleCreateOrder">
            <el-icon><Plus /></el-icon>
            创建订单
          </el-button>
          <el-button type="success">
            <el-icon><Download /></el-icon>
            导出
          </el-button>

        </div>
        <div class="operation-right">
          <el-button type="default" @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="statistics-row">
      <el-col :span="6">
        <el-card class="statistics-card" shadow="never">
          <div class="statistics-item">
            <div class="statistics-icon today-orders">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="statistics-content">
              <div class="statistics-value">{{ statistics.todayOrders }}</div>
              <div class="statistics-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card" shadow="never">
          <div class="statistics-item">
            <div class="statistics-icon today-income">
              <el-icon><Money /></el-icon>
            </div>
            <div class="statistics-content">
              <div class="statistics-value">¥{{ statistics.todayIncome }}</div>
              <div class="statistics-label">今日收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card" shadow="never">
          <div class="statistics-item">
            <div class="statistics-icon pending-orders">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="statistics-content">
              <div class="statistics-value">{{ statistics.pendingOrders }}</div>
              <div class="statistics-label">待处理订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistics-card" shadow="never">
          <div class="statistics-item">
            <div class="statistics-icon completed-orders">
              <el-icon><Finished /></el-icon>
            </div>
            <div class="statistics-content">
              <div class="statistics-value">{{ statistics.completedOrders }}</div>
              <div class="statistics-label">已完成订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="orderList"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#fafafa', color: '#606266' }"
        class="data-table"
        height="calc(100vh - 420px)"
      >
        <el-table-column type="index" width="50" label="#" />
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户名称" width="120" show-overflow-tooltip />
        <el-table-column prop="staffName" label="陪玩昵称" width="120" show-overflow-tooltip />
        <el-table-column prop="gameType" label="游戏类型" width="100" align="center">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.gameType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="serviceHours" label="服务时长" width="100" align="center">
          <template #default="scope">
            {{ scope.row.serviceHours }}小时
          </template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="单价" width="90" align="right">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.unitPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="100" align="right">
          <template #default="scope">
            <span style="color: #67c23a; font-weight: bold;">¥{{ scope.row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="100" align="center">
          <template #default="scope">
            <StatusTag :status="scope.row.orderStatus" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" show-overflow-tooltip />
        <el-table-column prop="payTime" label="支付时间" width="160" align="center" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                v-if="scope.row.orderStatus === '待支付'" 
                size="small" 
                type="success" 
                plain
                @click="handlePay(scope.row)"
              >
                <el-icon><CircleCheck /></el-icon>
                确认支付
              </el-button>
              <el-button 
                v-if="scope.row.orderStatus === '已支付'" 
                size="small" 
                type="primary" 
                plain
                @click="handleStart(scope.row)"
              >
                <el-icon><VideoPlay /></el-icon>
                开始服务
              </el-button>
              <el-button 
                v-if="scope.row.orderStatus === '服务中'" 
                size="small" 
                type="warning" 
                plain
                @click="handleComplete(scope.row)"
              >
                <el-icon><SuccessFilled /></el-icon>
                完成服务
              </el-button>
              <el-button 
                v-if="['待支付', '已支付'].includes(scope.row.orderStatus)" 
                size="small" 
                type="danger" 
                plain
                @click="handleCancel(scope.row)"
              >
                <el-icon><CircleClose /></el-icon>
                取消
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :page-sizes="[10, 20, 50, 100]"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="page.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建订单对话框 -->
    <el-dialog 
      title="创建订单" 
      v-model="createDialogVisible" 
      width="600px"
      :close-on-click-modal="false"
      :destroy-on-close="true"
    >
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="100px">
        <el-form-item label="选择客户" prop="customerId">
          <el-select v-model="orderForm.customerId" filterable placeholder="请选择客户" style="width: 100%;">
            <el-option
              v-for="customer in customerList"
              :key="customer.customerId"
              :label="customer.username"
              :value="customer.customerId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择陪玩" prop="staffId">
          <el-select v-model="orderForm.staffId" filterable placeholder="请选择陪玩" style="width: 100%;">
            <el-option
              v-for="staff in availableStaffList"
              :key="staff.staffId"
              :label="`${staff.staffName} - ${staff.skillLevel} - ¥${staff.unitPrice}/小时`"
              :value="staff.staffId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="游戏类型" prop="gameType">
          <el-select v-model="orderForm.gameType" placeholder="请选择游戏类型" style="width: 100%;">
            <el-option label="英雄联盟" value="英雄联盟" />
            <el-option label="王者荣耀" value="王者荣耀" />
            <el-option label="和平精英" value="和平精英" />
            <el-option label="绝地求生" value="绝地求生" />
            <el-option label="原神" value="原神" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务时长" prop="serviceHours">
          <el-input-number 
            v-model="orderForm.serviceHours" 
            :min="1" 
            :max="24" 
            :controls="false"
            style="width: 100%;"
          />
          <span style="margin-left: 10px; color: #909399;">小时</span>
        </el-form-item>
        <el-form-item label="单价">
          <el-input v-model="orderForm.unitPrice" readonly />
        </el-form-item>
        <el-form-item label="总金额">
          <el-input 
            :value="(orderForm.serviceHours * orderForm.unitPrice).toFixed(2)" 
            readonly 
            style="color: #f56c6c; font-weight: bold;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleCreateSubmit">创 建</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { getOrderList, addOrder, updateOrderStatus } from '@/api/order'
import { getCustomerList } from '@/api/customer'
import { getStaffList } from '@/api/staff'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStatusType, debounce } from '@/composables/useTable'
import StatusTag from '@/components/StatusTag.vue'
import {
  Search,
  Refresh,
  Plus,
  Download,
  Calendar,
  Money,
  Clock,
  Finished,
  CircleCheck,
  VideoPlay,
  SuccessFilled,
  CircleClose
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const orderList = ref([])
const customerList = ref([])
const availableStaffList = ref([])
const createDialogVisible = ref(false)

// 统计数据
const statistics = reactive({
  todayOrders: 0,
  todayIncome: 0,
  pendingOrders: 0,
  completedOrders: 0
})

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  customerName: '',
  staffName: '',
  gameType: '',
  orderStatus: '',
  createTimeRange: []
})

// 分页信息
const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 订单表单
const orderForm = reactive({
  customerId: '',
  staffId: '',
  gameType: '',
  serviceHours: 1,
  unitPrice: 50
})

// 表单验证规则
const orderRules = {
  customerId: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  staffId: [
    { required: true, message: '请选择陪玩', trigger: 'change' }
  ],
  gameType: [
    { required: true, message: '请选择游戏类型', trigger: 'change' }
  ],
  serviceHours: [
    { required: true, message: '请输入服务时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 24, message: '服务时长必须在1-24小时之间', trigger: 'blur' }
  ]
}

const orderFormRef = ref()

// 监听陪玩ID变化，自动更新单价
watch(() => orderForm.staffId, (newVal) => {
  if (newVal) {
    const staff = availableStaffList.value.find(s => s.staffId === newVal)
    if (staff) {
      orderForm.unitPrice = staff.unitPrice
    }
  }
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: page.current,
      size: page.size
    }
    if (searchForm.createTimeRange && searchForm.createTimeRange.length === 2) {
      params.startTime = searchForm.createTimeRange[0]
      params.endTime = searchForm.createTimeRange[1]
    }
    
    const response = await getOrderList(params)
    orderList.value = response.data.records || []
    page.total = response.data.total || 0
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 获取统计信息
const fetchStatistics = async () => {
  try {
    const response = await request({ url: '/api/test/statistics', method: 'get' })
    if (response.code === 200) {
      statistics.todayOrders = response.data.todayOrders || 0
      statistics.todayIncome = response.data.todayIncome || 0
      statistics.pendingOrders = response.data.pendingOrders || 0
      statistics.completedOrders = response.data.completedOrders || 0
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 搜索（带防抖）
const handleSearch = debounce(() => {
  page.current = 1
  fetchData()
}, 300)

// 重置
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.customerName = ''
  searchForm.staffName = ''
  searchForm.gameType = ''
  searchForm.orderStatus = ''
  searchForm.createTimeRange = []
  handleSearch()
}

// 刷新数据
const refreshData = () => {
  fetchData()
  fetchStatistics()
}

// 创建订单
const handleCreateOrder = async () => {
  // 加载客户列表
  try {
    const customerResponse = await getCustomerList({ page: 1, size: 1000 })
    customerList.value = customerResponse.data.records || []
    
    // 加载空闲陪玩列表
    const staffResponse = await getStaffList({ page: 1, size: 1000, status: '空闲' })
    availableStaffList.value = staffResponse.data.records || []
  } catch (error) {
    console.error('加载客户或陪玩列表失败:', error)
    ElMessage.error('加载数据失败')
    return
  }
  
  // 重置表单
  Object.assign(orderForm, {
    customerId: '',
    staffId: '',
    gameType: '',
    serviceHours: 1,
    unitPrice: 50
  })
  
  createDialogVisible.value = true
}

// 提交创建订单
const handleCreateSubmit = () => {
  orderFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 计算总金额
        const totalAmount = orderForm.serviceHours * orderForm.unitPrice
        
        const orderData = {
          ...orderForm,
          totalAmount: totalAmount,
          orderStatus: '待支付'
        }
        
        await addOrder(orderData)
        ElMessage.success('订单创建成功')
        createDialogVisible.value = false
        fetchData()
        fetchStatistics()
      } catch (error) {
        console.error('创建订单失败:', error)
        ElMessage.error('创建订单失败')
      }
    }
  })
}

// 确认支付
const handlePay = async (row) => {
  try {
    await ElMessageBox.confirm('确认该订单已支付?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateOrderStatus(row.orderId, '已支付')
    ElMessage.success('操作成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新订单状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 开始服务
const handleStart = async (row) => {
  try {
    await ElMessageBox.confirm('确认开始服务?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateOrderStatus(row.orderId, '服务中')
    ElMessage.success('操作成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新订单状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 完成服务
const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确认完成服务?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateOrderStatus(row.orderId, '已完成')
    ElMessage.success('操作成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新订单状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 取消订单
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确认取消该订单?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateOrderStatus(row.orderId, '已取消')
    ElMessage.success('取消成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消失败')
    }
  }
}

// 分页大小变化
const handleSizeChange = (val) => {
  page.size = val
  fetchData()
}

// 当前页变化
const handleCurrentChange = (val) => {
  page.current = val
  fetchData()
}

onMounted(() => {
  fetchData()
  fetchStatistics()
})
</script>

<style scoped>
.management-container {
  padding: 12px;
  background: #f5f7fa;
}

.search-bar,
.operation-bar {
  margin-bottom: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-form :deep(.el-form-item) {
  margin-bottom: 8px;
}

.operation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.operation-left .el-button,
.operation-right .el-button {
  margin-right: 8px;
}

.statistics-row {
  margin-bottom: 12px;
}

.statistics-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.statistics-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.statistics-item {
  display: flex;
  align-items: center;
  padding: 10px;
}

.statistics-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
}

.today-orders {
  background: #e6f7ff;
  color: #1890ff;
}

.today-income {
  background: #f6ffed;
  color: #52c41a;
}

.pending-orders {
  background: #fff7e6;
  color: #fa8c16;
}

.completed-orders {
  background: #f9f0ff;
  color: #722ed1;
}

.statistics-content {
  flex: 1;
}

.statistics-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
  margin-bottom: 4px;
}

.statistics-label {
  font-size: 13px;
  color: #909399;
}

.table-container {
  height: calc(100vh - 420px);
  background: #fff;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.data-table :deep(.el-table__header-wrapper) {
  border-radius: 6px 6px 0 0;
}

.action-buttons .el-button {
  margin: 0 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 12px 0;
}
</style>