package com.zy.demo.web;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zy.core.result.PageResult;
import com.zy.core.result.Result;
import com.zy.demo.entity.DemoItem;
import com.zy.demo.mapper.DemoItemMapper;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * H2、校验和分页的示例接口。
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    private final DemoItemMapper demoItemMapper;

    /**
     * 使用示例 Mapper 创建控制器。
     */
    public DemoController(DemoItemMapper demoItemMapper) {
        this.demoItemMapper = demoItemMapper;
    }

    /**
     * 返回可预测的问候结果。
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.ok("hello");
    }

    /**
     * 校验 JSON 请求体中的名称字段。
     */
    @PostMapping("/validate")
    public Result<DemoValidationRequest> validate(@Valid @RequestBody DemoValidationRequest request) {
        return Result.ok(request);
    }

    /**
     * 查询 H2 示例数据分页。
     */
    @GetMapping("/page")
    public Result<PageResult<DemoItem>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "2") long pageSize) {
        Page<DemoItem> page = demoItemMapper.selectPage(
                new Page<>(pageNum, pageSize),
                Wrappers.<DemoItem>lambdaQuery().orderByAsc(DemoItem::getId));
        return Result.ok(PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords()));
    }
}
