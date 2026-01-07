package org.example.oracle01.controller;

import org.example.oracle01.model.Order;
import org.example.oracle01.service.OrderService;
import org.example.oracle01.util.Result;
import org.example.oracle01.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 查询订单列表
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> getOrderList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "staffName", required = false) String staffName) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", (page - 1) * size);
        params.put("size", size);
        params.put("orderNo", orderNo);
        params.put("status", status);
        params.put("customerName", customerName);
        params.put("staffName", staffName);

        return orderService.getOrderList(params);
    }

    // 根据ID查询订单
    @GetMapping("/{id}")
    public Result<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    // 新增订单
    @PostMapping
    public Result<String> addOrder(@RequestBody Order order) {
        return orderService.addOrder(order);
    }

    // 更新订单
    @PutMapping
    public Result<String> updateOrder(@RequestBody Order order) {
        return orderService.updateOrder(order);
    }

    // 删除订单
    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }

    // 更新订单状态
    @PutMapping("/{id}/status")
    public Result<String> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return orderService.updateOrderStatus(id, status);
    }
}