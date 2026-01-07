package org.example.oracle01.service.impl;

import org.example.oracle01.mapper.StaffMapper;
import org.example.oracle01.model.Staff;
import org.example.oracle01.service.StaffService;
import org.example.oracle01.util.Result;
import org.example.oracle01.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private StaffMapper staffMapper;

    @Override
    public Result<PageResult<Staff>> getStaffList(Map<String, Object> params) {
        try {
            // 获取总记录数
            Integer page = params.get("page") != null ? (Integer) params.get("page") : 0;
            Integer size = params.get("size") != null ? (Integer) params.get("size") : 10;
            
            int total = staffMapper.countStaff(params);
            List<Staff> staffList = staffMapper.selectStaffList(params);
            
            PageResult<Staff> pageResult = new PageResult<>((long) total, staffList, page/size + 1, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("查询陪玩人员列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Staff> getStaffById(Long id) {
        try {
            Staff staff = staffMapper.selectStaffById(id);
            if (staff != null) {
                return Result.success(staff);
            } else {
                return Result.error("未找到ID为 " + id + " 的陪玩人员");
            }
        } catch (Exception e) {
            return Result.error("查询陪玩人员失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> addStaff(Staff staff) {
        try {
            int result = staffMapper.insertStaff(staff);
            if (result > 0) {
                return Result.success("新增陪玩人员成功");
            } else {
                return Result.error("新增陪玩人员失败");
            }
        } catch (Exception e) {
            return Result.error("新增陪玩人员失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> updateStaff(Staff staff) {
        try {
            int result = staffMapper.updateStaff(staff);
            if (result > 0) {
                return Result.success("更新陪玩人员成功");
            } else {
                return Result.error("更新陪玩人员失败");
            }
        } catch (Exception e) {
            return Result.error("更新陪玩人员失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> deleteStaff(Long id) {
        try {
            int result = staffMapper.deleteStaff(id);
            if (result > 0) {
                return Result.success("删除陪玩人员成功");
            } else {
                return Result.error("删除陪玩人员失败");
            }
        } catch (Exception e) {
            return Result.error("删除陪玩人员失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Integer> countStaff(Map<String, Object> params) {
        try {
            int count = staffMapper.countStaff(params);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("统计陪玩人员数量失败：" + e.getMessage());
        }
    }
}