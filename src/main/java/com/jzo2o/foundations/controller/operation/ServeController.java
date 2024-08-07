package com.jzo2o.foundations.controller.operation;

import com.jzo2o.common.enums.EnableStatusEnum;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.model.Result;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @className: ServeController
 * @description: 区域服务管理
 * @author: angelee
 * @date: 2024/8/6
 **/
@RestController("operationServeController")
@RequestMapping("/operation/serve")
@Api(tags = "运营端 - 区域服务管理相关接口")
@Slf4j
public class ServeController {

    @Resource
    private IServeService serveService;

    @GetMapping("/page")
    @ApiOperation("区域服务分页查询")
    public PageResult<ServeResDTO> page(@RequestBody ServePageQueryReqDTO servePageQueryReqDTO){
        return serveService.page(servePageQueryReqDTO);
    }

    @PostMapping("/batch")
    @ApiOperation("区域服务批量新增")
    public Result batch(@RequestBody List<ServeUpsertReqDTO> serveUpsertReqDTOS){
        log.info("区域服务批量新增");
        return serveService.batchAdd(serveUpsertReqDTOS);
    }

    @PutMapping("/{id}")
    @ApiOperation("修改区域服务价格")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "服务id", required = true, dataTypeClass = Long.class),
            @ApiImplicitParam(name = "price", value = "价格", required = true, dataTypeClass = BigDecimal.class)
    })
    public Result update(@PathVariable("id") Long id, @PathVariable("price") BigDecimal price){
        log.info("修改区域服务价格",id,price);
        return serveService.update(id, price);
    }

    @PutMapping("/onSale/{id}")
    @ApiOperation("区域服务上架")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "服务id", required = true, dataTypeClass = Long.class)
    })
    public Result onSale(@PathVariable("id") Long id){
        return serveService.onSale(id);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除区域服务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "服务id", required = true, dataTypeClass = Long.class)
    })
    public Result delete(@NotNull(message = "id不能为空") @PathVariable Long id){
        return serveService.deleteById(id);
    }

    @PutMapping("/offSale/{id}")
    @ApiOperation("区域服务下架")
    public Result offSale(@PathVariable Long id){
        return serveService.offSale(id);
    }

    @PutMapping("/onHot/{id}")
    @ApiOperation("设置热门服务")
    public Result onHot(@PathVariable Long id){
        return serveService.changHotStatus(id, EnableStatusEnum.ENABLE.getStatus());
    }

    @PutMapping("/offHot/{id}")
    @ApiOperation("取消热门服务")
    public Result offHot(@PathVariable Long id){
        return serveService.changHotStatus(id, EnableStatusEnum.DISABLE.getStatus());
    }


}
