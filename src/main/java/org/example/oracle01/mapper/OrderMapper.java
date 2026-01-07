package org.example.oracle01.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.oracle01.model.Order;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    // 查询订单列表
    List<Map<String, Object>> selectOrderList(@Param("params") Map<String, Object> params);

    // 根据ID查询订单
    Order selectOrderById(@Param("id") Long id);

    // 新增订单
    int insertOrder(Order order);

    // 更新订单
    int updateOrder(Order order);

    // 删除订单（逻辑删除）
    int deleteOrder(@Param("id") Long id);

    // 统计订单数量
    int countOrder(@Param("params") Map<String, Object> params);

    // 更新订单状态
    int updateOrderStatus(@Param("id") Long id, @Param("status") String status);
}