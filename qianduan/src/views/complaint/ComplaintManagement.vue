<template>
  <div class="management-container">
    <!-- 投诉搜索栏 -->
    <el-card class="search-bar">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="投诉类型">
          <el-select v-model="searchForm.complaintType" placeholder="请选择">
            <el-option label="全部" value="" />
            <el-option label="服务态度" value="服务态度" />
            <el-option label="服务质量" value="服务质量" />
            <el-option label="收费问题" value="收费问题" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="投诉状态">
          <el-select v-model="searchForm.complaintStatus" placeholder="请选择">
            <el-option label="全部" value="" />
            <el-option label="待处理" value="待处理" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已解决" value="已解决" />
            <el-option label="已关闭" value="已关闭" />
          </el-select>
        </el-form-item>
        <el-form-item label="投诉时间">
          <el-date-picker
            v-model="searchForm.createTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 投诉数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="complaintList"
        border
        style="width: 100%"
        height="calc(100vh - 280px)"
      >
        <el-table-column type="index" width="50" />
        <el-table-column prop="complaintId" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="customerName" label="客户名称" width="120" />
        <el-table-column prop="staffName" label="陪玩昵称" width="120" />
        <el-table-column prop="complaintType" label="投诉类型" width="100" />
        <el-table-column prop="complaintContent" label="投诉内容" min-width="200" />
        <el-table-column prop="complaintStatus" label="投诉状态" width="100">
          <template #default="scope">
            <StatusTag :status="scope.row.complaintStatus" />
          </template>
        </el-table-column>
        <el-table-column prop="handlerName" label="处理人" width="100" />
        <el-table-column prop="handleTime" label="处理时间" width="160" />
        <el-table-column prop="createTime" label="投诉时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleViewComplaint(scope.row)">查看详情</el-button>
            <el-button 
              v-if="scope.row.complaintStatus !== '已解决' && scope.row.complaintStatus !== '已关闭'" 
              size="small" 
              type="primary" 
              @click="handleHandleComplaint(scope.row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="page.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 投诉处理对话框 -->
    <el-dialog title="处理投诉" v-model="handleDialogVisible" width="600px">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="投诉ID">
          <span>{{ currentComplaint.complaintId }}</span>
        </el-form-item>
        <el-form-item label="订单号">
          <span>{{ currentComplaint.orderNo }}</span>
        </el-form-item>
        <el-form-item label="客户名称">
          <span>{{ currentComplaint.customerName }}</span>
        </el-form-item>
        <el-form-item label="陪玩昵称">
          <span>{{ currentComplaint.staffName }}</span>
        </el-form-item>
        <el-form-item label="投诉类型">
          <span>{{ currentComplaint.complaintType }}</span>
        </el-form-item>
        <el-form-item label="投诉内容">
          <div class="complaint-content">{{ currentComplaint.complaintContent }}</div>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input 
            v-model="handleForm.result" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入处理结果"
          />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="handleForm.status" placeholder="请选择处理状态">
            <el-option label="待处理" value="待处理" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已解决" value="已解决" />
            <el-option label="已关闭" value="已关闭" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleProcessSubmit">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 投诉详情对话框 -->
    <el-dialog title="投诉详情" v-model="detailDialogVisible" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="投诉ID">{{ currentComplaint.complaintId }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentComplaint.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ currentComplaint.customerName }}</el-descriptions-item>
        <el-descriptions-item label="陪玩昵称">{{ currentComplaint.staffName }}</el-descriptions-item>
        <el-descriptions-item label="投诉类型">
          <el-tag>{{ currentComplaint.complaintType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="投诉内容">{{ currentComplaint.complaintContent }}</el-descriptions-item>
        <el-descriptions-item label="投诉状态">
          <StatusTag :status="currentComplaint.complaintStatus" />
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentComplaint.handlerName || '未处理' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ currentComplaint.handleTime || '未处理' }}</el-descriptions-item>
        <el-descriptions-item label="投诉时间">{{ currentComplaint.createTime }}</el-descriptions-item>
        <el-descriptions-item label="处理结果" v-if="currentComplaint.handlerComment">
          {{ currentComplaint.handlerComment }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getComplaintList, handleComplaint } from '@/api/complaint'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStatusType } from '@/composables/useTable'
import StatusTag from '@/components/StatusTag.vue'

// 响应式数据
const loading = ref(false)
const complaintList = ref([])
const handleDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentComplaint = ref({})

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  complaintType: '',
  complaintStatus: '',
  createTimeRange: []
})

// 分页信息
const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 处理表单
const handleForm = reactive({
  result: '',
  status: '已解决'
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
    
    const response = await getComplaintList(params)
    if (response.code === 200) {
      complaintList.value = response.data.records || []
      page.total = response.data.total || 0
    } else {
      console.error('获取投诉列表失败:', response.message)
    }
  } catch (error) {
    console.error('获取投诉列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  page.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.complaintType = ''
  searchForm.complaintStatus = ''
  searchForm.createTimeRange = []
  handleSearch()
}

// 查看投诉详情
const handleViewComplaint = (row) => {
  currentComplaint.value = row
  detailDialogVisible.value = true
}

// 处理投诉
const handleHandleComplaint = (row) => {
  currentComplaint.value = row
  handleForm.result = ''
  handleForm.status = '已解决'
  handleDialogVisible.value = true
}

// 提交投诉处理
const handleProcessSubmit = async () => {
  try {
    const data = {
      complaintId: currentComplaint.value.complaintId,
      handlerComment: handleForm.result,
      complaintStatus: handleForm.status
    }
    const response = await handleComplaint(data)
    if (response.code === 200) {
      ElMessage.success('处理成功')
      handleDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error('处理失败')
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
})
</script>

<style scoped>
.management-container {
  padding: 12px;
  background: #f5f7fa;
}

.search-bar {
  margin-bottom: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-form :deep(.el-form-item) {
  margin-bottom: 8px;
}

.table-container {
  height: calc(100vh - 220px);
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

.complaint-content {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.5;
  color: #606266;
}
</style>