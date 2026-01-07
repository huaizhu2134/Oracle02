<template>
  <div class="management-container">
    <!-- 搜索栏 -->
    <el-card class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="陪玩昵称">
          <el-input 
            v-model="searchForm.staffName" 
            placeholder="请输入陪玩昵称" 
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="技能等级">
          <el-select v-model="searchForm.skillLevel" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="全部" value="" />
            <el-option label="青铜" value="青铜" />
            <el-option label="白银" value="白银" />
            <el-option label="黄金" value="黄金" />
            <el-option label="铂金" value="铂金" />
            <el-option label="钻石" value="钻石" />
            <el-option label="王者" value="王者" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="全部" value="" />
            <el-option label="空闲" value="空闲" />
            <el-option label="忙碌" value="忙碌" />
            <el-option label="离线" value="离线" />
            <el-option label="封禁" value="封禁" />
          </el-select>
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
            新增陪玩
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
        :data="staffList"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#fafafa', color: '#606266' }"
        class="data-table"
        height="calc(100vh - 320px)"
      >
        <el-table-column type="index" width="50" label="排序" />
        <el-table-column prop="staffId" label="ID" width="80" />
        <el-table-column prop="staffName" label="陪玩昵称" width="120" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" width="100" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="60" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.gender === 'M' ? 'primary' : 'danger'" size="small">
              {{ scope.row.gender === 'M' ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="60" align="center" />
        <el-table-column prop="phone" label="手机号" width="130" show-overflow-tooltip />
        <el-table-column prop="skillLevel" label="技能等级" width="90" align="center">
          <template #default="scope">
            <StatusTag :status="scope.row.skillLevel" />
          </template>
        </el-table-column>
        <el-table-column prop="serviceType" label="服务类型" width="150" show-overflow-tooltip />
        <el-table-column prop="unitPrice" label="单价(元/小时)" width="120" align="right">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.unitPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalIncome" label="总收入(元)" width="120" align="right">
          <template #default="scope">
            <span style="color: #67c23a; font-weight: bold;">¥{{ scope.row.totalIncome }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="scope">
            <StatusTag :status="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" @click="handleEdit(scope.row)" type="primary" plain>
                <el-icon><Edit /></el-icon>
                编辑
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
      <el-form ref="staffFormRef" :model="staffForm" :rules="staffRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="陪玩昵称" prop="staffName">
              <el-input v-model="staffForm.staffName" placeholder="请输入陪玩昵称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="staffForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="staffForm.gender">
                <el-radio label="M">男</el-radio>
                <el-radio label="F">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number 
                v-model="staffForm.age" 
                :min="18" 
                :max="60" 
                :precision="0"
                :controls="false"
                style="width: 100%;"
                placeholder="请输入年龄"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="staffForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="staffForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="技能等级" prop="skillLevel">
              <el-select v-model="staffForm.skillLevel" placeholder="请选择技能等级" style="width: 100%;">
                <el-option label="青铜" value="青铜" />
                <el-option label="白银" value="白银" />
                <el-option label="黄金" value="黄金" />
                <el-option label="铂金" value="铂金" />
                <el-option label="钻石" value="钻石" />
                <el-option label="王者" value="王者" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价(元/小时)" prop="unitPrice">
              <el-input-number 
                v-model="staffForm.unitPrice" 
                :min="0" 
                :precision="2" 
                :controls="false"
                style="width: 100%;"
                placeholder="请输入单价"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="服务类型" prop="serviceType">
          <el-checkbox-group v-model="staffForm.serviceType">
            <el-checkbox label="英雄联盟" />
            <el-checkbox label="王者荣耀" />
            <el-checkbox label="和平精英" />
            <el-checkbox label="绝地求生" />
            <el-checkbox label="原神" />
            <el-checkbox label="其他" />
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-select v-model="staffForm.status" placeholder="请选择状态" style="width: 200px;">
            <el-option label="空闲" value="空闲" />
            <el-option label="忙碌" value="忙碌" />
            <el-option label="离线" value="离线" />
            <el-option label="封禁" value="封禁" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStaffList, addStaff, updateStaff, delStaff } from '@/api/staff'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStatusType, debounce } from '@/composables/useTable'
import StatusTag from '@/components/StatusTag.vue'
import {
  Search,
  Plus,
  Edit,
  Delete,
  Refresh,
  Download
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const staffList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')

// 搜索表单
const searchForm = reactive({
  staffName: '',
  skillLevel: '',
  status: ''
})

// 分页信息
const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表单数据
const staffForm = reactive({
  staffId: null,
  staffName: '',
  realName: '',
  gender: 'M',
  age: 18,
  phone: '',
  email: '',
  skillLevel: '黄金',
  serviceType: [],
  unitPrice: 50,
  status: '空闲'
})

// 表单验证规则
const staffRules = {
  staffName: [
    { required: true, message: '请输入陪玩昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  unitPrice: [
    { required: true, message: '请输入单价', trigger: 'blur' },
    { type: 'number', min: 0, message: '单价不能小于0', trigger: 'blur' }
  ]
}

const staffFormRef = ref()

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const response = await getStaffList({
      ...searchForm,
      page: page.current,
      size: page.size
    })
    staffList.value = response.data.records || []
    page.total = response.data.total || 0
  } catch (error) {
    console.error('获取陪玩人员列表失败:', error)
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
  searchForm.staffName = ''
  searchForm.skillLevel = ''
  searchForm.status = ''
  handleSearch()
}

// 刷新数据
const refreshData = () => {
  fetchData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增陪玩'
  Object.assign(staffForm, {
    staffId: null,
    staffName: '',
    realName: '',
    gender: 'M',
    age: 18,
    phone: '',
    email: '',
    skillLevel: '黄金',
    serviceType: [],
    unitPrice: 50,
    status: '空闲'
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑陪玩'
  Object.assign(staffForm, { ...row })
  // 将服务类型字符串转换为数组
  staffForm.serviceType = row.serviceType ? row.serviceType.split(',') : []
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await delStaff(row.staffId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 提交表单
const handleSubmit = () => {
  staffFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = { ...staffForm }
        // 将服务类型数组转换为字符串
        formData.serviceType = Array.isArray(formData.serviceType) 
          ? formData.serviceType.join(',') 
          : formData.serviceType
        
        if (!formData.staffId) {
          // 新增
          await addStaff(formData)
          ElMessage.success('新增成功')
        } else {
          // 更新
          await updateStaff(formData)
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
  padding: var(--spacing-md);
  background: var(--bg-color-page);
  min-height: 100%;
}

.search-bar,
.operation-bar {
  margin-bottom: var(--spacing-md);
  border: none;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  background-color: var(--color-white);
  padding: var(--spacing-md);
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
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
  height: calc(100vh - 300px);
  background: var(--color-white);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color-light);
}

.data-table :deep(.el-table__header-wrapper) {
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.data-table :deep(.el-table__body-wrapper) {
  border-radius: 0 0 var(--radius-md) var(--radius-md);
}

.action-buttons .el-button {
  margin: 0 4px;
  transition: all 0.3s;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: var(--spacing-md) 0;
  background-color: var(--color-white);
  border-top: 1px solid var(--border-color-light);
  border-radius: 0 0 var(--radius-md) var(--radius-md);
  margin-top: var(--spacing-sm);
}
</style>