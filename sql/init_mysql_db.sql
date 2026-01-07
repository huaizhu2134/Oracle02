-- MySQL数据库初始化脚本

-- 1. 创建表结构
SOURCE create_tables_mysql.sql;

-- 2. 插入基础数据
SOURCE insert_staff_data_mysql.sql;
SOURCE insert_customer_data_mysql.sql;
SOURCE insert_order_data_mysql.sql;
SOURCE insert_evaluation_data_mysql.sql;
SOURCE insert_complaint_data_mysql.sql;

-- 3. 插入其他数据（如果存在）
SOURCE insert_finance_data_mysql.sql;
SOURCE insert_mock_data_mysql.sql;

-- 4. 插入字典数据
-- 注意：字典数据已经在create_tables_mysql.sql中插入

-- 5. 创建触发器、函数和存储过程
SOURCE triggers_functions_procedures_mysql.sql;

-- 6. 更新统计数据（如果需要）
SOURCE update_statistics_mysql.sql;

-- 完成
SELECT 'MySQL数据库初始化完成' AS message;