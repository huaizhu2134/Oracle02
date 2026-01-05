<template>
  <div class="management-container">
    <el-tabs v-model="activeTab" class="tabs">
      <!-- 财务统计标签页 -->
      <el-tab-pane label="财务统计" name="statistics">
        <div class="tab-content">
          <!-- 统计卡片 -->
          <el-row :gutter="20" class="statistics-row">
            <el-col :span="6">
              <el-card class="statistics-card">
                <div class="statistics-item">
                  <div class="statistics-label">今日订单</div>
                  <div class="statistics-value">{{ statistics.todayOrders }}</div>
                  <div class="statistics-desc">笔</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="statistics-card">
                <div class="statistics-item">
                  <div class="statistics-label">今日收入</div>
                  <div class="statistics-value">¥{{ statistics.todayIncome }}</div>
                  <div class="statistics-desc">元</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="statistics-card">
                <div class="statistics-item">
                  <div class="statistics-label">平台收入</div>
                  <div class="statistics-value">¥{{ statistics.platformIncome }}</div>
                  <div class="statistics-desc">元</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="statistics-card">
                <div class="statistics-item">
                  <div class="statistics-label">陪玩收入</div>
                  <div class="statistics-value">¥{{ statistics.staffIncome }}</div>
                  <div class="statistics-desc">元</div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top: var(--spacing-md);">
            <el-col :span="12">
              <el-card class="chart-card">
                <template #header>
                  <span>收入趋势</span>
                </template>
                <div class="chart-container" id="incomeChart" style="height: 300px;"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="chart-card">
                <template #header>
                  <span>订单状态分布</span>
                </template>
                <div class="chart-container" id="orderStatusChart" style="height: 300px;"></div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>

      <!-- 提现管理标签页 -->
      <el-tab-pane label="提现管理" name="withdraw">
        <div class="tab-content">
          <!-- 提现搜索栏 -->
          <el-card class="search-bar">
            <el-form :inline="true" :model="withdrawSearchForm" class="demo-form-inline">
              <el-form-item label="陪玩ID">
                <el-input v-model="withdrawSearchForm.staffId" placeholder="请输入陪玩ID" />
              </el-form-item>
              <el-form-item label="提现状态">
                <el-select v-model="withdrawSearchForm.status" placeholder="请选择">
                  <el-option label="全部" value="" />
                  <el-option label="待审核" value="待审核" />
                  <el-option label="处理中" value="处理中" />
                  <el-option label="已通过" value="已通过" />
                  <el-option label="已拒绝" value="已拒绝" />
                </el-select>
              </el-form-item>
              <el-form-item label="申请时间">
                <el-date-picker
                  v-model="withdrawSearchForm.applyTimeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleWithdrawSearch">查询</el-button>
                <el-button @click="handleWithdrawReset">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 提现操作栏 -->
          <el-card class="operation-bar">
            <div class="operation-header">
              <div class="operation-left">
                <el-button type="primary" @click="handleBatchApprove">批量审核</el-button>
                <el-button type="danger" @click="handleBatchReject">批量拒绝</el-button>
              </div>
            </div>
          </el-card>

          <!-- 提现数据表格 -->
          <div class="table-container">
            <el-table
              v-loading="withdrawLoading"
              :data="withdrawList"
              border
              style="width: 100%"
              @selection-change="handleWithdrawSelectionChange"
              height="calc(100vh - 320px)"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column type="index" width="50" />
              <el-table-column prop="withdrawId" label="提现ID" width="100" />
              <el-table-column prop="staffId" label="陪玩ID" width="100" />
              <el-table-column prop="staffName" label="陪玩昵称" width="120" />
              <el-table-column prop="amount" label="提现金额" width="100">
                <template #default="scope">
                  <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.amount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <StatusTag :status="scope.row.status" />
                </template>
              </el-table-column>
              <el-table-column prop="applyTime" label="申请时间" width="160" />
              <el-table-column prop="approveTime" label="审核时间" width="160" />
              <el-table-column prop="approver" label="审核人" width="100" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button 
                    v-if="scope.row.status === '待审核'" 
                    size="small" 
                    type="success" 
                    @click="handleApproveWithdraw(scope.row)"
                  >
                    通过
                  </el-button>
                  <el-button 
                    v-if="scope.row.status === '待审核'" 
                    size="small" 
                    type="danger" 
                    @click="handleRejectWithdraw(scope.row)"
                  >
                    拒绝
                  </el-button>
                  <el-button size="small" @click="handleViewWithdraw(scope.row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 提现分页 -->
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="withdrawPage.current"
                v-model:page-size="withdrawPage.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="withdrawPage.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleWithdrawSizeChange"
                @current-change="handleWithdrawCurrentChange"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 收入明细标签页 -->
      <el-tab-pane label="收入明细" name="income">
        <div class="tab-content">
          <!-- 收入搜索栏 -->
          <el-card class="search-bar">
            <el-form :inline="true" :model="incomeSearchForm" class="demo-form-inline">
              <el-form-item label="陪玩ID">
                <el-input v-model="incomeSearchForm.staffId" placeholder="请输入陪玩ID" />
              </el-form-item>
              <el-form-item label="订单号">
                <el-input v-model="incomeSearchForm.orderNo" placeholder="请输入订单号" />
              </el-form-item>
              <el-form-item label="收入类型">
                <el-select v-model="incomeSearchForm.incomeType" placeholder="请选择">
                  <el-option label="全部" value="" />
                  <el-option label="订单收入" value="订单收入" />
                  <el-option label="提现" value="提现" />
                </el-select>
              </el-form-item>
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="incomeSearchForm.timeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleIncomeSearch">查询</el-button>
                <el-button @click="handleIncomeReset">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 收入数据表格 -->
          <div class="table-container">
            <el-table
              v-loading="incomeLoading"
              :data="incomeList"
              border
              style="width: 100%"
              height="calc(100vh - 280px)"
            >
              <el-table-column type="index" width="50" />
              <el-table-column prop="incomeId" label="收入ID" width="100" />
              <el-table-column prop="staffId" label="陪玩ID" width="100" />
              <el-table-column prop="staffName" label="陪玩昵称" width="120" />
              <el-table-column prop="orderId" label="订单号" width="150" />
              <el-table-column prop="incomeType" label="收入类型" width="100" />
              <el-table-column prop="amount" label="金额" width="100">
                <template #default="scope">
                  <span :style="{ color: scope.row.amount >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
                    {{ scope.row.amount >= 0 ? '+' : '' }}{{ scope.row.amount }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="balanceAfter" label="余额" width="100">
                <template #default="scope">
                  <span style="color: #409eff; font-weight: bold;">¥{{ scope.row.balanceAfter }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="时间" width="160" />
              <el-table-column prop="remark" label="备注" min-width="150" />
            </el-table>

            <!-- 收入分页 -->
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="incomePage.current"
                v-model:page-size="incomePage.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="incomePage.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleIncomeSizeChange"
                @current-change="handleIncomeCurrentChange"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 提现详情对话框 -->
    <el-dialog title="提现详情" v-model="withdrawDetailVisible" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="提现ID">{{ currentWithdraw.withdrawId }}</el-descriptions-item>
        <el-descriptions-item label="陪玩ID">{{ currentWithdraw.staffId }}</el-descriptions-item>
        <el-descriptions-item label="陪玩昵称">{{ currentWithdraw.staffName }}</el-descriptions-item>
        <el-descriptions-item label="提现金额">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ currentWithdraw.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :status="currentWithdraw.status" />
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentWithdraw.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentWithdraw.approveTime || '未审核' }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ currentWithdraw.approver || '未审核' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentWithdraw.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="withdrawDetailVisible = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getFinanceSummary, getWithdrawList, getIncomeList, applyWithdraw, approveWithdraw } from '@/api/finance'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStatusType } from '@/composables/useTable'
import StatusTag from '@/components/StatusTag.vue'

// 响应式数据
const activeTab = ref('statistics')
const withdrawLoading = ref(false)
const incomeLoading = ref(false)
const withdrawList = ref([])
const incomeList = ref([])
const withdrawDetailVisible = ref(false)
const currentWithdraw = ref({})
const multipleWithdrawSelection = ref([])

// 统计数据
const statistics = reactive({
  todayOrders: 0,
  todayIncome: 0,
  platformIncome: 0,
  staffIncome: 0
})

// 提现搜索表单
const withdrawSearchForm = reactive({
  staffId: '',
  status: '',
  applyTimeRange: []
})

// 收入搜索表单
const incomeSearchForm = reactive({
  staffId: '',
  orderNo: '',
  incomeType: '',
  timeRange: []
})

// 提现分页信息
const withdrawPage = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 收入分页信息
const incomePage = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取财务统计数据
const fetchStatistics = async () => {
  try {
    const response = await request({ url: '/api/test/statistics', method: 'get' })
    if (response.code === 200) {
      statistics.todayOrders = response.data.todayOrders || 0
      statistics.todayIncome = response.data.todayIncome || 0
      statistics.platformIncome = response.data.platformIncome || 0
      statistics.staffIncome = response.data.staffIncome || 0
    }
  } catch (error) {
    console.error('获取财务统计失败:', error)
  }
}

// 获取提现列表
const fetchWithdrawData = async () => {
  withdrawLoading.value = true
  try {
    const params = {
      ...withdrawSearchForm,
      page: withdrawPage.current,
      size: withdrawPage.size
    }
    const response = await getWithdrawList(params)
    if (response.code === 200) {
      withdrawList.value = response.data.records || []
      withdrawPage.total = response.data.total || 0
    } else {
      console.error('获取提现列表失败:', response.message)
    }
  } catch (error) {
    console.error('获取提现列表失败:', error)
  } finally {
    withdrawLoading.value = false
  }
}

// 获取收入明细
const fetchIncomeData = async () => {
  incomeLoading.value = true
  try {
    const params = {
      ...incomeSearchForm,
      page: incomePage.current,
      size: incomePage.size
    }
    const response = await getIncomeList(params)
    if (response.code === 200) {
      incomeList.value = response.data.records || []
      incomePage.total = response.data.total || 0
    } else {
      console.error('获取收入明细失败:', response.message)
    }
  } catch (error) {
    console.error('获取收入明细失败:', error)
  } finally {
    incomeLoading.value = false
  }
}

// 提现搜索
const handleWithdrawSearch = () => {
  withdrawPage.current = 1
  fetchWithdrawData()
}

// 提现重置
const handleWithdrawReset = () => {
  withdrawSearchForm.staffId = ''
  withdrawSearchForm.status = ''
  withdrawSearchForm.applyTimeRange = []
  handleWithdrawSearch()
}

// 收入搜索
const handleIncomeSearch = () => {
  incomePage.current = 1
  fetchIncomeData()
}

// 收入重置
const handleIncomeReset = () => {
  incomeSearchForm.staffId = ''
  incomeSearchForm.orderNo = ''
  incomeSearchForm.incomeType = ''
  incomeSearchForm.timeRange = []
  handleIncomeSearch()
}

// 提现分页大小变化
const handleWithdrawSizeChange = (val) => {
  withdrawPage.size = val
  fetchWithdrawData()
}

// 提现当前页变化
const handleWithdrawCurrentChange = (val) => {
  withdrawPage.current = val
  fetchWithdrawData()
}

// 收入分页大小变化
const handleIncomeSizeChange = (val) => {
  incomePage.size = val
  fetchIncomeData()
}

// 收入当前页变化
const handleIncomeCurrentChange = (val) => {
  incomePage.current = val
  fetchIncomeData()
}

// 提现选择变化
const handleWithdrawSelectionChange = (val) => {
  multipleWithdrawSelection.value = val
}

// 批量审核
const handleBatchApprove = async () => {
  if (multipleWithdrawSelection.value.length === 0) {
    ElMessage.warning('请选择要审核的提现记录')
    return
  }

  try {
    await ElMessageBox.confirm(`确认批量审核通过${multipleWithdrawSelection.value.length}条提现记录吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 批量审核通过
    for (const item of multipleWithdrawSelection.value) {
      await approveWithdraw({ staffId: item.staffId, status: '已通过' })
    }
    ElMessage.success('批量审核成功')
    fetchWithdrawData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error('批量审核失败')
    }
  }
}

// 批量拒绝
const handleBatchReject = async () => {
  if (multipleWithdrawSelection.value.length === 0) {
    ElMessage.warning('请选择要拒绝的提现记录')
    return
  }

  try {
    await ElMessageBox.confirm(`确认批量拒绝${multipleWithdrawSelection.value.length}条提现记录吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 批量拒绝
    for (const item of multipleWithdrawSelection.value) {
      await approveWithdraw({ staffId: item.staffId, status: '已拒绝' })
    }
    ElMessage.success('批量拒绝成功')
    fetchWithdrawData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败:', error)
      ElMessage.error('批量拒绝失败')
    }
  }
}

// 审核提现
const handleApproveWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm(`确认通过陪玩“${row.staffName}”的提现申请吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await approveWithdraw({ staffId: row.staffId, status: '已通过' })
    if (response.code === 200) {
      ElMessage.success('审核通过')
      fetchWithdrawData()
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  }
}

// 拒绝提现
const handleRejectWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm(`确认拒绝陪玩“${row.staffName}”的提现申请吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await approveWithdraw({ staffId: row.staffId, status: '已拒绝' })
    if (response.code === 200) {
      ElMessage.success('已拒绝')
      fetchWithdrawData()
    } else {
      ElMessage.error(response.message || '拒绝失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error)
      ElMessage.error('拒绝失败')
    }
  }
}

// 查看提现详情
const handleViewWithdraw = (row) => {
  currentWithdraw.value = row
  withdrawDetailVisible.value = true
}

onMounted(() => {
  fetchStatistics()
  fetchWithdrawData()
  fetchIncomeData()
})
</script>

<style scoped>
.management-container {
  padding: 12px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.tabs {
  background: #fff;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.tab-content {
  padding: 12px 0;
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

.operation-left .el-button {
  margin-right: 8px;
}

.statistics-row {
  margin-bottom: 20px;
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
  text-align: center;
  padding: 10px 0;
}

.statistics-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.statistics-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.statistics-desc {
  font-size: 12px;
  color: #909399;
}

.chart-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.table-container {
  height: calc(100vh - 380px);
  background: #fff;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.data-table :deep(.el-table__header-wrapper) {
  border-radius: 6px 6px 0 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 12px 0;
}
</style>

