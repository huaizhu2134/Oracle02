package org.example.oracle01.service;

import org.example.oracle01.model.Order;
import org.example.oracle01.util.Result;
import org.example.oracle01.util.PageResult;

import java.util.Map;

public interface OrderService {

    // 查询订单列表
    Result<PageResult<Map<String, Object>>> getOrderList(Map<String, Object> params);

    // 根据ID查询订单
    Result<Order> getOrderById(Long id);

    // 新增订单
    Result<String> addOrder(Order order);

    // 更新订单
    Result<String> updateOrder(Order order);

    // 删除订单
    Result<String> deleteOrder(Long id);

    // 更新订单状态
    Result<String> updateOrderStatus(Long id, String status);

    // 统计订单数量
    Result<Integer> countOrder(Map<String, Object> params);
}