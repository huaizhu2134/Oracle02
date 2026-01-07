-- Update finance related data for completed orders - MySQL version
-- Update order financial fields
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

-- Update staff's total income and order count (based on completed orders)
UPDATE TB_STAFF s
JOIN (
  SELECT 
    STAFF_ID,
    SUM(STAFF_INCOME) AS TOTAL_INCOME,
    COUNT(*) AS TOTAL_ORDERS
  FROM TB_ORDER 
  WHERE ORDER_STATUS = '已完成' AND IS_DELETED = 'N'
  GROUP BY STAFF_ID
) o ON s.STAFF_ID = o.STAFF_ID
SET 
  s.TOTAL_INCOME = o.TOTAL_INCOME,
  s.TOTAL_ORDERS = o.TOTAL_ORDERS;

-- Update some order statuses to completed to ensure financial data exists
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

-- Update staff's average rating (based on evaluation table)
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

-- Output a confirmation message
SELECT 'Finance data update completed' AS RESULT;