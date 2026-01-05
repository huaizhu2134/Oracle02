<template>
  <div class="management-container">
    <!-- 评价搜索栏 -->
    <el-card class="search-bar">
      <el-form :inline="true" :model="evaluationSearchForm" class="demo-form-inline">
        <el-form-item label="订单号">
          <el-input v-model="evaluationSearchForm.orderNo" placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="陪玩昵称">
          <el-input v-model="evaluationSearchForm.staffName" placeholder="请输入陪玩昵称" />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="evaluationSearchForm.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="evaluationSearchForm.score" placeholder="请选择">
            <el-option label="全部" value="" />
            <el-option label="1星" value="1" />
            <el-option label="2星" value="2" />
            <el-option label="3星" value="3" />
            <el-option label="4星" value="4" />
            <el-option label="5星" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价时间">
          <el-date-picker
            v-model="evaluationSearchForm.createTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleEvaluationSearch">查询</el-button>
          <el-button @click="handleEvaluationReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评价数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="evaluationLoading"
        :data="evaluationList"
        border
        style="width: 100%"
        height="calc(100vh - 320px)"
      >
        <el-table-column type="index" width="50" />
        <el-table-column prop="evalId" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="staffName" label="陪玩昵称" width="120" />
        <el-table-column prop="customerName" label="客户名称" width="120" />
        <el-table-column prop="score" label="评分" width="100">
          <template #default="scope">
            <el-rate
              v-model="scope.row.score"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}星"
            />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" />
        <el-table-column prop="tags" label="标签" width="150" />
        <el-table-column prop="isAnonymous" label="是否匿名" width="80">
          <template #default="scope">
            {{ scope.row.isAnonymous === 'Y' ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleReplyEvaluation(scope.row)" type="primary">
              回复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 评价分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="evaluationPage.current"
          v-model:page-size="evaluationPage.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="evaluationPage.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleEvaluationSizeChange"
          @current-change="handleEvaluationCurrentChange"
        />
      </div>
    </div>

    <!-- 评价回复对话框 -->
    <el-dialog title="回复评价" v-model="replyDialogVisible" width="600px">
      <el-form>
        <el-form-item label="评价内容">
          <div class="evaluation-content">{{ currentEvaluation.content }}</div>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input 
            v-model="replyContent" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleReplySubmit">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getEvaluationList } from '@/api/evaluation'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const evaluationLoading = ref(false)
const evaluationList = ref([])
const replyDialogVisible = ref(false)
const currentEvaluation = ref({})
const replyContent = ref('')

// 评价搜索表单
const evaluationSearchForm = reactive({
  orderNo: '',
  staffName: '',
  customerName: '',
  score: '',
  createTimeRange: []
})

// 评价分页信息
const evaluationPage = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取评价数据
const fetchEvaluationData = async () => {
  evaluationLoading.value = true
  try {
    const params = {
      ...evaluationSearchForm,
      page: evaluationPage.current,
      size: evaluationPage.size
    }
    if (evaluationSearchForm.createTimeRange && evaluationSearchForm.createTimeRange.length === 2) {
      params.startTime = evaluationSearchForm.createTimeRange[0]
      params.endTime = evaluationSearchForm.createTimeRange[1]
    }
    
    const response = await getEvaluationList(params)
    if (response.code === 200) {
      evaluationList.value = response.data.records || []
      evaluationPage.total = response.data.total || 0
    } else {
      console.error('获取评价列表失败:', response.message)
    }
  } catch (error) {
    console.error('获取评价列表失败:', error)
  } finally {
    evaluationLoading.value = false
  }
}

// 评价搜索
const handleEvaluationSearch = () => {
  evaluationPage.current = 1
  fetchEvaluationData()
}

// 评价重置
const handleEvaluationReset = () => {
  evaluationSearchForm.orderNo = ''
  evaluationSearchForm.staffName = ''
  evaluationSearchForm.customerName = ''
  evaluationSearchForm.score = ''
  evaluationSearchForm.createTimeRange = []
  handleEvaluationSearch()
}

// 评价分页大小变化
const handleEvaluationSizeChange = (val) => {
  evaluationPage.size = val
  fetchEvaluationData()
}

// 评价当前页变化
const handleEvaluationCurrentChange = (val) => {
  evaluationPage.current = val
  fetchEvaluationData()
}

// 回复评价
const handleReplyEvaluation = (row) => {
  currentEvaluation.value = row
  replyContent.value = ''
  replyDialogVisible.value = true
}

// 提交评价回复
const handleReplySubmit = async () => {
  try {
    const response = await request({
      url: '/api/evaluation/reply',
      method: 'post',
      data: {
        evalId: currentEvaluation.value.evalId,
        staffReply: replyContent.value
      }
    })
    if (response.code === 200) {
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      fetchEvaluationData() // 刷新数据
    } else {
      ElMessage.error(response.message || '回复失败')
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  }
}

onMounted(() => {
  fetchEvaluationData()
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

.evaluation-content {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.5;
  color: #606266;
}
</style>