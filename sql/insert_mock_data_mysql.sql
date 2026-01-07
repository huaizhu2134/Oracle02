-- 插入所有测试数据的主脚本 - MySQL版本
-- 按依赖关系顺序执行

DELIMITER $$

-- 首先插入员工数据（如果尚未存在）
DROP PROCEDURE IF EXISTS InsertMockStaffData$$
CREATE PROCEDURE InsertMockStaffData()
BEGIN
  DECLARE staff_count INT DEFAULT 0;
  DECLARE i INT DEFAULT 1;
  
  SELECT COUNT(*) INTO staff_count FROM TB_STAFF;
  
  IF staff_count = 0 THEN
    WHILE i <= 100 DO
      INSERT INTO TB_STAFF (
        STAFF_NAME, REAL_NAME, GENDER, AGE, PHONE, EMAIL, 
        AVATAR_URL, SKILL_LEVEL, SERVICE_TYPE, UNIT_PRICE, STATUS, 
        TOTAL_ORDERS, TOTAL_INCOME, AVG_SCORE, CERT_STATUS, 
        CREATE_TIME, UPDATE_TIME, LAST_LOGIN_TIME, IS_DELETED
      ) VALUES (
        CONCAT('陪玩', i),
        CONCAT('真实姓名', i),
        CASE WHEN i % 2 = 0 THEN 'M' ELSE 'F' END,
        18 + (i % 30),
        CONCAT('138', LPAD(FLOOR(10000000 + (RAND() * 90000000)), 8, '0')),
        CONCAT('staff', i, '@example.com'),
        CONCAT('http://example.com/avatar/', (10000 + i), '.jpg'),
        CASE i % 6
          WHEN 0 THEN '青铜'
          WHEN 1 THEN '白银'
          WHEN 2 THEN '黄金'
          WHEN 3 THEN '铂金'
          WHEN 4 THEN '钻石'
          ELSE '王者'
        END,
        CASE i % 5
          WHEN 0 THEN '单排'
          WHEN 1 THEN '双排'
          WHEN 2 THEN '三排'
          WHEN 3 THEN '五排'
          ELSE '自由排位'
        END,
        ROUND(30 + (RAND() * 70), 2),
        CASE WHEN i % 10 = 0 THEN '忙碌' 
             WHEN i % 10 = 1 THEN '离线' 
             WHEN i % 10 = 2 THEN '封禁' 
             ELSE '空闲' END,
        0,  -- TOTAL_ORDERS
        0,  -- TOTAL_INCOME
        5.00,  -- AVG_SCORE
        CASE WHEN i % 4 = 0 THEN '已认证' ELSE '未认证' END,
        DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY),
        NOW(),
        DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 24) HOUR),
        'N'
      );
      SET i = i + 1;
    END WHILE;
  END IF;
END$$

-- 然后插入客户数据（如果尚未存在）
DROP PROCEDURE IF EXISTS InsertMockCustomerData$$
CREATE PROCEDURE InsertMockCustomerData()
BEGIN
  DECLARE customer_count INT DEFAULT 0;
  DECLARE i INT DEFAULT 1;
  
  SELECT COUNT(*) INTO customer_count FROM TB_CUSTOMER;
  
  IF customer_count = 0 THEN
    WHILE i <= 200 DO
      INSERT INTO TB_CUSTOMER (
        USERNAME, PASSWORD, NICKNAME, PHONE, EMAIL, 
        GENDER, AGE, MEMBER_LEVEL, TOTAL_CONSUME, ORDER_COUNT, 
        BALANCE, STATUS, CREATE_TIME, IS_DELETED
      ) VALUES (
        CONCAT('customer', i),
        'e10adc3949ba59abbe56e057f20f883e', -- MD5加密的"123456"
        CONCAT('客户', i),
        CONCAT('139', LPAD(FLOOR(10000000 + (RAND() * 90000000)), 8, '0')),
        CONCAT('customer', i, '@example.com'),
        CASE WHEN i % 2 = 0 THEN 'M' ELSE 'F' END,
        16 + (i % 35),
        CASE i % 3
          WHEN 0 THEN '普通会员'
          WHEN 1 THEN 'VIP会员'
          ELSE 'SVIP会员'
        END,
        ROUND(RAND() * 5000, 2),
        FLOOR(RAND() * 50),
        ROUND(RAND() * 1000, 2),
        CASE WHEN i % 10 = 0 THEN '冻结' ELSE '正常' END,
        DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
        'N'
      );
      SET i = i + 1;
    END WHILE;
  END IF;
END$$

