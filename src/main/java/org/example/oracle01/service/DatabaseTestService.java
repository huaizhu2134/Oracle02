package org.example.oracle01.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class DatabaseTestService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String testConnection() {
        try {
            // 尝试查询数据库版本
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            return "数据库连接成功！版本信息：" + version;
        } catch (Exception e) {
            return "数据库连接失败：" + e.getMessage();
        }
    }

    public String createTables() {
        try {
            // 检查是否已存在表
            int tableCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('TB_STAFF', 'TB_CUSTOMER', 'TB_ORDER')",
                    Integer.class);

            if (tableCount > 0) {
                return "检测到已有部分表存在，跳过创建";
            }

            // 创建表的SQL语句
            String[] sqlStatements = {
                    // 陪玩人员表
                    "CREATE TABLE TB_STAFF (" +
                            "STAFF_ID INT AUTO_INCREMENT PRIMARY KEY," +
                            "STAFF_NAME VARCHAR(50) NOT NULL," +
                            "REAL_NAME VARCHAR(50)," +
                            "GENDER CHAR(1) CHECK (GENDER IN ('M', 'F'))," +
                            "AGE INT CHECK (AGE BETWEEN 18 AND 60)," +
                            "PHONE VARCHAR(20) UNIQUE NOT NULL," +
                            "EMAIL VARCHAR(100)," +
                            "AVATAR_URL VARCHAR(500)," +
                            "SKILL_LEVEL VARCHAR(20) DEFAULT '黄金'," +
                            "SERVICE_TYPE VARCHAR(200)," +
                            "UNIT_PRICE DECIMAL(10,2) DEFAULT 0 CHECK (UNIT_PRICE >= 0)," +
                            "STATUS VARCHAR(20) DEFAULT '空闲' CHECK (STATUS IN ('空闲', '忙碌', '离线', '封禁'))," +
                            "TOTAL_ORDERS INT DEFAULT 0," +
                            "TOTAL_INCOME DECIMAL(10,2) DEFAULT 0," +
                            "AVG_SCORE DECIMAL(3,2) DEFAULT 5.00," +
                            "CERT_STATUS VARCHAR(20) DEFAULT '未认证'," +
                            "CREATE_TIME DATETIME DEFAULT NOW()," +
                            "UPDATE_TIME DATETIME DEFAULT NOW() ON UPDATE NOW()," +
                            "LAST_LOGIN_TIME DATETIME," +
                            "IS_DELETED CHAR(1) DEFAULT 'N'" +
                            ")",

                    // 客户表
                    "CREATE TABLE TB_CUSTOMER (" +
                            "CUSTOMER_ID INT AUTO_INCREMENT PRIMARY KEY," +
                            "USERNAME VARCHAR(50) UNIQUE NOT NULL," +
                            "PASSWORD VARCHAR(100) NOT NULL," +
                            "NICKNAME VARCHAR(50)," +
                            "PHONE VARCHAR(20) UNIQUE NOT NULL," +
                            "EMAIL VARCHAR(100)," +
                            "GENDER CHAR(1) CHECK (GENDER IN ('M', 'F'))," +
                            "AGE INT," +
                            "AVATAR_URL VARCHAR(500)," +
                            "MEMBER_LEVEL VARCHAR(20) DEFAULT '普通会员'," +
                            "TOTAL_CONSUME DECIMAL(10,2) DEFAULT 0," +
                            "ORDER_COUNT INT DEFAULT 0," +
                            "BALANCE DECIMAL(10,2) DEFAULT 0," +
                            "STATUS VARCHAR(20) DEFAULT '正常' CHECK (STATUS IN ('正常', '冻结', '注销'))," +
                            "CREATE_TIME DATETIME DEFAULT NOW()," +
                            "UPDATE_TIME DATETIME DEFAULT NOW() ON UPDATE NOW()," +
                            "LAST_LOGIN_TIME DATETIME," +
                            "IS_DELETED CHAR(1) DEFAULT 'N'" +
                            ")",

                    // 订单表
                    "CREATE TABLE TB_ORDER (" +
                            "ORDER_ID BIGINT AUTO_INCREMENT PRIMARY KEY," +
                            "ORDER_NO VARCHAR(50) UNIQUE NOT NULL," +
                            "CUSTOMER_ID INT NOT NULL," +
                            "STAFF_ID INT NOT NULL," +
                            "GAME_TYPE VARCHAR(50) NOT NULL," +
                            "SERVICE_HOURS INT DEFAULT 1 CHECK (SERVICE_HOURS > 0)," +
                            "UNIT_PRICE DECIMAL(10,2) NOT NULL CHECK (UNIT_PRICE >= 0)," +
                            "TOTAL_AMOUNT DECIMAL(10,2) NOT NULL CHECK (TOTAL_AMOUNT >= 0)," +
                            "PLATFORM_COMMISSION DECIMAL(10,2) DEFAULT 0," +
                            "STAFF_INCOME DECIMAL(10,2) DEFAULT 0," +
                            "ORDER_STATUS VARCHAR(20) DEFAULT '待支付' " +
                            "CHECK (ORDER_STATUS IN ('待支付', '已支付', '服务中', '已完成', '已取消', '退款中', '已退款'))," +
                            "PAY_TIME DATETIME," +
                            "START_TIME DATETIME," +
                            "END_TIME DATETIME," +
                            "CUSTOMER_COMMENT VARCHAR(500)," +
                            "STAFF_COMMENT VARCHAR(500)," +
                            "CREATE_TIME DATETIME DEFAULT NOW()," +
                            "UPDATE_TIME DATETIME DEFAULT NOW() ON UPDATE NOW()," +
                            "IS_DELETED CHAR(1) DEFAULT 'N'," +
                            "CONSTRAINT FK_ORDER_CUSTOMER FOREIGN KEY (CUSTOMER_ID) REFERENCES TB_CUSTOMER(CUSTOMER_ID),"
                            +
                            "CONSTRAINT FK_ORDER_STAFF FOREIGN KEY (STAFF_ID) REFERENCES TB_STAFF(STAFF_ID)" +
                            ")",

                    // 评价表
                    "CREATE TABLE TB_EVALUATION (" +
                            "EVAL_ID BIGINT AUTO_INCREMENT PRIMARY KEY," +
                            "ORDER_ID BIGINT NOT NULL," +
                            "CUSTOMER_ID INT NOT NULL," +
                            "STAFF_ID INT NOT NULL," +
                            "SCORE INT CHECK (SCORE BETWEEN 1 AND 5)," +
                            "CONTENT VARCHAR(500)," +
                            "TAGS VARCHAR(200)," +
                            "IS_ANONYMOUS CHAR(1) DEFAULT 'N'," +
                            "STAFF_REPLY VARCHAR(500)," +
                            "REPLY_TIME DATETIME," +
                            "CREATE_TIME DATETIME DEFAULT NOW()," +
                            "IS_DELETED CHAR(1) DEFAULT 'N'," +
                            "CONSTRAINT FK_EVAL_ORDER FOREIGN KEY (ORDER_ID) REFERENCES TB_ORDER(ORDER_ID)," +
                            "CONSTRAINT FK_EVAL_CUSTOMER FOREIGN KEY (CUSTOMER_ID) REFERENCES TB_CUSTOMER(CUSTOMER_ID),"
                            +
                            "CONSTRAINT FK_EVAL_STAFF FOREIGN KEY (STAFF_ID) REFERENCES TB_STAFF(STAFF_ID)" +
                            ")",

                    // 投诉表
                    "CREATE TABLE TB_COMPLAINT (" +
                            "COMPLAINT_ID BIGINT AUTO_INCREMENT PRIMARY KEY," +
                            "ORDER_ID BIGINT," +
                            "CUSTOMER_ID INT NOT NULL," +
                            "STAFF_ID INT," +
                            "COMPLAINT_TYPE VARCHAR(50) NOT NULL," +
                            "COMPLAINT_CONTENT VARCHAR(1000) NOT NULL," +
                            "EVIDENCE_URL VARCHAR(500)," +
                            "COMPLAINT_STATUS VARCHAR(20) DEFAULT '待处理' " +
                            "CHECK (COMPLAINT_STATUS IN ('待处理', '处理中', '已解决', '已关闭'))," +
                            "HANDLER_ID INT," +
                            "HANDLER_COMMENT VARCHAR(500)," +
                            "HANDLE_TIME DATETIME," +
                            "CREATE_TIME DATETIME DEFAULT NOW()," +
                            "UPDATE_TIME DATETIME DEFAULT NOW() ON UPDATE NOW()," +
                            "IS_DELETED CHAR(1) DEFAULT 'N'," +
                            "CONSTRAINT FK_COMP_ORDER FOREIGN KEY (ORDER_ID) REFERENCES TB_ORDER(ORDER_ID)," +
                            "CONSTRAINT FK_COMP_CUSTOMER FOREIGN KEY (CUSTOMER_ID) REFERENCES TB_CUSTOMER(CUSTOMER_ID),"
                            +
                            "CONSTRAINT FK_COMP_STAFF FOREIGN KEY (STAFF_ID) REFERENCES TB_STAFF(STAFF_ID)" +
                            ")",

                    // 管理员表
                    "CREATE TABLE TB_ADMIN (" +
                            "ADMIN_ID INT AUTO_INCREMENT PRIMARY KEY," +
                            "USERNAME VARCHAR(50) UNIQUE NOT NULL," +
                            "PASSWORD VARCHAR(100) NOT NULL," +
                            "NICKNAME VARCHAR(50)," +
                            "PHONE VARCHAR(20)," +
                            "EMAIL VARCHAR(100)," +
                            "ROLE_ID INT NOT NULL," +
                            "STATUS VARCHAR(20) DEFAULT '正常' CHECK (STATUS IN ('正常', '冻结'))," +
                            "LAST_LOGIN_TIME DATETIME," +
                            "CREATE_TIME DATETIME DEFAULT NOW()," +
                            "UPDATE_TIME DATETIME DEFAULT NOW() ON UPDATE NOW()," +
                            "IS_DELETED CHAR(1) DEFAULT 'N'," +
                            "CONSTRAINT FK_ADMIN_ROLE FOREIGN KEY (ROLE_ID) REFERENCES TB_ROLE(ROLE_ID)" +
                            ")",

                    // 角色表
                    "CREATE TABLE TB_ROLE (" +
                            "ROLE_ID INT AUTO_INCREMENT PRIMARY KEY," +
                            "ROLE_NAME VARCHAR(50) NOT NULL," +
                            "ROLE_DESC VARCHAR(200)," +
                            "PERMISSIONS VARCHAR(2000)," +
                            "CREATE_TIME DATETIME DEFAULT NOW()," +
                            "UPDATE_TIME DATETIME DEFAULT NOW() ON UPDATE NOW()," +
                            "IS_DELETED CHAR(1) DEFAULT 'N'" +
                            ")",

                    // 数据字典表
                    "CREATE TABLE TB_DICTIONARY (" +
                            "DICT_ID INT AUTO_INCREMENT PRIMARY KEY," +
                            "DICT_TYPE VARCHAR(50) NOT NULL," +
                            "DICT_CODE VARCHAR(50) NOT NULL," +
                            "DICT_VALUE VARCHAR(100) NOT NULL," +
                            "DICT_DESC VARCHAR(200)," +
                            "SORT_ORDER INT DEFAULT 0," +
                            "CREATE_TIME DATETIME DEFAULT NOW()," +
                            "IS_DELETED CHAR(1) DEFAULT 'N'," +
                            "UNIQUE KEY UK_DICT_TYPE_CODE (DICT_TYPE, DICT_CODE)" +
                            ")",

                    // 操作日志表
                    "CREATE TABLE TB_OPERATION_LOG (" +
                            "LOG_ID BIGINT AUTO_INCREMENT PRIMARY KEY," +
                            "ADMIN_ID INT," +
                            "ADMIN_NAME VARCHAR(50)," +
                            "OPERATION_TYPE VARCHAR(50)," +
                            "OPERATION_DESC VARCHAR(500)," +
                            "REQUEST_URL VARCHAR(200)," +
                            "REQUEST_METHOD VARCHAR(20)," +
                            "REQUEST_IP VARCHAR(50)," +
                            "REQUEST_PARAMS VARCHAR(2000)," +
                            "RESPONSE_RESULT VARCHAR(2000)," +
                            "EXECUTE_TIME INT," +
                            "CREATE_TIME DATETIME DEFAULT NOW()," +
                            "IS_DELETED CHAR(1) DEFAULT 'N'," +
                            "CONSTRAINT FK_LOG_ADMIN FOREIGN KEY (ADMIN_ID) REFERENCES TB_ADMIN(ADMIN_ID)" +
                            ")"
            };

            // 执行创建表的SQL
            for (String sql : sqlStatements) {
                jdbcTemplate.execute(sql);
            }

            // 创建索引
            String[] indexStatements = {
                    "CREATE INDEX IDX_STAFF_NAME ON TB_STAFF(STAFF_NAME)",
                    "CREATE INDEX IDX_STAFF_STATUS ON TB_STAFF(STATUS)",
                    "CREATE INDEX IDX_STAFF_SKILL ON TB_STAFF(SKILL_LEVEL)",
                    "CREATE INDEX IDX_STAFF_CREATE_TIME ON TB_STAFF(CREATE_TIME)",
                    "CREATE INDEX IDX_CUSTOMER_USERNAME ON TB_CUSTOMER(USERNAME)",
                    "CREATE INDEX IDX_CUSTOMER_PHONE ON TB_CUSTOMER(PHONE)",
                    "CREATE INDEX IDX_CUSTOMER_STATUS ON TB_CUSTOMER(STATUS)",
                    "CREATE INDEX IDX_CUSTOMER_CREATE_TIME ON TB_CUSTOMER(CREATE_TIME)",
                    "CREATE INDEX IDX_ORDER_NO ON TB_ORDER(ORDER_NO)",
                    "CREATE INDEX IDX_ORDER_CUSTOMER ON TB_ORDER(CUSTOMER_ID)",
                    "CREATE INDEX IDX_ORDER_STAFF ON TB_ORDER(STAFF_ID)",
                    "CREATE INDEX IDX_ORDER_STATUS ON TB_ORDER(ORDER_STATUS)",
                    "CREATE INDEX IDX_EVAL_ORDER ON TB_EVALUATION(ORDER_ID)",
                    "CREATE INDEX IDX_EVAL_STAFF ON TB_EVALUATION(STAFF_ID)",
                    "CREATE INDEX IDX_EVAL_SCORE ON TB_EVALUATION(SCORE)",
                    "CREATE INDEX IDX_EVAL_CREATE_TIME ON TB_EVALUATION(CREATE_TIME)",
                    "CREATE INDEX IDX_COMP_STATUS ON TB_COMPLAINT(COMPLAINT_STATUS)",
                    "CREATE INDEX IDX_COMP_CREATE_TIME ON TB_COMPLAINT(CREATE_TIME)",
                    "CREATE INDEX IDX_ADMIN_USERNAME ON TB_ADMIN(USERNAME)",
                    "CREATE INDEX IDX_ADMIN_ROLE ON TB_ADMIN(ROLE_ID)",
                    "CREATE INDEX IDX_DICT_TYPE ON TB_DICTIONARY(DICT_TYPE)",
                    "CREATE INDEX IDX_LOG_ADMIN ON TB_OPERATION_LOG(ADMIN_ID)",
                    "CREATE INDEX IDX_LOG_CREATE_TIME ON TB_OPERATION_LOG(CREATE_TIME)",
                    "CREATE INDEX IDX_LOG_OPERATION_TYPE ON TB_OPERATION_LOG(OPERATION_TYPE)"
            };

            for (String sql : indexStatements) {
                jdbcTemplate.execute(sql);
            }

            // 插入默认角色
            String[] insertRoleStatements = {
                    "INSERT INTO TB_ROLE (ROLE_NAME, ROLE_DESC, PERMISSIONS) VALUES " +
                            "('超级管理员', '拥有所有权限', 'all')",
                    "INSERT INTO TB_ROLE (ROLE_NAME, ROLE_DESC, PERMISSIONS) VALUES " +
                            "('运营管理员', '陪玩人员管理、订单管理', 'staff,order,evaluation')",
                    "INSERT INTO TB_ROLE (ROLE_NAME, ROLE_DESC, PERMISSIONS) VALUES " +
                            "('客服人员', '投诉处理、评价管理', 'complaint,evaluation')",
                    "INSERT INTO TB_ROLE (ROLE_NAME, ROLE_DESC, PERMISSIONS) VALUES " +
                            "('财务人员', '财务管理、提现审核', 'finance,withdraw')",
                    "INSERT INTO TB_ROLE (ROLE_NAME, ROLE_DESC, PERMISSIONS) VALUES " +
                            "('查看员', '仅查看权限', 'view')"
            };

            for (String sql : insertRoleStatements) {
                jdbcTemplate.execute(sql);
            }

            // 插入默认数据字典
            String[] insertDictStatements = {
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('GAME_TYPE', 'LOL', '英雄联盟', 1, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('GAME_TYPE', 'WZRY', '王者荣耀', 2, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('GAME_TYPE', 'HPJY', '和平精英', 3, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('GAME_TYPE', 'JDCQ', '绝地求生', 4, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('GAME_TYPE', 'DNF', '地下城与勇士', 5, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('SKILL_LEVEL', 'BRONZE', '青铜', 1, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('SKILL_LEVEL', 'SILVER', '白银', 2, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('SKILL_LEVEL', 'GOLD', '黄金', 3, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('SKILL_LEVEL', 'PLATINUM', '铂金', 4, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('SKILL_LEVEL', 'DIAMOND', '钻石', 5, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('SKILL_LEVEL', 'MASTER', '王者', 6, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('COMPLAINT_TYPE', 'ATTITUDE', '服务态度', 1, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('COMPLAINT_TYPE', 'QUALITY', '服务质量', 2, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('COMPLAINT_TYPE', 'PRICE', '收费问题', 3, NOW(), 'N')",
                    "INSERT INTO TB_DICTIONARY (DICT_TYPE, DICT_CODE, DICT_VALUE, SORT_ORDER, CREATE_TIME, IS_DELETED) VALUES ('COMPLAINT_TYPE', 'OTHER', '其他', 4, NOW(), 'N')"
            };

            for (String sql : insertDictStatements) {
                jdbcTemplate.execute(sql);
            }

            return "所有表和初始数据创建成功！";
        } catch (Exception e) {
            return "创建表失败：" + e.getMessage();
        }
    }

    /**
     * 初始化测试数据
     */
    public String initTestData() {
        try {
            // 检查是否已有数据
            int staffCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM TB_STAFF", Integer.class);
            if (staffCount > 0) {
                return "数据库已有数据，跳过初始化。如需重新初始化，请先清空数据。";
            }

            // 插入陪玩人员数据
            String[] staffInserts = {
                    "INSERT INTO TB_STAFF (STAFF_NAME, REAL_NAME, GENDER, AGE, PHONE, EMAIL, SKILL_LEVEL, SERVICE_TYPE, UNIT_PRICE, STATUS, TOTAL_ORDERS, TOTAL_INCOME, AVG_SCORE, CERT_STATUS, CREATE_TIME) VALUES ('游戏达人小王', '王明', 'M', 22, '13800138001', 'wang@example.com', '钻石', '单排,双排,开黑', 50.00, '空闲', 128, 6400.00, 4.8, '已认证', DATE_SUB(NOW(), INTERVAL 30 DAY))",
                    "INSERT INTO TB_STAFF (STAFF_NAME, REAL_NAME, GENDER, AGE, PHONE, EMAIL, SKILL_LEVEL, SERVICE_TYPE, UNIT_PRICE, STATUS, TOTAL_ORDERS, TOTAL_INCOME, AVG_SCORE, CERT_STATUS, CREATE_TIME) VALUES ('电竞女神小李', '李娜', 'F', 20, '13800138002', 'li@example.com', '王者', '陪练,语音,视频', 80.00, '空闲', 256, 20480.00, 4.9, '已认证', DATE_SUB(NOW(), INTERVAL 60 DAY))",
                    "INSERT INTO TB_STAFF (STAFF_NAME, REAL_NAME, GENDER, AGE, PHONE, EMAIL, SKILL_LEVEL, SERVICE_TYPE, UNIT_PRICE, STATUS, TOTAL_ORDERS, TOTAL_INCOME, AVG_SCORE, CERT_STATUS, CREATE_TIME) VALUES ('职业选手阿杰', '张杰', 'M', 24, '13800138003', 'zhang@example.com', '王者', '教学,五排', 100.00, '忙碌', 512, 51200.00, 5.0, '已认证', DATE_SUB(NOW(), INTERVAL 90 DAY))",
                    "INSERT INTO TB_STAFF (STAFF_NAME, REAL_NAME, GENDER, AGE, PHONE, EMAIL, SKILL_LEVEL, SERVICE_TYPE, UNIT_PRICE, STATUS, TOTAL_ORDERS, TOTAL_INCOME, AVG_SCORE, CERT_STATUS, CREATE_TIME) VALUES ('萌妹子小雪', '刘雪', 'F', 19, '13800138004', 'liu@example.com', '铂金', '单排,语音', 40.00, '空闲', 64, 2560.00, 4.5, '已认证', DATE_SUB(NOW(), INTERVAL 45 DAY))",
                    "INSERT INTO TB_STAFF (STAFF_NAME, REAL_NAME, GENDER, AGE, PHONE, EMAIL, SKILL_LEVEL, SERVICE_TYPE, UNIT_PRICE, STATUS, TOTAL_ORDERS, TOTAL_INCOME, AVG_SCORE, CERT_STATUS, CREATE_TIME) VALUES ('技术流小陈', '陈伟', 'M', 26, '13800138005', 'chen@example.com', '钻石', '双排,开黑,教学', 60.00, '离线', 96, 5760.00, 4.7, '已认证', DATE_SUB(NOW(), INTERVAL 15 DAY))"
            };
            for (String sql : staffInserts) {
                jdbcTemplate.execute(sql);
            }

            // 插入客户数据
            String[] customerInserts = {
                    "INSERT INTO TB_CUSTOMER (USERNAME, PASSWORD, NICKNAME, PHONE, EMAIL, GENDER, AGE, MEMBER_LEVEL, TOTAL_CONSUME, ORDER_COUNT, BALANCE, STATUS, CREATE_TIME) VALUES ('user001', 'e10adc3949ba59abbe56e057f20f883e', '游戏玩家小明', '13900139001', 'xiaoming@example.com', 'M', 25, 'VIP会员', 1500.00, 30, 200.00, '正常', DATE_SUB(NOW(), INTERVAL 100 DAY))",
                    "INSERT INTO TB_CUSTOMER (USERNAME, PASSWORD, NICKNAME, PHONE, EMAIL, GENDER, AGE, MEMBER_LEVEL, TOTAL_CONSUME, ORDER_COUNT, BALANCE, STATUS, CREATE_TIME) VALUES ('user002', 'e10adc3949ba59abbe56e057f20f883e', '快乐玩家小红', '13900139002', 'xiaohong@example.com', 'F', 22, 'SVIP会员', 5000.00, 100, 500.00, '正常', DATE_SUB(NOW(), INTERVAL 80 DAY))",
                    "INSERT INTO TB_CUSTOMER (USERNAME, PASSWORD, NICKNAME, PHONE, EMAIL, GENDER, AGE, MEMBER_LEVEL, TOTAL_CONSUME, ORDER_COUNT, BALANCE, STATUS, CREATE_TIME) VALUES ('user003', 'e10adc3949ba59abbe56e057f20f883e', '菜鸟玩家小刚', '13900139003', 'xiaogang@example.com', 'M', 18, '普通会员', 300.00, 6, 50.00, '正常', DATE_SUB(NOW(), INTERVAL 50 DAY))",
                    "INSERT INTO TB_CUSTOMER (USERNAME, PASSWORD, NICKNAME, PHONE, EMAIL, GENDER, AGE, MEMBER_LEVEL, TOTAL_CONSUME, ORDER_COUNT, BALANCE, STATUS, CREATE_TIME) VALUES ('user004', 'e10adc3949ba59abbe56e057f20f883e', '大神玩家阿强', '13900139004', 'aqiang@example.com', 'M', 28, 'VIP会员', 2000.00, 40, 100.00, '正常', DATE_SUB(NOW(), INTERVAL 120 DAY))",
                    "INSERT INTO TB_CUSTOMER (USERNAME, PASSWORD, NICKNAME, PHONE, EMAIL, GENDER, AGE, MEMBER_LEVEL, TOTAL_CONSUME, ORDER_COUNT, BALANCE, STATUS, CREATE_TIME) VALUES ('user005', 'e10adc3949ba59abbe56e057f20f883e', '甜心玩家小美', '13900139005', 'xiaomei@example.com', 'F', 20, '普通会员', 800.00, 16, 80.00, '正常', DATE_SUB(NOW(), INTERVAL 30 DAY))"
            };
            for (String sql : customerInserts) {
                jdbcTemplate.execute(sql);
            }

            // 插入订单数据
            String[] orderInserts = {
                    "INSERT INTO TB_ORDER (ORDER_NO, CUSTOMER_ID, STAFF_ID, GAME_TYPE, SERVICE_HOURS, UNIT_PRICE, TOTAL_AMOUNT, ORDER_STATUS, PAY_TIME, CREATE_TIME) VALUES ('PM20251230000001', 20001, 10001, '英雄联盟', 2, 50.00, 100.00, '已完成', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY))",
                    "INSERT INTO TB_ORDER (ORDER_NO, CUSTOMER_ID, STAFF_ID, GAME_TYPE, SERVICE_HOURS, UNIT_PRICE, TOTAL_AMOUNT, ORDER_STATUS, PAY_TIME, CREATE_TIME) VALUES ('PM20251230000002', 20002, 10002, '王者荣耀', 3, 80.00, 240.00, '已完成', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY))",
                    "INSERT INTO TB_ORDER (ORDER_NO, CUSTOMER_ID, STAFF_ID, GAME_TYPE, SERVICE_HOURS, UNIT_PRICE, TOTAL_AMOUNT, ORDER_STATUS, PAY_TIME, CREATE_TIME) VALUES ('PM20251230000003', 20003, 10003, '和平精英', 1, 100.00, 100.00, '服务中', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY))",
                    "INSERT INTO TB_ORDER (ORDER_NO, CUSTOMER_ID, STAFF_ID, GAME_TYPE, SERVICE_HOURS, UNIT_PRICE, TOTAL_AMOUNT, ORDER_STATUS, PAY_TIME, CREATE_TIME) VALUES ('PM20251230000004', 20004, 10004, 'DNF', 2, 40.00, 80.00, '已支付', NOW(), NOW())",
                    "INSERT INTO TB_ORDER (ORDER_NO, CUSTOMER_ID, STAFF_ID, GAME_TYPE, SERVICE_HOURS, UNIT_PRICE, TOTAL_AMOUNT, ORDER_STATUS, CREATE_TIME) VALUES ('PM20251230000005', 20005, 10005, '原神', 2, 60.00, 120.00, '待支付', NOW())",
                    "INSERT INTO TB_ORDER (ORDER_NO, CUSTOMER_ID, STAFF_ID, GAME_TYPE, SERVICE_HOURS, UNIT_PRICE, TOTAL_AMOUNT, ORDER_STATUS, PAY_TIME, CREATE_TIME) VALUES ('PM20251230000006', 20001, 10002, '英雄联盟', 4, 80.00, 320.00, '已完成', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY))",
                    "INSERT INTO TB_ORDER (ORDER_NO, CUSTOMER_ID, STAFF_ID, GAME_TYPE, SERVICE_HOURS, UNIT_PRICE, TOTAL_AMOUNT, ORDER_STATUS, PAY_TIME, CREATE_TIME) VALUES ('PM20251230000007', 20002, 10003, '王者荣耀', 2, 100.00, 200.00, '已完成', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY))",
                    "INSERT INTO TB_ORDER (ORDER_NO, CUSTOMER_ID, STAFF_ID, GAME_TYPE, SERVICE_HOURS, UNIT_PRICE, TOTAL_AMOUNT, ORDER_STATUS, PAY_TIME, CREATE_TIME) VALUES ('PM20251230000008', 20003, 10001, '绝地求生', 3, 50.00, 150.00, '已完成', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY))"
            };
            for (String sql : orderInserts) {
                jdbcTemplate.execute(sql);
            }

            // 插入评价数据
            String[] evalInserts = {
                    "INSERT INTO TB_EVALUATION (ORDER_ID, CUSTOMER_ID, STAFF_ID, SCORE, CONTENT, TAGS, CREATE_TIME) VALUES (3000000001, 20001, 10001, 5, '技术很棒，态度也很好，下次还约！', '技术好,态度好', DATE_SUB(NOW(), INTERVAL 4 DAY))",
                    "INSERT INTO TB_EVALUATION (ORDER_ID, CUSTOMER_ID, STAFF_ID, SCORE, CONTENT, TAGS, CREATE_TIME) VALUES (3000000002, 20002, 10002, 5, '小姐姐声音好听，打得也很6！', '声音好听,技术好', DATE_SUB(NOW(), INTERVAL 3 DAY))",
                    "INSERT INTO TB_EVALUATION (ORDER_ID, CUSTOMER_ID, STAFF_ID, SCORE, CONTENT, TAGS, CREATE_TIME) VALUES (3000000006, 20001, 10002, 5, '非常专业，值得推荐', '专业,耐心', DATE_SUB(NOW(), INTERVAL 9 DAY))",
                    "INSERT INTO TB_EVALUATION (ORDER_ID, CUSTOMER_ID, STAFF_ID, SCORE, CONTENT, TAGS, CREATE_TIME) VALUES (3000000007, 20002, 10003, 5, '职业选手就是不一样，学到了很多', '经验丰富,教学好', DATE_SUB(NOW(), INTERVAL 7 DAY))",
                    "INSERT INTO TB_EVALUATION (ORDER_ID, CUSTOMER_ID, STAFF_ID, SCORE, CONTENT, TAGS, CREATE_TIME) VALUES (3000000008, 20003, 10001, 4, '还不错，配合默契', '配合好,准时', DATE_SUB(NOW(), INTERVAL 5 DAY))"
            };
            for (String sql : evalInserts) {
                jdbcTemplate.execute(sql);
            }

            // 插入投诉数据
            String[] complaintInserts = {
                    "INSERT INTO TB_COMPLAINT (ORDER_ID, CUSTOMER_ID, STAFF_ID, COMPLAINT_TYPE, COMPLAINT_CONTENT, COMPLAINT_STATUS, CREATE_TIME) VALUES (3000000001, 20001, 10001, '其他', '希望能提供更多游戏类型的陪玩服务', '已解决', DATE_SUB(NOW(), INTERVAL 3 DAY))",
                    "INSERT INTO TB_COMPLAINT (ORDER_ID, CUSTOMER_ID, STAFF_ID, COMPLAINT_TYPE, COMPLAINT_CONTENT, COMPLAINT_STATUS, CREATE_TIME) VALUES (3000000002, 20002, 10002, '服务态度', '陪玩态度一般，沟通不够积极', '处理中', DATE_SUB(NOW(), INTERVAL 2 DAY))"
            };
            for (String sql : complaintInserts) {
                jdbcTemplate.execute(sql);
            }

            // 插入管理员数据
            String[] adminInserts = {
                    "INSERT INTO TB_ADMIN (USERNAME, PASSWORD, NICKNAME, PHONE, EMAIL, ROLE_ID, STATUS, CREATE_TIME) VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员', '13700137001', 'admin@example.com', 70001, '正常', DATE_SUB(NOW(), INTERVAL 365 DAY))",
                    "INSERT INTO TB_ADMIN (USERNAME, PASSWORD, NICKNAME, PHONE, EMAIL, ROLE_ID, STATUS, CREATE_TIME) VALUES ('operator', 'e10adc3949ba59abbe56e057f20f883e', '运营专员', '13700137002', 'operator@example.com', 70002, '正常', DATE_SUB(NOW(), INTERVAL 180 DAY))",
                    "INSERT INTO TB_ADMIN (USERNAME, PASSWORD, NICKNAME, PHONE, EMAIL, ROLE_ID, STATUS, CREATE_TIME) VALUES ('service', 'e10adc3949ba59abbe56e057f20f883e', '客服专员', '13700137003', 'service@example.com', 70003, '正常', DATE_SUB(NOW(), INTERVAL 90 DAY))"
            };
            for (String sql : adminInserts) {
                jdbcTemplate.execute(sql);
            }

            return "测试数据初始化成功！已插入陪玩人员5条、客户5条、订单8条、评价5条、投诉2条、管理员3条。";
        } catch (Exception e) {
            return "初始化测试数据失败：" + e.getMessage();
        }
    }

    /**
     * 获取统计数据
     */
    public java.util.Map<String, Object> getStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        try {
            stats.put("staffCount",
                    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM TB_STAFF WHERE IS_DELETED = 'N'", Integer.class));
            stats.put("customerCount", jdbcTemplate
                    .queryForObject("SELECT COUNT(*) FROM TB_CUSTOMER WHERE IS_DELETED = 'N'", Integer.class));
            stats.put("orderCount",
                    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM TB_ORDER WHERE IS_DELETED = 'N'", Integer.class));
            stats.put("todayOrders", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM TB_ORDER WHERE IS_DELETED = 'N' AND DATE(CREATE_TIME) = CURDATE()",
                    Integer.class));

            Double totalIncome = jdbcTemplate.queryForObject(
                    "SELECT IFNULL(SUM(TOTAL_AMOUNT), 0) FROM TB_ORDER WHERE IS_DELETED = 'N' AND ORDER_STATUS = '已完成'",
                    Double.class);
            stats.put("totalIncome", totalIncome != null ? totalIncome : 0.0);

            Double todayIncome = jdbcTemplate.queryForObject(
                    "SELECT IFNULL(SUM(TOTAL_AMOUNT), 0) FROM TB_ORDER WHERE IS_DELETED = 'N' AND ORDER_STATUS = '已完成' AND DATE(CREATE_TIME) = CURDATE()",
                    Double.class);
            stats.put("todayIncome", todayIncome != null ? todayIncome : 0.0);

            stats.put("pendingOrders",
                    jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM TB_ORDER WHERE IS_DELETED = 'N' AND ORDER_STATUS IN ('待支付', '已支付')",
                            Integer.class));
            stats.put("completedOrders", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM TB_ORDER WHERE IS_DELETED = 'N' AND ORDER_STATUS = '已完成'", Integer.class));
            stats.put("pendingComplaints", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM TB_COMPLAINT WHERE IS_DELETED = 'N' AND COMPLAINT_STATUS IN ('待处理', '处理中')",
                    Integer.class));

            // 平台收入（按20%抽成计算）
            Double platformIncome = totalIncome != null ? totalIncome * 0.2 : 0.0;
            stats.put("platformIncome", platformIncome);
            stats.put("staffIncome", totalIncome != null ? totalIncome * 0.8 : 0.0);

        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }
        return stats;
    }
}