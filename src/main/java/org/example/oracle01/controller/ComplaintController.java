package org.example.oracle01.controller;

import org.example.oracle01.util.PageResult;
import org.example.oracle01.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "complaintType", required = false) String complaintType,
            @RequestParam(value = "complaintStatus", required = false) String complaintStatus,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(
                    "SELECT c.COMPLAINT_ID, o.ORDER_NO, cust.NICKNAME AS CUSTOMER_NAME, st.STAFF_NAME AS STAFF_NAME, ");
            sql.append(
                    "CASE c.COMPLAINT_TYPE WHEN 'ATTITUDE' THEN '服务态度' WHEN 'QUALITY' THEN '服务质量' WHEN 'PRICE' THEN '收费问题' ELSE '其他' END AS COMPLAINT_TYPE, ");
            sql.append("c.COMPLAINT_CONTENT, c.COMPLAINT_STATUS, c.HANDLE_TIME, c.CREATE_TIME, ");
            sql.append("adm.NICKNAME AS HANDLER_NAME, c.HANDLER_COMMENT ");
            sql.append("FROM TB_COMPLAINT c ");
            sql.append("LEFT JOIN TB_ORDER o ON c.ORDER_ID = o.ORDER_ID ");
            sql.append("LEFT JOIN TB_CUSTOMER cust ON c.CUSTOMER_ID = cust.CUSTOMER_ID ");
            sql.append("LEFT JOIN TB_STAFF st ON c.STAFF_ID = st.STAFF_ID ");
            sql.append("LEFT JOIN TB_ADMIN adm ON c.HANDLER_ID = adm.ADMIN_ID ");
            sql.append("WHERE c.IS_DELETED = 'N' ");

            List<Object> params = new ArrayList<>();
            if (orderNo != null && !orderNo.isEmpty()) {
                sql.append("AND o.ORDER_NO LIKE ? ");
                params.add("%" + orderNo + "%");
            }
            if (complaintType != null && !complaintType.isEmpty()) {
                String typeCode = "";
                switch (complaintType) {
                    case "服务态度":
                        typeCode = "ATTITUDE";
                        break;
                    case "服务质量":
                        typeCode = "QUALITY";
                        break;
                    case "收费问题":
                        typeCode = "PRICE";
                        break;
                    case "其他":
                        typeCode = "OTHER";
                        break;
                    default:
                        typeCode = complaintType;
                }
                sql.append("AND c.COMPLAINT_TYPE = ? ");
                params.add(typeCode);
            }
            if (complaintStatus != null && !complaintStatus.isEmpty()) {
                sql.append("AND c.COMPLAINT_STATUS = ? ");
                params.add(complaintStatus);
            }
            if (startTime != null && !startTime.isEmpty()) {
                sql.append("AND DATE(c.CREATE_TIME) >= ? ");
                params.add(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                sql.append("AND DATE(c.CREATE_TIME) <= ? ");
                params.add(endTime);
            }

            sql.append("ORDER BY c.CREATE_TIME DESC LIMIT ? OFFSET ?");
            params.add(size);
            params.add((page - 1) * size);

            List<Map<String, Object>> rawList = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> row : rawList) {
                Map<String, Object> item = new HashMap<>();
                item.put("complaintId", row.get("COMPLAINT_ID"));
                item.put("orderNo", row.get("ORDER_NO"));
                item.put("customerName", row.get("CUSTOMER_NAME"));
                item.put("staffName", row.get("STAFF_NAME"));
                item.put("complaintType", row.get("COMPLAINT_TYPE"));
                item.put("complaintContent", row.get("COMPLAINT_CONTENT"));
                item.put("complaintStatus", row.get("COMPLAINT_STATUS"));
                item.put("handlerName", row.get("HANDLER_NAME"));
                item.put("handleTime", row.get("HANDLE_TIME"));
                item.put("createTime", row.get("CREATE_TIME"));
                item.put("handlerComment", row.get("HANDLER_COMMENT"));
                list.add(item);
            }

            StringBuilder countSql = new StringBuilder();
            countSql.append(
                    "SELECT COUNT(*) FROM TB_COMPLAINT c LEFT JOIN TB_ORDER o ON c.ORDER_ID = o.ORDER_ID WHERE c.IS_DELETED = 'N' ");
            List<Object> countParams = new ArrayList<>();
            if (orderNo != null && !orderNo.isEmpty()) {
                countSql.append("AND o.ORDER_NO LIKE ? ");
                countParams.add("%" + orderNo + "%");
            }
            if (complaintType != null && !complaintType.isEmpty()) {
                String typeCode = "";
                switch (complaintType) {
                    case "服务态度":
                        typeCode = "ATTITUDE";
                        break;
                    case "服务质量":
                        typeCode = "QUALITY";
                        break;
                    case "收费问题":
                        typeCode = "PRICE";
                        break;
                    case "其他":
                        typeCode = "OTHER";
                        break;
                    default:
                        typeCode = complaintType;
                }
                countSql.append("AND c.COMPLAINT_TYPE = ? ");
                countParams.add(typeCode);
            }
            if (complaintStatus != null && !complaintStatus.isEmpty()) {
                countSql.append("AND c.COMPLAINT_STATUS = ? ");
                countParams.add(complaintStatus);
            }
            if (startTime != null && !startTime.isEmpty()) {
                countSql.append("AND DATE(c.CREATE_TIME) >= ? ");
                countParams.add(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                countSql.append("AND DATE(c.CREATE_TIME) <= ? ");
                countParams.add(endTime);
            }

            Integer total = jdbcTemplate.queryForObject(countSql.toString(), Integer.class, countParams.toArray());
            PageResult<Map<String, Object>> pageResult = new PageResult<>(
                    total != null ? total.longValue() : 0L,
                    list,
                    page,
                    size);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("获取投诉列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable("id") Long id) {
        try {
            String sql = "SELECT c.COMPLAINT_ID, o.ORDER_NO, cust.NICKNAME AS CUSTOMER_NAME, st.STAFF_NAME AS STAFF_NAME, "
                    +
                    "CASE c.COMPLAINT_TYPE WHEN 'ATTITUDE' THEN '服务态度' WHEN 'QUALITY' THEN '服务质量' WHEN 'PRICE' THEN '收费问题' ELSE '其他' END AS COMPLAINT_TYPE, "
                    +
                    "c.COMPLAINT_CONTENT, c.COMPLAINT_STATUS, c.HANDLE_TIME, c.CREATE_TIME, " +
                    "adm.NICKNAME AS HANDLER_NAME, c.HANDLER_COMMENT " +
                    "FROM TB_COMPLAINT c " +
                    "LEFT JOIN TB_ORDER o ON c.ORDER_ID = o.ORDER_ID " +
                    "LEFT JOIN TB_CUSTOMER cust ON c.CUSTOMER_ID = cust.CUSTOMER_ID " +
                    "LEFT JOIN TB_STAFF st ON c.STAFF_ID = st.STAFF_ID " +
                    "LEFT JOIN TB_ADMIN adm ON c.HANDLER_ID = adm.ADMIN_ID " +
                    "WHERE c.IS_DELETED = 'N' AND c.COMPLAINT_ID = ?";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, id);
            Map<String, Object> item = new HashMap<>();
            item.put("complaintId", row.get("COMPLAINT_ID"));
            item.put("orderNo", row.get("ORDER_NO"));
            item.put("customerName", row.get("CUSTOMER_NAME"));
            item.put("staffName", row.get("STAFF_NAME"));
            item.put("complaintType", row.get("COMPLAINT_TYPE"));
            item.put("complaintContent", row.get("COMPLAINT_CONTENT"));
            item.put("complaintStatus", row.get("COMPLAINT_STATUS"));
            item.put("handlerName", row.get("HANDLER_NAME"));
            item.put("handleTime", row.get("HANDLE_TIME"));
            item.put("createTime", row.get("CREATE_TIME"));
            item.put("handlerComment", row.get("HANDLER_COMMENT"));
            return Result.success(item);
        } catch (Exception e) {
            return Result.error("获取投诉详情失败：" + e.getMessage());
        }
    }

    @PostMapping("/handle")
    public Result<String> handle(@RequestBody Map<String, Object> body) {
        try {
            Long complaintId = Long.valueOf(body.get("complaintId").toString());
            String status = body.get("complaintStatus") != null ? body.get("complaintStatus").toString() : "已解决";
            String comment = body.get("handlerComment") != null ? body.get("handlerComment").toString() : "";
            // 默认使用管理员 60001 作为处理人示例
            Long handlerId = 60001L;

            String sql = "UPDATE TB_COMPLAINT SET COMPLAINT_STATUS = ?, HANDLER_COMMENT = ?, HANDLER_ID = ?, HANDLE_TIME = NOW(), UPDATE_TIME = NOW() WHERE COMPLAINT_ID = ?";
            int updated = jdbcTemplate.update(sql, status, comment, handlerId, complaintId);
            if (updated > 0) {
                return Result.success("处理成功", null);
            }
            return Result.error("未找到对应投诉记录");
        } catch (Exception e) {
            return Result.error("处理失败：" + e.getMessage());
        }
    }
}