-- 插入订单数据（如果尚未存在）
DROP PROCEDURE IF EXISTS InsertMockOrderData$$
CREATE PROCEDURE InsertMockOrderData()
BEGIN
  DECLARE order_count INT DEFAULT 0;
  DECLARE i INT DEFAULT 1;
  DECLARE v_customer_id INT;
  DECLARE v_staff_id INT;
  DECLARE v_game_type VARCHAR(50);
  DECLARE v_service_hours INT;
  DECLARE v_unit_price DECIMAL(10,2);
  DECLARE v_total_amount DECIMAL(10,2);
  DECLARE v_order_status VARCHAR(20);
  DECLARE v_pay_time DATETIME;
  DECLARE v_create_time DATETIME;
  DECLARE v_platform_commission DECIMAL(10,2);
  DECLARE v_staff_income DECIMAL(10,2);
  
  SELECT COUNT(*) INTO order_count FROM TB_ORDER;
  
  IF order_count = 0 THEN
    WHILE i <= 500 DO
      -- 从现有的客户和陪玩人员中选择
      SET v_customer_id = (
        SELECT CUSTOMER_ID 
        FROM TB_CUSTOMER 
        ORDER BY RAND() 
        LIMIT 1
      );
      
      SET v_staff_id = (
        SELECT STAFF_ID 
        FROM TB_STAFF 
        ORDER BY RAND() 
        LIMIT 1
      );
      
      -- 如果没有找到客户或员工，则使用默认值
      IF v_customer_id IS NULL THEN
        SET v_customer_id = 1;
      END IF;
      
      IF v_staff_id IS NULL THEN
        SET v_staff_id = 1;
      END IF;
      
      SET v_game_type = CASE i % 10
        WHEN 0 THEN '英雄联盟'
        WHEN 1 THEN '王者荣耀'
        WHEN 2 THEN '和平精英'
        WHEN 3 THEN '绝地求生'
        WHEN 4 THEN '原神'
        WHEN 5 THEN 'DNF'
        WHEN 6 THEN 'CS2'
        WHEN 7 THEN 'Apex英雄'
        WHEN 8 THEN '炉石传说'
        ELSE 'DOTA2'
      END;
      
      SET v_service_hours = 1 + (i % 8);
      SET v_unit_price = ROUND(30 + (RAND() * 70), 2);
      SET v_total_amount = v_unit_price * v_service_hours;
      SET v_order_status = CASE i % 5
        WHEN 0 THEN '待支付'
        WHEN 1 THEN '已支付'
        WHEN 2 THEN '服务中'
        WHEN 3 THEN '已完成'
        ELSE '已取消'
      END;
      
      SET v_create_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY); -- 近180天内
      
      IF v_order_status IN ('已支付', '服务中', '已完成') THEN
        SET v_pay_time = DATE_ADD(v_create_time, INTERVAL FLOOR(RAND() * 24) HOUR); -- 创建后0-24小时内支付
      ELSE
        SET v_pay_time = NULL;
      END IF;
      
      -- 计算平台抽成和员工收入（假设平台抽成20%，员工获得80%）
      IF v_order_status = '已完成' THEN
        SET v_platform_commission = v_total_amount * 0.2;
        SET v_staff_income = v_total_amount * 0.8;
      ELSE
        SET v_platform_commission = 0;
        SET v_staff_income = 0;
      END IF;
      
      INSERT INTO TB_ORDER (
        ORDER_NO, CUSTOMER_ID, STAFF_ID, GAME_TYPE, 
        SERVICE_HOURS, UNIT_PRICE, TOTAL_AMOUNT, ORDER_STATUS, 
        PAY_TIME, CREATE_TIME, PLATFORM_COMMISSION, STAFF_INCOME
      ) VALUES (
        CONCAT('PM', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(i, 6, '0')),
        v_customer_id,
        v_staff_id,
        v_game_type,
        v_service_hours,
        v_unit_price,
        v_total_amount,
        v_order_status,
        v_pay_time,
        v_create_time,
        v_platform_commission,
        v_staff_income
      );
      
      SET i = i + 1;
    END WHILE;
  END IF;
END$$

