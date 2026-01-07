package org.example.oracle01.service.impl;

import org.example.oracle01.mapper.OrderMapper;
import org.example.oracle01.model.Order;
import org.example.oracle01.service.OrderService;
import org.example.oracle01.util.Result;
import org.example.oracle01.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Result<PageResult<Map<String, Object>>> getOrderList(Map<String, Object> params) {
        try {
            // 获取总记录数
            Integer page = params.get("page") != null ? (Integer) params.get("page") : 0;
            Integer size = params.get("size") != null ? (Integer) params.get("size") : 10;

            int total = orderMapper.countOrder(params);
            List<Map<String, Object>> orderList = orderMapper.selectOrderList(params);

            PageResult<Map<String, Object>> pageResult = new PageResult<>((long) total, orderList, page / size + 1,
                    size);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("查询订单列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Order> getOrderById(Long id) {
        try {
            Order order = orderMapper.selectOrderById(id);
            if (order != null) {
                return Result.success(order);
            } else {
                return Result.error("未找到ID为 " + id + " 的订单");
            }
        } catch (Exception e) {
            return Result.error("查询订单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> addOrder(Order order) {
        try {
            int result = orderMapper.insertOrder(order);
            if (result > 0) {
                return Result.success("新增订单成功");
            } else {
                return Result.error("新增订单失败");
            }
        } catch (Exception e) {
            return Result.error("新增订单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> updateOrder(Order order) {
        try {
            int result = orderMapper.updateOrder(order);
            if (result > 0) {
                return Result.success("更新订单成功");
            } else {
                return Result.error("更新订单失败");
            }
        } catch (Exception e) {
            return Result.error("更新订单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> deleteOrder(Long id) {
        try {
            int result = orderMapper.deleteOrder(id);
            if (result > 0) {
                return Result.success("删除订单成功");
            } else {
                return Result.error("删除订单失败");
            }
        } catch (Exception e) {
            return Result.error("删除订单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> updateOrderStatus(Long id, String status) {
        try {
            int result = orderMapper.updateOrderStatus(id, status);
            if (result > 0) {
                return Result.success("更新订单状态成功");
            } else {
                return Result.error("更新订单状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新订单状态失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Integer> countOrder(Map<String, Object> params) {
        try {
            int count = orderMapper.countOrder(params);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("统计订单数量失败：" + e.getMessage());
        }
    }
}