package org.example.oracle01.service.impl;

import org.example.oracle01.mapper.CustomerMapper;
import org.example.oracle01.model.Customer;
import org.example.oracle01.service.CustomerService;
import org.example.oracle01.util.Result;
import org.example.oracle01.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public Result<PageResult<Customer>> getCustomerList(Map<String, Object> params) {
        try {
            // 获取总记录数
            Integer page = params.get("page") != null ? (Integer) params.get("page") : 0;
            Integer size = params.get("size") != null ? (Integer) params.get("size") : 10;
            
            int total = customerMapper.countCustomer(params);
            List<Customer> customerList = customerMapper.selectCustomerList(params);
            
            PageResult<Customer> pageResult = new PageResult<>((long) total, customerList, page/size + 1, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("查询客户列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Customer> getCustomerById(Long id) {
        try {
            Customer customer = customerMapper.selectCustomerById(id);
            if (customer != null) {
                return Result.success(customer);
            } else {
                return Result.error("未找到ID为 " + id + " 的客户");
            }
        } catch (Exception e) {
            return Result.error("查询客户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> addCustomer(Customer customer) {
        try {
            int result = customerMapper.insertCustomer(customer);
            if (result > 0) {
                return Result.success("新增客户成功");
            } else {
                return Result.error("新增客户失败");
            }
        } catch (Exception e) {
            return Result.error("新增客户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> updateCustomer(Customer customer) {
        try {
            int result = customerMapper.updateCustomer(customer);
            if (result > 0) {
                return Result.success("更新客户成功");
            } else {
                return Result.error("更新客户失败");
            }
        } catch (Exception e) {
            return Result.error("更新客户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> deleteCustomer(Long id) {
        try {
            int result = customerMapper.deleteCustomer(id);
            if (result > 0) {
                return Result.success("删除客户成功");
            } else {
                return Result.error("删除客户失败");
            }
        } catch (Exception e) {
            return Result.error("删除客户失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Integer> countCustomer(Map<String, Object> params) {
        try {
            int count = customerMapper.countCustomer(params);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("统计客户数量失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> rechargeBalance(Long customerId, BigDecimal amount) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("充值金额必须大于0");
            }
            int result = customerMapper.rechargeBalance(customerId, amount);
            if (result > 0) {
                return Result.success("充值成功");
            } else {
                return Result.error("充值失败，客户不存在");
            }
        } catch (Exception e) {
            return Result.error("充值失败：" + e.getMessage());
        }
    }
}