-- 插入投诉数据
DROP PROCEDURE IF EXISTS InsertMockComplaintData$$
CREATE PROCEDURE InsertMockComplaintData()
BEGIN
  DECLARE complaint_count INT DEFAULT 0;
  DECLARE i INT DEFAULT 1;
  DECLARE v_order_id BIGINT;
  DECLARE v_customer_id INT;
  DECLARE v_staff_id INT;
  DECLARE v_complaint_type VARCHAR(50);
  DECLARE v_complaint_status VARCHAR(20);
  DECLARE v_complaint_content VARCHAR(1000);
  DECLARE v_handler_id INT;
  
  SELECT COUNT(*) INTO complaint_count FROM TB_COMPLAINT;
  
  IF complaint_count = 0 THEN
    WHILE i <= 100 DO
      -- 随机选择一个订单
      SET v_order_id = (
        SELECT ORDER_ID 
        FROM TB_ORDER 
        WHERE ORDER_STATUS IN ('服务中', '已完成', '已取消') 
        ORDER BY RAND() 
        LIMIT 1
      );
      
      IF v_order_id IS NULL THEN
        -- 如果没有找到合适的订单，使用默认值
        SET v_order_id = 1;
        SET v_customer_id = 1;
        SET v_staff_id = 1;
      ELSE
        -- 获取订单的客户和员工ID
        SELECT CUSTOMER_ID, STAFF_ID 
        INTO v_customer_id, v_staff_id
        FROM TB_ORDER 
        WHERE ORDER_ID = v_order_id;
      END IF;
      
      -- 随机生成投诉类型
      SET v_complaint_type = CASE i % 4
        WHEN 0 THEN 'ATTITUDE'
        WHEN 1 THEN 'QUALITY'
        WHEN 2 THEN 'PRICE'
        ELSE 'OTHER'
      END;
      
      SET v_complaint_status = CASE i % 4
        WHEN 0 THEN '待处理'
        WHEN 1 THEN '处理中'
        WHEN 2 THEN '已解决'
        ELSE '已关闭'
      END;
      
      SET v_complaint_content = CONCAT(
        CASE i % 5
          WHEN 0 THEN '服务态度恶劣，对客户不礼貌'
          WHEN 1 THEN '服务质量差，没有达到预期'
          WHEN 2 THEN '收费不合理，存在乱收费现象'
          WHEN 3 THEN '技能水平不够，无法满足要求'
          ELSE '其他问题，具体情况需要进一步了解'
        END,
        '，订单编号：', i
      );
      
      -- 随机分配处理人（管理员ID）
      SET v_handler_id = 60001 + (i % 5); -- 假设有5个管理员
      
      INSERT INTO TB_COMPLAINT (
        ORDER_ID,
        CUSTOMER_ID,
        STAFF_ID,
        COMPLAINT_TYPE,
        COMPLAINT_CONTENT,
        EVIDENCE_URL,
        COMPLAINT_STATUS,
        HANDLER_ID,
        HANDLER_COMMENT,
        HANDLE_TIME,
        CREATE_TIME,
        UPDATE_TIME,
        IS_DELETED
      ) VALUES (
        v_order_id,
        v_customer_id,
        v_staff_id,
        v_complaint_type,
        v_complaint_content,
        CASE 
          WHEN i % 3 = 0 THEN NULL 
          ELSE CONCAT('http://example.com/evidence/', i, '.jpg') 
        END,
        v_complaint_status,
        CASE 
          WHEN v_complaint_status IN ('处理中', '已解决', '已关闭') THEN v_handler_id 
          ELSE NULL 
        END,
        CASE 
          WHEN v_complaint_status IN ('已解决', '已关闭') THEN 
            CONCAT('已处理，',
              CASE i % 3
                WHEN 0 THEN '经核实，投诉属实，已对相关人员进行处理'
                WHEN 1 THEN '经核实，投诉不属实，为误解'
                ELSE '已协调解决，双方达成一致'
              END)
          ELSE NULL
        END,
        CASE 
          WHEN v_complaint_status IN ('已解决', '已关闭') THEN 
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY) -- 7天内处理
          ELSE NULL
        END,
        DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY), -- 15天内创建
        NOW(),
        'N'
      );
      
      SET i = i + 1;
    END WHILE;
    
    SELECT '成功插入100条投诉数据' AS message;
  END IF;
END$$

