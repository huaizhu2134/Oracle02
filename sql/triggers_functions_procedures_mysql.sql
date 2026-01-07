-- 游戏陪玩管理系统 - MySQL触发器、函数和存储过程定义脚本
-- 注意：执行此脚本前，请确保已执行 create_tables_mysql.sql 脚本以创建必要的表

-- 1. 创建触发器 - 自动更新订单时间戳
DELIMITER $$

DROP TRIGGER IF EXISTS TRG_ORDER_UPDATE_TIME$$
CREATE TRIGGER TRG_ORDER_UPDATE_TIME
    BEFORE UPDATE ON TB_ORDER
    FOR EACH ROW
BEGIN
    SET NEW.UPDATE_TIME = NOW();
END$$

-- 2. 创建触发器 - 自动更新评价后更新陪玩人员评分
DROP TRIGGER IF EXISTS TRG_STAFF_SCORE_UPDATE$$
CREATE TRIGGER TRG_STAFF_SCORE_UPDATE
    AFTER INSERT ON TB_EVALUATION
    FOR EACH ROW
BEGIN
    DECLARE v_avg_score DECIMAL(3,2);
    DECLARE v_total_orders INT;
    DECLARE v_staff_id INT;

    SET v_staff_id = NEW.STAFF_ID;

    -- 计算平均评分和评价数量
    SELECT AVG(SCORE), COUNT(*) 
    INTO v_avg_score, v_total_orders
    FROM TB_EVALUATION 
    WHERE STAFF_ID = v_staff_id 
    AND IS_DELETED = 'N';

    -- 更新陪玩人员表的评分和订单数
    UPDATE TB_STAFF 
    SET AVG_SCORE = COALESCE(v_avg_score, 0),
        TOTAL_ORDERS = COALESCE(v_total_orders, 0),
        UPDATE_TIME = NOW()
    WHERE STAFF_ID = v_staff_id;
END$$

-- 为评价表的更新和删除操作创建触发器
DROP TRIGGER IF EXISTS TRG_STAFF_SCORE_UPDATE_UPDATE$$
CREATE TRIGGER TRG_STAFF_SCORE_UPDATE_UPDATE
    AFTER UPDATE ON TB_EVALUATION
    FOR EACH ROW
BEGIN
    DECLARE v_avg_score DECIMAL(3,2);
    DECLARE v_total_orders INT;
    DECLARE v_staff_id INT;

    SET v_staff_id = NEW.STAFF_ID;

    -- 计算平均评分和评价数量
    SELECT AVG(SCORE), COUNT(*) 
    INTO v_avg_score, v_total_orders
    FROM TB_EVALUATION 
    WHERE STAFF_ID = v_staff_id 
    AND IS_DELETED = 'N';

    -- 更新陪玩人员表的评分和订单数
    UPDATE TB_STAFF 
    SET AVG_SCORE = COALESCE(v_avg_score, 0),
        TOTAL_ORDERS = COALESCE(v_total_orders, 0),
        UPDATE_TIME = NOW()
    WHERE STAFF_ID = v_staff_id;
END$$

DROP TRIGGER IF EXISTS TRG_STAFF_SCORE_UPDATE_DELETE$$
CREATE TRIGGER TRG_STAFF_SCORE_UPDATE_DELETE
    AFTER DELETE ON TB_EVALUATION
    FOR EACH ROW
BEGIN
    DECLARE v_avg_score DECIMAL(3,2);
    DECLARE v_total_orders INT;
    DECLARE v_staff_id INT;

    SET v_staff_id = OLD.STAFF_ID;

    -- 计算平均评分和评价数量
    SELECT AVG(SCORE), COUNT(*) 
    INTO v_avg_score, v_total_orders
    FROM TB_EVALUATION 
    WHERE STAFF_ID = v_staff_id 
    AND IS_DELETED = 'N';

    -- 更新陪玩人员表的评分和订单数
    UPDATE TB_STAFF 
    SET AVG_SCORE = COALESCE(v_avg_score, 0),
        TOTAL_ORDERS = COALESCE(v_total_orders, 0),
        UPDATE_TIME = NOW()
    WHERE STAFF_ID = v_staff_id;
END$$

