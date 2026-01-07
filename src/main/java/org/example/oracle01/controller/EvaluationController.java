package org.example.oracle01.controller;

import org.example.oracle01.model.Evaluation;
import org.example.oracle01.service.EvaluationService;
import org.example.oracle01.util.Result;
import org.example.oracle01.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    // 查询评价列表
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> getEvaluationList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "score", required = false) Integer score,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "staffName", required = false) String staffName,
            @RequestParam(value = "customerName", required = false) String customerName) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", (page - 1) * size);
        params.put("size", size);
        params.put("orderId", orderId);
        params.put("score", score);
        params.put("orderNo", orderNo);
        params.put("staffName", staffName);
        params.put("customerName", customerName);

        return evaluationService.getEvaluationList(params);
    }

    // 根据ID查询评价
    @GetMapping("/{id}")
    public Result<Evaluation> getEvaluationById(@PathVariable Long id) {
        return evaluationService.getEvaluationById(id);
    }

    // 新增评价
    @PostMapping
    public Result<String> addEvaluation(@RequestBody Evaluation evaluation) {
        return evaluationService.addEvaluation(evaluation);
    }

    // 更新评价
    @PutMapping
    public Result<String> updateEvaluation(@RequestBody Evaluation evaluation) {
        return evaluationService.updateEvaluation(evaluation);
    }

    // 删除评价
    @DeleteMapping("/{id}")
    public Result<String> deleteEvaluation(@PathVariable Long id) {
        return evaluationService.deleteEvaluation(id);
    }
}