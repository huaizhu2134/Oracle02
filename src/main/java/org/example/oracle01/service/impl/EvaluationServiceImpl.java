package org.example.oracle01.service.impl;

import org.example.oracle01.mapper.EvaluationMapper;
import org.example.oracle01.model.Evaluation;
import org.example.oracle01.service.EvaluationService;
import org.example.oracle01.util.Result;
import org.example.oracle01.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Override
    public Result<PageResult<Map<String, Object>>> getEvaluationList(Map<String, Object> params) {
        try {
            // 获取总记录数
            Integer page = params.get("page") != null ? (Integer) params.get("page") : 0;
            Integer size = params.get("size") != null ? (Integer) params.get("size") : 10;

            int total = evaluationMapper.countEvaluation(params);
            List<Map<String, Object>> evaluationList = evaluationMapper.selectEvaluationList(params);

            PageResult<Map<String, Object>> pageResult = new PageResult<>((long) total, evaluationList, page / size + 1,
                    size);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("查询评价列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Evaluation> getEvaluationById(Long id) {
        try {
            Evaluation evaluation = evaluationMapper.selectEvaluationById(id);
            if (evaluation != null) {
                return Result.success(evaluation);
            } else {
                return Result.error("未找到ID为 " + id + " 的评价");
            }
        } catch (Exception e) {
            return Result.error("查询评价失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> addEvaluation(Evaluation evaluation) {
        try {
            int result = evaluationMapper.insertEvaluation(evaluation);
            if (result > 0) {
                return Result.success("新增评价成功");
            } else {
                return Result.error("新增评价失败");
            }
        } catch (Exception e) {
            return Result.error("新增评价失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> updateEvaluation(Evaluation evaluation) {
        try {
            int result = evaluationMapper.updateEvaluation(evaluation);
            if (result > 0) {
                return Result.success("更新评价成功");
            } else {
                return Result.error("更新评价失败");
            }
        } catch (Exception e) {
            return Result.error("更新评价失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> deleteEvaluation(Long id) {
        try {
            int result = evaluationMapper.deleteEvaluation(id);
            if (result > 0) {
                return Result.success("删除评价成功");
            } else {
                return Result.error("删除评价失败");
            }
        } catch (Exception e) {
            return Result.error("删除评价失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Integer> countEvaluation(Map<String, Object> params) {
        try {
            int count = evaluationMapper.countEvaluation(params);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("统计评价数量失败：" + e.getMessage());
        }
    }
}