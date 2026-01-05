<template>
  <div class="management-container">
    <!-- 搜索栏 -->
    <el-card class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input 
            v-model="searchForm.username" 
            placeholder="请输入用户名" 
            clearable
            style="width: 150px;"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input 
            v-model="searchForm.phone" 
            placeholder="请输入手机号" 
            clearable
            style="width: 150px;"
          />
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="searchForm.memberLevel" placeholder="请选择" clearable style="width: 130px;">
            <el-option label="全部" value="" />
            <el-option label="普通会员" value="普通会员" />
            <el-option label="VIP会员" value="VIP会员" />
            <el-option label="SVIP会员" value="SVIP会员" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 130px;">
            <el-option label="全部" value="" />
            <el-option label="正常" value="正常" />
            <el-option label="冻结" value="冻结" />
            <el-option label="注销" value="注销" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册时间">
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
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增客户
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

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="customerList"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#fafafa', color: '#606266' }"
        class="data-table"
        height="calc(100vh - 320px)"
      >
        <el-table-column type="index" width="50" label="#" />
        <el-table-column prop="customerId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" width="120" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="60" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.gender === 'M' ? 'primary' : 'danger'" size="small">
              {{ scope.row.gender === 'M' ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="60" align="center" />
        <el-table-column prop="memberLevel" label="会员等级" width="100" align="center">
          <template #default="scope">
            <StatusTag :status="scope.row.memberLevel" />
          </template>
        </el-table-column>
        <el-table-column prop="totalConsume" label="总消费" width="100" align="right">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.totalConsume }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="80" align="center" />
        <el-table-column prop="balance" label="余额" width="100" align="right">
          <template #default="scope">
            <span style="color: #67c23a; font-weight: bold;">¥{{ scope.row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <StatusTag :status="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" align="center" show-overflow-tooltip />
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" align="center" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" @click="handleEdit(scope.row)" type="primary" plain>
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" @click="handleRecharge(scope.row)" type="success" plain>
                <el-icon><Money /></el-icon>
                充值
              </el-button>
              <el-popconfirm
                title="确定要删除这条数据吗？"
                @confirm="handleDelete(scope.row)"
              >
                <template #reference>
                  <el-button size="small" type="danger" plain>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="600px"
      :close-on-click-modal="false"
      :destroy-on-close="true"
    >
      <el-form ref="customerFormRef" :model="customerForm" :rules="customerRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="customerForm.username" 
                :disabled="dialogTitle === '编辑客户'"
                placeholder="请输入用户名" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="dialogTitle === '新增客户'">
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="customerForm.password" 
                type="password" 
                show-password
                placeholder="请输入密码" 
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="customerForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="customerForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="customerForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="customerForm.gender">
                <el-radio label="M">男</el-radio>
                <el-radio label="F">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number 
                v-model="customerForm.age" 
                :min="1" 
                :max="120" 
                :precision="0"
                :controls="false"
                style="width: 100%;"
                placeholder="请输入年龄"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会员等级" prop="memberLevel">
              <el-select v-model="customerForm.memberLevel" placeholder="请选择会员等级" style="width: 100%;">
                <el-option label="普通会员" value="普通会员" />
                <el-option label="VIP会员" value="VIP会员" />
                <el-option label="SVIP会员" value="SVIP会员" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="余额" prop="balance">
              <el-input-number 
                v-model="customerForm.balance" 
                :min="0" 
                :precision="2" 
                :controls="false"
                style="width: 100%;"
                placeholder="请输入余额"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="customerForm.status" placeholder="请选择状态" style="width: 100%;">
                <el-option label="正常" value="正常" />
                <el-option label="冻结" value="冻结" />
                <el-option label="注销" value="注销" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog 
      title="客户充值" 
      v-model="rechargeDialogVisible" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rechargeForm" label-width="100px">
        <el-form-item label="客户">
          <span>{{ rechargeForm.customerName }}</span>
        </el-form-item>
        <el-form-item label="当前余额">
          <span style="color: #67c23a; font-weight: bold;">¥{{ rechargeForm.currentBalance }}</span>
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number 
            v-model="rechargeForm.amount" 
            :min="0.01" 
            :precision="2" 
            :controls="false"
            style="width: 100%;" 
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="rechargeForm.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rechargeDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleRechargeSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCustomerList, addCustomer, updateCustomer, delCustomer, rechargeBalance } from '@/api/customer'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStatusType, debounce } from '@/composables/useTable'
import StatusTag from '@/components/StatusTag.vue'
import {
  Search,
  Refresh,
  Plus,
  Download,
  Edit,
  Money,
  Delete
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const customerList = ref([])
const dialogVisible = ref(false)
const rechargeDialogVisible = ref(false)
const dialogTitle = ref('')

// 搜索表单
const searchForm = reactive({
  username: '',
  phone: '',
  memberLevel: '',
  status: '',
  createTimeRange: []
})

// 分页信息
const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 客户表单
const customerForm = reactive({
  customerId: null,
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 'M',
  age: 18,
  memberLevel: '普通会员',
  balance: 0,
  status: '正常'
})

// 充值表单
const rechargeForm = reactive({
  customerId: '',
  customerName: '',
  currentBalance: 0,
  amount: 0,
  remark: ''
})

// 表单验证规则
const customerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在6到20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const customerFormRef = ref()

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
    
    const response = await getCustomerList(params)
    customerList.value = response.data.records || []
    page.total = response.data.total || 0
  } catch (error) {
    console.error('获取客户列表失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索（带防抖）
const handleSearch = debounce(() => {
  page.current = 1
  fetchData()
}, 300)

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.memberLevel = ''
  searchForm.status = ''
  searchForm.createTimeRange = []
  handleSearch()
}

// 刷新数据
const refreshData = () => {
  fetchData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增客户'
  Object.assign(customerForm, {
    customerId: null,
    username: '',
    password: '',
    nickname: '',
    phone: '',
    email: '',
    gender: 'M',
    age: 18,
    memberLevel: '普通会员',
    balance: 0,
    status: '正常'
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑客户'
  Object.assign(customerForm, { ...row })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await delCustomer(row.customerId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 提交表单
const handleSubmit = () => {
  customerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (!customerForm.customerId) {
          // 新增
          await addCustomer(customerForm)
          ElMessage.success('新增成功')
        } else {
          // 更新
          await updateCustomer(customerForm)
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 充值
const handleRecharge = (row) => {
  rechargeForm.customerId = row.customerId
  rechargeForm.customerName = row.username
  rechargeForm.currentBalance = row.balance
  rechargeForm.amount = 0
  rechargeForm.remark = ''
  rechargeDialogVisible.value = true
}

// 充值提交
const handleRechargeSubmit = async () => {
  if (rechargeForm.amount <= 0) {
    ElMessage.error('充值金额必须大于0')
    return
  }
  
  try {
    const response = await rechargeBalance({
      customerId: rechargeForm.customerId,
      amount: rechargeForm.amount
    })
    if (response.code === 200) {
      ElMessage.success('充值成功')
      rechargeDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(response.message || '充值失败')
    }
  } catch (error) {
    console.error('充值失败:', error)
    ElMessage.error('充值失败')
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

.table-container {
  height: calc(100vh - 280px);
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