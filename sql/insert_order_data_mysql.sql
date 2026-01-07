-- 插入订单数据 (MySQL版本)
DELIMITER $$

CREATE PROCEDURE InsertOrderData()
BEGIN
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
  
END$$

DELIMITER ;

-- 调用存储过程插入数据
CALL InsertOrderData();

-- 删除存储过程
DROP PROCEDURE InsertOrderData;