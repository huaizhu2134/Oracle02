-- 插入投诉数据 (MySQL版本)
DELIMITER $$

CREATE PROCEDURE InsertComplaintData()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE v_order_id BIGINT;
  DECLARE v_customer_id INT;
  DECLARE v_staff_id INT;
  DECLARE v_complaint_type VARCHAR(50);
  DECLARE v_complaint_status VARCHAR(20);
  DECLARE v_complaint_content VARCHAR(1000);
  DECLARE v_handler_id INT;
  DECLARE v_max_complaint_id BIGINT;
  
  -- 获取当前投诉表中的最大ID，避免冲突
  SET v_max_complaint_id = (SELECT COALESCE(MAX(COMPLAINT_ID), 5000000000) FROM TB_COMPLAINT) + 1;
  
  WHILE i <= 50 DO
    -- 随机选择一个订单
    SET v_order_id = (
      SELECT ORDER_ID 
      FROM TB_ORDER 
      WHERE ORDER_STATUS IN ('服务中', '已完成', '已取消') 
      ORDER BY RAND() 
      LIMIT 1
    );
    
    IF v_order_id IS NULL THEN
      -- 如果没有合适的订单，使用默认值
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
    
    SET v_complaint_content = CONCAT('Complaint content for order ', v_max_complaint_id + i - 1);
    
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
        ELSE CONCAT('http://example.com/evidence/', v_max_complaint_id + i - 1, '.jpg') 
      END,
      v_complaint_status,
      CASE 
        WHEN v_complaint_status IN ('处理中', '已解决', '已关闭') THEN v_handler_id 
        ELSE NULL 
      END,
      CASE 
        WHEN v_complaint_status IN ('已解决', '已关闭') THEN 
          CONCAT('Handled complaint ', v_max_complaint_id + i - 1)
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
  
  SELECT CONCAT('Successfully inserted 50 complaint records starting from ID: ', v_max_complaint_id) AS message;
  
END$$

DELIMITER ;

-- 调用存储过程插入数据
CALL InsertComplaintData();

-- 删除存储过程
DROP PROCEDURE InsertComplaintData;