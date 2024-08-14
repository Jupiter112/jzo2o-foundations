package com.jzo2o.foundations.controller.consumer;

import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeAggregationTypeSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.foundations.service.IServeSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @className: FirstPageServeController
 * @description: 门户查询缓存类接口
 * @author: angelee
 * @date: 2024/8/10
 **/
@RestController("consumerFirstPageServeController")
@RequestMapping("/customer/serve")
@Api(tags = "用户端 - 首页服务查询接口")
public class FirstPageServeController {

    @Resource
    private HomeService homeService;

    @Resource
    private IServeService serveService;

    @Resource
    private IServeSyncService serveSyncService;

    @GetMapping("/firstPageServeList")
    @ApiOperation("首页服务列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "regionId", value = "区域id", required = true, dataTypeClass = Long.class)
    })
    public List<ServeCategoryResDTO> firstPageServeList(@RequestParam("regionId") Long regionId){
        return homeService.queryServeIconCategoryByRegionId(regionId);
    }

    @GetMapping("/serveTypeList")
    @ApiOperation("服务类型列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "regionId", value = "区域id", required = true, dataTypeClass = Long.class)
    })
    public List<ServeAggregationTypeSimpleResDTO> serveTypeList(@RequestParam("regionId") Long regionId){
        return homeService.queryServeTypeListByRegionIdCache(regionId);
    }

    @GetMapping("/hotServeList")
    public List<ServeAggregationSimpleResDTO> hotServeList(@RequestParam("regionId") Long regionId){
        return homeService.findHotServeListByRegionIdCache(regionId);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据服务id查询服务详情页面")
    public ServeAggregationSimpleResDTO findDetailById(@NotNull(message = "id不能为空") @PathVariable("id") Long id){
        return serveService.findDetailById(id);
    }

    @GetMapping("/search")
    @ApiOperation(("首页服务搜索"))
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cityCode", value = "城市编码", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "serveTypeId", value = "服务类型id", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "keyword", value = "关键词", dataTypeClass = String.class),

    })
    public List<ServeSimpleResDTO> findServeList(@RequestParam("cityCode") String cityCode,
                                                 @RequestParam("serveTypeId") Long serveTypeId,
                                                 @RequestParam("keyword") String keyword){
        return serveSyncService.findServeList(cityCode, serveTypeId, keyword);
    }

}
