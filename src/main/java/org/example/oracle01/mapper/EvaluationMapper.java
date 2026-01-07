package org.example.oracle01.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.oracle01.model.Evaluation;

import java.util.List;
import java.util.Map;

@Mapper
public interface EvaluationMapper {

    // 查询评价列表
    List<Map<String, Object>> selectEvaluationList(@Param("params") Map<String, Object> params);

    // 根据ID查询评价
    Evaluation selectEvaluationById(@Param("id") Long id);

    // 新增评价
    int insertEvaluation(Evaluation evaluation);

    // 更新评价
    int updateEvaluation(Evaluation evaluation);

    // 删除评价（逻辑删除）
    int deleteEvaluation(@Param("id") Long id);

    // 统计评价数量
    int countEvaluation(@Param("params") Map<String, Object> params);
}