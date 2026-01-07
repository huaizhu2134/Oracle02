package org.example.oracle01.service;

import org.example.oracle01.model.Evaluation;
import org.example.oracle01.util.Result;
import org.example.oracle01.util.PageResult;

import java.util.List;
import java.util.Map;

public interface EvaluationService {

    // 查询评价列表
    Result<PageResult<Map<String, Object>>> getEvaluationList(Map<String, Object> params);

    // 根据ID查询评价
    Result<Evaluation> getEvaluationById(Long id);

    // 新增评价
    Result<String> addEvaluation(Evaluation evaluation);

    // 更新评价
    Result<String> updateEvaluation(Evaluation evaluation);

    // 删除评价
    Result<String> deleteEvaluation(Long id);

    // 统计评价数量
    Result<Integer> countEvaluation(Map<String, Object> params);
}