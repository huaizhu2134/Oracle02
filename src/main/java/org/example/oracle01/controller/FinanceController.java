package org.example.oracle01.controller;

import org.example.oracle01.util.Result;
import org.example.oracle01.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 获取财务统计信息
    @GetMapping("/summary")
    public Result<Map<String, Object>> getFinanceSummary() {
        try {
            Map<String, Object> summary = new HashMap<>();

            // 今日订单数
            String todayOrdersSql = "SELECT COUNT(*) FROM TB_ORDER WHERE DATE(CREATE_TIME) = CURDATE()";
            Integer todayOrders = jdbcTemplate.queryForObject(todayOrdersSql, Integer.class);
            summary.put("todayOrders", todayOrders != null ? todayOrders : 0);

            // 今日收入
            String todayIncomeSql = "SELECT IFNULL(SUM(TOTAL_AMOUNT), 0) FROM TB_ORDER WHERE DATE(CREATE_TIME) = CURDATE() AND ORDER_STATUS = '已完成'";
            BigDecimal todayIncome = jdbcTemplate.queryForObject(todayIncomeSql, BigDecimal.class);
            summary.put("todayIncome", todayIncome != null ? todayIncome : BigDecimal.ZERO);

            // 平台总收入（平台抽成20%）
            String platformIncomeSql = "SELECT IFNULL(SUM(TOTAL_AMOUNT * 0.2), 0) FROM TB_ORDER WHERE ORDER_STATUS = '已完成'";
            BigDecimal platformIncome = jdbcTemplate.queryForObject(platformIncomeSql, BigDecimal.class);
            summary.put("platformIncome", platformIncome != null ? platformIncome : BigDecimal.ZERO);

            // 陪玩总收入（陪玩分成80%）
            String staffIncomeSql = "SELECT IFNULL(SUM(TOTAL_AMOUNT * 0.8), 0) FROM TB_ORDER WHERE ORDER_STATUS = '已完成'";
            BigDecimal staffIncome = jdbcTemplate.queryForObject(staffIncomeSql, BigDecimal.class);
            summary.put("staffIncome", staffIncome != null ? staffIncome : BigDecimal.ZERO);

            return Result.success(summary);
        } catch (Exception e) {
            return Result.error("获取财务统计失败：" + e.getMessage());
        }
    }

    // 获取提现列表
    @GetMapping("/withdraw/list")
    public Result<PageResult<Map<String, Object>>> getWithdrawList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "staffId", required = false) String staffId,
            @RequestParam(value = "status", required = false) String status) {
        try {
            List<Map<String, Object>> withdrawList = new ArrayList<>();

            // 构建查询SQL
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT w.WITHDRAW_ID, w.STAFF_ID, s.STAFF_NAME, w.AMOUNT, w.STATUS, ");
            sql.append("w.APPLY_TIME, w.APPROVE_TIME, w.APPROVER, w.REMARK ");
            sql.append("FROM TB_WITHDRAW w ");
            sql.append("LEFT JOIN TB_STAFF s ON w.STAFF_ID = s.STAFF_ID ");
            sql.append("WHERE w.IS_DELETED = 'N' ");

            List<Object> queryParams = new ArrayList<>();

            if (staffId != null && !staffId.isEmpty()) {
                sql.append("AND w.STAFF_ID = ? ");
                queryParams.add(Long.valueOf(staffId));
            }
            if (status != null && !status.isEmpty()) {
                sql.append("AND w.STATUS = ? ");
                queryParams.add(status);
            }

            sql.append("ORDER BY w.APPLY_TIME DESC LIMIT ? OFFSET ?");
            queryParams.add(size);
            queryParams.add((page - 1) * size);

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql.toString(), queryParams.toArray());

            for (Map<String, Object> row : result) {
                Map<String, Object> withdraw = new HashMap<>();
                withdraw.put("withdrawId", row.get("WITHDRAW_ID"));
                withdraw.put("staffId", row.get("STAFF_ID"));
                withdraw.put("staffName", row.get("STAFF_NAME"));
                withdraw.put("amount", row.get("AMOUNT"));
                withdraw.put("status", row.get("STATUS"));
                withdraw.put("applyTime", row.get("APPLY_TIME"));
                withdraw.put("approveTime", row.get("APPROVE_TIME"));
                withdraw.put("approver", row.get("APPROVER"));
                withdraw.put("remark", row.get("REMARK"));
                withdrawList.add(withdraw);
            }

            // 获取总数
            StringBuilder countSql = new StringBuilder();
            countSql.append("SELECT COUNT(*) FROM TB_WITHDRAW w WHERE w.IS_DELETED = 'N'");

            List<Object> countParams = new ArrayList<>();

            if (staffId != null && !staffId.isEmpty()) {
                countSql.append(" AND w.STAFF_ID = ? ");
                countParams.add(Long.valueOf(staffId));
            }
            if (status != null && !status.isEmpty()) {
                countSql.append(" AND w.STATUS = ? ");
                countParams.add(status);
            }

            Integer total = jdbcTemplate.queryForObject(countSql.toString(), Integer.class, countParams.toArray());

            PageResult<Map<String, Object>> pageResult = new PageResult<>(
                    total != null ? total.longValue() : 0L,
                    withdrawList,
                    page,
                    size);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("获取提现列表失败：" + e.getMessage());
        }
    }

    // 申请提现
    @PostMapping("/withdraw")
    public Result<String> applyWithdraw(@RequestBody Map<String, Object> params) {
        try {
            Long staffId = Long.valueOf(params.get("staffId").toString());
            BigDecimal amount = new BigDecimal(params.get("amount").toString());
            String remark = params.get("remark") != null ? params.get("remark").toString() : "";

            // 检查陪玩人员余额是否充足
            String checkBalanceSql = "SELECT TOTAL_INCOME FROM TB_STAFF WHERE STAFF_ID = ? AND IS_DELETED = 'N'";
            BigDecimal balance = jdbcTemplate.queryForObject(checkBalanceSql, BigDecimal.class, staffId);

            if (balance == null || balance.compareTo(amount) < 0) {
                return Result.error("余额不足，无法申请提现");
            }

            // 插入提现申请记录
            String insertSql = "INSERT INTO TB_WITHDRAW (STAFF_ID, AMOUNT, REMARK) VALUES (?, ?, ?)";
            int result = jdbcTemplate.update(insertSql, staffId, amount, remark);

            if (result > 0) {
                return Result.success("提现申请已提交");
            } else {
                return Result.error("提现申请提交失败");
            }
        } catch (Exception e) {
            return Result.error("申请提现失败：" + e.getMessage());
        }
    }

    // 审核提现 - 通过
    @PostMapping("/withdraw/approve")
    public Result<String> approveWithdraw(@RequestBody Map<String, Object> params) {
        try {
            Long staffId = Long.valueOf(params.get("staffId").toString());
            String status = params.get("status") != null ? params.get("status").toString() : "已通过";

            String approver = params.get("approver") != null ? params.get("approver").toString() : "系统管理员";

            // 更新提现申请状态
            String updateWithdrawSql = "UPDATE TB_WITHDRAW SET STATUS = ?, APPROVE_TIME = NOW(), APPROVER = ? WHERE STAFF_ID = ? AND STATUS = '待审核'";
            int updated = jdbcTemplate.update(updateWithdrawSql, status, approver, staffId);

            if (updated > 0 && "已通过".equals(status)) {
                // 如果审核通过，从陪玩收入中扣除已批准的提现金额
                String updateStaffSql = "UPDATE TB_STAFF SET TOTAL_INCOME = TOTAL_INCOME - (SELECT COALESCE(SUM(AMOUNT), 0) FROM TB_WITHDRAW WHERE STAFF_ID = ? AND STATUS = '已通过'), UPDATE_TIME = NOW() WHERE STAFF_ID = ?";
                jdbcTemplate.update(updateStaffSql, staffId, staffId);
                return Result.success("审核通过，提现成功");
            } else if (updated > 0) {
                return Result.success("已拒绝提现申请");
            } else {
                return Result.error("未找到待审核的提现申请或操作失败");
            }
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    // 获取收入明细列表
    @GetMapping("/income/list")
    public Result<PageResult<Map<String, Object>>> getIncomeList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "staffId", required = false) String staffId,
            @RequestParam(value = "orderNo", required = false) String orderNo) {
        try {
            // 从已完成订单中查询收入明细
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT o.ORDER_ID, o.ORDER_NO, o.STAFF_ID, s.STAFF_NAME AS STAFF_NAME, ");
            sql.append("o.TOTAL_AMOUNT, o.TOTAL_AMOUNT * 0.8 AS STAFF_INCOME, ");
            sql.append("o.ORDER_STATUS, o.CREATE_TIME ");
            sql.append("FROM TB_ORDER o ");
            sql.append("LEFT JOIN TB_STAFF s ON o.STAFF_ID = s.STAFF_ID ");
            sql.append("WHERE o.IS_DELETED = 'N' AND o.ORDER_STATUS = '已完成' ");

            List<Object> queryParams = new ArrayList<>();
            if (staffId != null && !staffId.isEmpty()) {
                sql.append("AND o.STAFF_ID = ? ");
                queryParams.add(Long.valueOf(staffId));
            }
            if (orderNo != null && !orderNo.isEmpty()) {
                sql.append("AND o.ORDER_NO LIKE ? ");
                queryParams.add("%" + orderNo + "%");
            }

            sql.append("ORDER BY o.CREATE_TIME DESC ");
            sql.append("LIMIT ? OFFSET ?");
            queryParams.add(size);
            queryParams.add((page - 1) * size);

            List<Map<String, Object>> orderList = jdbcTemplate.queryForList(sql.toString(), queryParams.toArray());

            List<Map<String, Object>> incomeList = new ArrayList<>();
            int id = 1;
            BigDecimal runningBalance = new BigDecimal("10000"); // 模拟起始余额
            for (Map<String, Object> order : orderList) {
                Map<String, Object> income = new HashMap<>();
                income.put("incomeId", id++);
                income.put("staffId", order.get("STAFF_ID"));
                income.put("staffName", order.get("STAFF_NAME"));
                income.put("orderId", order.get("ORDER_NO"));
                income.put("incomeType", "订单收入");
                BigDecimal staffIncome = (BigDecimal) order.get("STAFF_INCOME");
                income.put("amount", staffIncome);
                runningBalance = runningBalance.add(staffIncome != null ? staffIncome : BigDecimal.ZERO);
                income.put("balanceAfter", runningBalance);
                income.put("createTime", order.get("CREATE_TIME"));
                income.put("remark", "订单完成收入");
                incomeList.add(income);
            }

            // 获取总数
            StringBuilder countSql = new StringBuilder();
            countSql.append("SELECT COUNT(*) FROM TB_ORDER o WHERE o.IS_DELETED = 'N' AND o.ORDER_STATUS = '已完成' ");
            List<Object> countParams = new ArrayList<>();
            if (staffId != null && !staffId.isEmpty()) {
                countSql.append("AND o.STAFF_ID = ? ");
                countParams.add(Long.valueOf(staffId));
            }
            if (orderNo != null && !orderNo.isEmpty()) {
                countSql.append("AND o.ORDER_NO LIKE ? ");
                countParams.add("%" + orderNo + "%");
            }

            Integer total = jdbcTemplate.queryForObject(countSql.toString(), Integer.class, countParams.toArray());

            PageResult<Map<String, Object>> pageResult = new PageResult<>(
                    total != null ? total.longValue() : 0L,
                    incomeList,
                    page,
                    size);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("获取收入明细失败：" + e.getMessage());
        }
    }
}