-- 插入财务管理相关数据
DROP PROCEDURE IF EXISTS UpdateFinanceData$$
CREATE PROCEDURE UpdateFinanceData()
BEGIN
  -- 更新已完成订单的财务字段
  UPDATE TB_ORDER 
  SET 
    PLATFORM_COMMISSION = CASE 
      WHEN ORDER_STATUS = '已完成' THEN TOTAL_AMOUNT * 0.2 
      ELSE 0 
    END,
    STAFF_INCOME = CASE 
      WHEN ORDER_STATUS = '已完成' THEN TOTAL_AMOUNT * 0.8 
      ELSE 0 
    END
  WHERE IS_DELETED = 'N';
  
  -- 更新陪玩人员的总收入和订单数（基于已完成订单）
  UPDATE TB_STAFF s
  JOIN (
    SELECT 
      STAFF_ID,
      SUM(STAFF_INCOME) AS total_income,
      COUNT(*) AS total_orders
    FROM TB_ORDER 
    WHERE ORDER_STATUS = '已完成' 
      AND IS_DELETED = 'N'
    GROUP BY STAFF_ID
  ) o ON s.STAFF_ID = o.STAFF_ID
  SET 
    s.TOTAL_INCOME = o.total_income,
    s.TOTAL_ORDERS = o.total_orders,
    -- 设置一个基于收入的余额（模拟可提现金额）
    s.BALANCE = CASE 
      WHEN o.total_income > 0 THEN o.total_income * 0.7  -- 假设70%可提现
      ELSE s.BALANCE 
    END;
  
  SELECT '财务相关数据更新完成' AS message;
  
  -- 为测试财务功能，更新一些订单的状态为已完成
  UPDATE TB_ORDER 
  SET 
    ORDER_STATUS = CASE 
      WHEN ORDER_ID % 3 = 0 THEN '已完成'
      WHEN ORDER_ID % 3 = 1 THEN '已支付'
      ELSE ORDER_STATUS
    END,
    PAY_TIME = CASE 
      WHEN ORDER_ID % 3 IN (0, 1) AND PAY_TIME IS NULL THEN DATE_ADD(CREATE_TIME, INTERVAL 4.8 HOUR) -- 0.2天约等于4.8小时
      ELSE PAY_TIME
    END
  WHERE IS_DELETED = 'N' AND ORDER_STATUS IN ('待支付', '已支付', '服务中');
  
  -- 更新陪玩人员的平均评分（基于评价表）
  UPDATE TB_STAFF s
  JOIN (
    SELECT 
      e.STAFF_ID,
      AVG(e.SCORE) as AVG_SCORE
    FROM TB_EVALUATION e
    JOIN TB_ORDER o ON e.ORDER_ID = o.ORDER_ID
    WHERE o.ORDER_STATUS = '已完成'
    GROUP BY e.STAFF_ID
  ) e ON s.STAFF_ID = e.STAFF_ID
  SET s.AVG_SCORE = ROUND(e.AVG_SCORE, 2);
END$$

-- 最后插入评价数据（如果尚未存在）
DROP PROCEDURE IF EXISTS InsertMockEvaluationData$$
CREATE PROCEDURE InsertMockEvaluationData()
BEGIN
  DECLARE evaluation_count INT DEFAULT 0;
  
  SELECT COUNT(*) INTO evaluation_count FROM TB_EVALUATION;
  
  IF evaluation_count = 0 THEN
    -- 为已完成的订单创建评价
    INSERT INTO TB_EVALUATION (
      ORDER_ID,
      CUSTOMER_ID,
      STAFF_ID,
      SCORE,
      CONTENT,
      TAGS,
      IS_ANONYMOUS,
      CREATE_TIME,
      IS_DELETED
    )
    SELECT 
      o.ORDER_ID,
      o.CUSTOMER_ID,
      o.STAFF_ID,
      ROUND(3 + (RAND() * 2)), -- 评分3-5分
      CASE (o.ORDER_ID % 5)
        WHEN 0 THEN '服务很好，非常满意'
        WHEN 1 THEN '服务不错，下次还会选择'
        WHEN 2 THEN '服务一般，还可以'
        WHEN 3 THEN '服务有待提升'
        ELSE '基本满意，略有不足'
      END,
      CASE (o.ORDER_ID % 4)
        WHEN 0 THEN '响应快,技术好,服务佳'
        WHEN 1 THEN '技术好,耐心好'
        WHEN 2 THEN '服务佳,响应快'
        ELSE '技术好'
      END,
      CASE WHEN o.ORDER_ID % 10 = 0 THEN 'Y' ELSE 'N' END,
      DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY), -- 30天内
      'N'
    FROM TB_ORDER o
    WHERE o.ORDER_STATUS = '已完成' AND o.IS_DELETED = 'N'
    ORDER BY RAND()
    LIMIT 300; -- 最多300条评价
    
    SELECT '成功插入评价数据' AS message;
  END IF;
END$$

DELIMITER ;

-- 调用所有存储过程来插入数据
CALL InsertMockStaffData();
CALL InsertMockCustomerData();
CALL InsertMockOrderData();
CALL InsertMockComplaintData();
CALL UpdateFinanceData();
CALL InsertMockEvaluationData();

-- 删除存储过程
DROP PROCEDURE InsertMockStaffData;
DROP PROCEDURE InsertMockCustomerData;
DROP PROCEDURE InsertMockOrderData;
DROP PROCEDURE InsertMockComplaintData;
DROP PROCEDURE UpdateFinanceData;
DROP PROCEDURE InsertMockEvaluationData;

SELECT 'MySQL版本测试数据插入完成' AS RESULT;