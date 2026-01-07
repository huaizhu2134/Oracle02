-- 插入陪玩人员数据 (MySQL版本)
-- 使用存储过程生成数据
DELIMITER $$

CREATE PROCEDURE InsertStaffData()
BEGIN
  DECLARE i INT DEFAULT 1;
  
  WHILE i <= 100 DO
    INSERT INTO TB_STAFF (
      STAFF_NAME, REAL_NAME, GENDER, AGE, PHONE, EMAIL, 
      SKILL_LEVEL, SERVICE_TYPE, UNIT_PRICE, STATUS, TOTAL_ORDERS, 
      TOTAL_INCOME, AVG_SCORE, CERT_STATUS, CREATE_TIME
    ) VALUES (
      CONCAT('陪玩人员', i),
      CONCAT('真实姓名', i),
      CASE WHEN i % 2 = 0 THEN 'M' ELSE 'F' END,
      18 + (i % 30),
      CONCAT('138', LPAD(FLOOR(10000000 + (RAND() * 90000000)), 8, '0')),
      CONCAT('staff', i, '@example.com'),
      CASE i % 6
        WHEN 0 THEN '青铜'
        WHEN 1 THEN '白银'
        WHEN 2 THEN '黄金'
        WHEN 3 THEN '铂金'
        WHEN 4 THEN '钻石'
        ELSE '王者'
      END,
      CASE i % 4
        WHEN 0 THEN '单排,开黑'
        WHEN 1 THEN '双排,教学'
        WHEN 2 THEN '五排,语音'
        ELSE '陪练,视频'
      END,
      ROUND(30 + (RAND() * 70), 2),
      CASE i % 4
        WHEN 0 THEN '空闲'
        WHEN 1 THEN '忙碌'
        WHEN 2 THEN '离线'
        ELSE '封禁'
      END,
      FLOOR(RAND() * 200),
      ROUND(RAND() * 10000, 2),
      ROUND(3 + RAND() * 2, 2),
      CASE WHEN i % 3 != 0 THEN '已认证' ELSE '未认证' END,
      DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
    );
    SET i = i + 1;
  END WHILE;
  
END$$

DELIMITER ;

-- 调用存储过程插入数据
CALL InsertStaffData();

-- 删除存储过程
DROP PROCEDURE InsertStaffData;