-- 3. 创建函数 - 计算订单实际收入（扣除平台佣金）
DROP FUNCTION IF EXISTS FN_CALCULATE_ACTUAL_INCOME$$
CREATE FUNCTION FN_CALCULATE_ACTUAL_INCOME(
    p_total_amount DECIMAL(10,2),
    p_commission_rate DECIMAL(5,4)
)
RETURNS DECIMAL(10,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_actual_income DECIMAL(10,2);
    
    -- 验证输入参数
    IF p_total_amount IS NULL OR p_total_amount < 0 THEN
        RETURN 0.00;
    END IF;
    
    -- 设置默认佣金率
    IF p_commission_rate IS NULL THEN
        SET p_commission_rate = 0.10;
    END IF;
    
    -- 计算实际收入 = 总金额 - 平台佣金
    SET v_actual_income = p_total_amount - (p_total_amount * p_commission_rate);
    
    RETURN v_actual_income;
END$$

-- 4. 创建函数 - 获取陪玩人员服务类型统计
DROP FUNCTION IF EXISTS FN_GET_STAFF_SERVICE_STATS$$
CREATE FUNCTION FN_GET_STAFF_SERVICE_STATS(
    p_staff_id INT
)
RETURNS TEXT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_service_types TEXT;
    
    -- 验证输入参数
    IF p_staff_id IS NULL THEN
        RETURN '参数错误：陪玩人员ID不能为空';
    END IF;
    
    -- 统计该陪玩人员服务过的游戏类型
    SELECT GROUP_CONCAT(DISTINCT GAME_TYPE ORDER BY GAME_TYPE SEPARATOR ', ')
    INTO v_service_types
    FROM TB_ORDER
    WHERE STAFF_ID = p_staff_id
    AND ORDER_STATUS = '已完成'
    AND IS_DELETED = 'N';
    
    IF v_service_types IS NULL THEN
        SET v_service_types = '暂无服务记录';
    END IF;
    
    RETURN v_service_types;
END$$

-- 5. 创建函数 - 计算用户等级
DROP FUNCTION IF EXISTS FN_CALCULATE_MEMBER_LEVEL$$
CREATE FUNCTION FN_CALCULATE_MEMBER_LEVEL(
    p_total_consume DECIMAL(10,2)
)
RETURNS VARCHAR(20)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_member_level VARCHAR(20);
    
    -- 根据消费总额计算会员等级
    IF p_total_consume >= 5000 THEN
        SET v_member_level = '钻石会员';
    ELSEIF p_total_consume >= 2000 THEN
        SET v_member_level = '黄金会员';
    ELSEIF p_total_consume >= 500 THEN
        SET v_member_level = '白银会员';
    ELSE
        SET v_member_level = '普通会员';
    END IF;
    
    RETURN v_member_level;
END$$

-- 6. 创建存储过程 - 批量更新订单状态
DROP PROCEDURE IF EXISTS SP_UPDATE_EXPIRED_ORDERS$$
CREATE PROCEDURE SP_UPDATE_EXPIRED_ORDERS()
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- 更新超过24小时未支付的订单为已取消
    UPDATE TB_ORDER
    SET ORDER_STATUS = '已取消',
        UPDATE_TIME = NOW()
    WHERE ORDER_STATUS = '待支付'
    AND CREATE_TIME < DATE_SUB(NOW(), INTERVAL 1 DAY); -- 24小时前
    
    SET v_count = ROW_COUNT();
    
    -- 记录操作日志（仅在有更新时记录）
    IF v_count > 0 THEN
        INSERT INTO TB_OPERATION_LOG (
            ADMIN_ID, 
            ADMIN_NAME, 
            OPERATION_TYPE, 
            OPERATION_DESC, 
            REQUEST_URL, 
            REQUEST_METHOD, 
            REQUEST_IP, 
            REQUEST_PARAMS, 
            RESPONSE_RESULT, 
            EXECUTE_TIME, 
            CREATE_TIME, 
            IS_DELETED
        ) VALUES (
            0,
            'SYSTEM',
            'BATCH_UPDATE_ORDERS',
            CONCAT('自动取消 ', v_count, ' 个超时未支付订单'),
            '/order/batch-update',
            'POST',
            'SYSTEM',
            '{"type":"expired_orders","status":"cancelled"}',
            CONCAT('{"count":', v_count, '}'),
            50,
            NOW(),
            'N'
        );
    END IF;
    
    COMMIT;
    
    SELECT CONCAT('成功更新 ', v_count, ' 个超时订单为已取消状态') AS message;
END$$

-- 7. 创建存储过程 - 计算并更新陪玩人员总收入
DROP PROCEDURE IF EXISTS SP_UPDATE_STAFF_INCOME$$
CREATE PROCEDURE SP_UPDATE_STAFF_INCOME()
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- 更新陪玩人员的总收入
    UPDATE TB_STAFF s
    JOIN (
        SELECT 
            o.STAFF_ID,
            SUM(o.STAFF_INCOME) AS total_income
        FROM TB_ORDER o
        WHERE o.ORDER_STATUS = '已完成'
        AND o.IS_DELETED = 'N'
        GROUP BY o.STAFF_ID
    ) AS income_data ON s.STAFF_ID = income_data.STAFF_ID
    SET s.TOTAL_INCOME = COALESCE(income_data.total_income, 0),
        s.UPDATE_TIME = NOW();
    
    SET v_count = ROW_COUNT();
    
    -- 记录操作日志
    INSERT INTO TB_OPERATION_LOG (
        ADMIN_ID, 
        ADMIN_NAME, 
        OPERATION_TYPE, 
        OPERATION_DESC, 
        REQUEST_URL, 
        REQUEST_METHOD, 
        REQUEST_IP, 
        REQUEST_PARAMS, 
        RESPONSE_RESULT, 
        EXECUTE_TIME, 
        CREATE_TIME, 
        IS_DELETED
    ) VALUES (
        0,
        'SYSTEM',
        'UPDATE_STAFF_INCOME',
        CONCAT('更新 ', v_count, ' 个陪玩人员的总收入'),
        '/staff/update-income',
        'POST',
        'SYSTEM',
        CONCAT('{"type":"staff_income_update","count":', v_count, '}'),
        '{"success":true}',
        100,
        NOW(),
        'N'
    );
    
    COMMIT;
    
    SELECT CONCAT('成功更新 ', v_count, ' 个陪玩人员的总收入') AS message;
END$$

-- 8. 创建存储过程 - 生成月度统计报告
DROP PROCEDURE IF EXISTS SP_GENERATE_MONTHLY_REPORT$$
CREATE PROCEDURE SP_GENERATE_MONTHLY_REPORT(
    IN p_year INT,
    IN p_month INT,
    OUT p_total_orders INT,
    OUT p_total_income DECIMAL(10,2),
    OUT p_avg_order_amount DECIMAL(10,2)
)
BEGIN
    -- 验证输入参数
    IF p_year IS NULL OR p_month IS NULL OR p_year < 1900 OR p_month < 1 OR p_month > 12 THEN
        SET p_total_orders := 0;
        SET p_total_income := 0.00;
        SET p_avg_order_amount := 0.00;
        RETURN;
    END IF;
    
    -- 计算指定月份的订单统计
    SELECT 
        COUNT(*),
        COALESCE(SUM(TOTAL_AMOUNT), 0),
        COALESCE(AVG(TOTAL_AMOUNT), 0.00)
    INTO 
        p_total_orders,
        p_total_income,
        p_avg_order_amount
    FROM TB_ORDER
    WHERE ORDER_STATUS IN ('已完成', '已支付', '服务中')
    AND YEAR(CREATE_TIME) = p_year
    AND MONTH(CREATE_TIME) = p_month
    AND IS_DELETED = 'N';
    
    -- 处理可能未设置变量的情况
    IF p_total_orders IS NULL THEN
        SET p_total_orders := 0;
        SET p_total_income := 0.00;
        SET p_avg_order_amount := 0.00;
    END IF;
END$$

-- 9. 创建触发器 - 自动更新客户等级
DROP TRIGGER IF EXISTS TRG_CUSTOMER_LEVEL_UPDATE$$
CREATE TRIGGER TRG_CUSTOMER_LEVEL_UPDATE
    AFTER UPDATE ON TB_CUSTOMER
    FOR EACH ROW
BEGIN
    -- 只有当消费总额发生变化时才更新等级
    IF OLD.TOTAL_CONSUME != NEW.TOTAL_CONSUME THEN
        -- 根据消费总额自动更新会员等级
        UPDATE TB_CUSTOMER
        SET MEMBER_LEVEL = FN_CALCULATE_MEMBER_LEVEL(NEW.TOTAL_CONSUME),
            UPDATE_TIME = NOW()
        WHERE CUSTOMER_ID = NEW.CUSTOMER_ID;
    END IF;
END$$

DELIMITER ;

-- 显示创建的触发器
SELECT '触发器创建状态:' AS status;
SELECT TRIGGER_NAME, EVENT_MANIPULATION, ACTION_TIMING 
FROM INFORMATION_SCHEMA.TRIGGERS 
WHERE TRIGGER_SCHEMA = DATABASE()
AND TRIGGER_NAME IN ('TRG_ORDER_UPDATE_TIME', 'TRG_STAFF_SCORE_UPDATE', 'TRG_STAFF_SCORE_UPDATE_UPDATE', 'TRG_STAFF_SCORE_UPDATE_DELETE', 'TRG_CUSTOMER_LEVEL_UPDATE');

-- 显示创建的函数
SELECT '函数创建状态:' AS status;
SELECT ROUTINE_NAME, ROUTINE_TYPE 
FROM INFORMATION_SCHEMA.ROUTINES 
WHERE ROUTINE_SCHEMA = DATABASE()
AND ROUTINE_TYPE = 'FUNCTION'
AND ROUTINE_NAME IN ('FN_CALCULATE_ACTUAL_INCOME', 'FN_GET_STAFF_SERVICE_STATS', 'FN_CALCULATE_MEMBER_LEVEL');

-- 显示创建的存储过程
SELECT '存储过程创建状态:' AS status;
SELECT ROUTINE_NAME, ROUTINE_TYPE 
FROM INFORMATION_SCHEMA.ROUTINES 
WHERE ROUTINE_SCHEMA = DATABASE()
AND ROUTINE_TYPE = 'PROCEDURE'
AND ROUTINE_NAME IN ('SP_UPDATE_EXPIRED_ORDERS', 'SP_UPDATE_STAFF_INCOME', 'SP_GENERATE_MONTHLY_REPORT');