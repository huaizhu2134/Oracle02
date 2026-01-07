-- 插入评价数据 (MySQL版本)
DELIMITER $$

CREATE PROCEDURE InsertEvaluationData()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE v_order_id BIGINT;
  DECLARE v_customer_id INT;
  DECLARE v_staff_id INT;
  DECLARE v_score INT;
  DECLARE v_content VARCHAR(500);
  DECLARE v_tags VARCHAR(200);
  DECLARE v_create_time DATETIME;
  
  WHILE i <= 400 DO
    -- 选择已完成的订单
    SET v_order_id = (
      SELECT ORDER_ID 
      FROM TB_ORDER 
      WHERE ORDER_STATUS = '已完成'
      ORDER BY RAND() 
      LIMIT 1
    );
    
    IF v_order_id IS NULL THEN
      -- 如果没有已完成的订单，就从所有订单中选一个
      SET v_order_id = (
        SELECT ORDER_ID 
        FROM TB_ORDER 
        ORDER BY RAND() 
        LIMIT 1
      );
      
      IF v_order_id IS NULL THEN
        -- 如果没有订单，使用默认值
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
    ELSE
      -- 获取订单的客户和员工ID
      SELECT CUSTOMER_ID, STAFF_ID 
      INTO v_customer_id, v_staff_id
      FROM TB_ORDER 
      WHERE ORDER_ID = v_order_id;
    END IF;
    
    SET v_score = 3 + FLOOR(RAND() * 3); -- 3-5分
    SET v_content = CASE i % 10
      WHEN 0 THEN '服务态度很好，技术也很棒！'
      WHEN 1 THEN '还不错，下次还会约'
      WHEN 2 THEN '技术很厉害，胜率高'
      WHEN 3 THEN '沟通顺畅，配合默契'
      WHEN 4 THEN '价格合理，服务周到'
      WHEN 5 THEN '响应速度快，很专业'
      WHEN 6 THEN '很有耐心，适合新手'
      WHEN 7 THEN '技术不错，态度也很好'
      WHEN 8 THEN '性价比很高，值得推荐'
      ELSE '非常满意，会再次选择'
    END;
    SET v_tags = CASE i % 5
      WHEN 0 THEN '技术好,态度好'
      WHEN 1 THEN '准时,耐心'
      WHEN 2 THEN '专业,配合度高'
      WHEN 3 THEN '声音好听,经验丰富'
      ELSE '颜值高,沟通顺畅'
    END;
    SET v_create_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY);
    
    INSERT INTO TB_EVALUATION (
      ORDER_ID, CUSTOMER_ID, STAFF_ID, SCORE, 
      CONTENT, TAGS, CREATE_TIME
    ) VALUES (
      v_order_id,
      v_customer_id,
      v_staff_id,
      v_score,
      v_content,
      v_tags,
      v_create_time
    );
    
    SET i = i + 1;
  END WHILE;
  
END$$

DELIMITER ;

-- 调用存储过程插入数据
CALL InsertEvaluationData();

-- 删除存储过程
DROP PROCEDURE InsertEvaluationData;