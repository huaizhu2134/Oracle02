-- 插入客户数据 (MySQL版本)
DELIMITER $$

CREATE PROCEDURE InsertCustomerData()
BEGIN
  DECLARE i INT DEFAULT 1;
  
  WHILE i <= 200 DO
    INSERT INTO TB_CUSTOMER (
      USERNAME, PASSWORD, NICKNAME, PHONE, EMAIL, 
      GENDER, AGE, MEMBER_LEVEL, TOTAL_CONSUME, ORDER_COUNT, 
      BALANCE, STATUS, CREATE_TIME
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
      DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
    );
    SET i = i + 1;
  END WHILE;
  
END$$

DELIMITER ;

-- 调用存储过程插入数据
CALL InsertCustomerData();

-- 删除存储过程
DROP PROCEDURE InsertCustomerData;