package com.jzo2o.foundations.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.model.Result;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.foundations.FoundationsApplication;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.mapper.ServeMapper;

import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;

import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.mysql.utils.PageHelperUtils;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @className: ServeServiceImpl
 * @description: IServeService实现类
 * @author: angelee
 * @date: 2024/8/6
 **/
@Service
public class ServeServiceImpl  extends ServiceImpl<ServeMapper, Serve> implements IServeService {

    @Resource
    private ServeMapper serveMapper;

    @Resource
    private ServeItemMapper serveItemMapper;

    @Resource
    private RegionMapper regionMapper;

    /**
     * 区域服务分页查询
     * @param servePageQueryReqDTO
     * @return
     */
    @Override
    public PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO) {
        return PageHelperUtils.selectPage(servePageQueryReqDTO,
                () -> serveMapper.queryListByRegionId(servePageQueryReqDTO.getRegionId()));
    }

    @Override
    @Transactional
    public Result batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOS) {
        for(ServeUpsertReqDTO serveUpsertReqDTO: serveUpsertReqDTOS){
            // 1. 校验服务项是否为启用状态，不是启用状态不能新增
            ServeItem serveItem = serveItemMapper.selectById(serveUpsertReqDTO.getServeItemId());
            if(ObjectUtil.isNull(serveItem) || serveItem.getActiveStatus()!= FoundationStatusEnum.ENABLE.getStatus()){
                throw new ForbiddenOperationException("该服务未启用无法添加到区域下使用");
            }
            // 2. 校验是否重复新增，同一个区域不能添加相同的服务
            Integer count = lambdaQuery()
                    .eq(Serve::getServeItemId, serveUpsertReqDTO.getServeItemId())
                    .eq(Serve::getRegionId, serveUpsertReqDTO.getRegionId())
                    .count();
            if(count>0){
                throw new ForbiddenOperationException(serveItem.getName()+"服务已存在");
            }

            // 3. 新增服务
            Serve serve = BeanUtil.toBean(serveUpsertReqDTO, Serve.class);
            Region region = regionMapper.selectById(serveUpsertReqDTO.getRegionId());
            serve.setCityCode(region.getCityCode());
            baseMapper.insert(serve);
        }
        return Result.ok();
    }

    @Override
    public Result update(Long id, BigDecimal price) {
        // 1. 更新服务价格
        boolean isSuccess = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getPrice, price)
                .update();
        if(!isSuccess){
            throw new CommonException("区域服务价格修改失败");
        }
        return Result.ok();
    }

    @Override
    @CachePut(value = RedisConstants.CacheName.SERVE, key = "#id", cacheManager = RedisConstants.CacheManager.ONE_DAY)
    @Transactional
    public Result onSale(Long id) {
        Serve serve = baseMapper.selectById(id);
        if(ObjectUtil.isNull(serve)){
            throw new ForbiddenOperationException("区域服务不存在");
        }
        // 上架状态
        Integer saleStatus = serve.getSaleStatus();
        // 草稿或下架就可以上架
        if(!(saleStatus==FoundationStatusEnum.INIT.getStatus() || saleStatus==FoundationStatusEnum.DISABLE.getStatus())){
            throw new ForbiddenOperationException("服务为启动状态");
        }
        // 查询是否有对应的服务项
        Long serveItemId = serve.getServeItemId();
        ServeItem serveItem = serveItemMapper.selectById(serveItemId);
        if(ObjectUtil.isNull(serve)){
            throw new ForbiddenOperationException("服务项不存在");
        }
        Integer activeStatus = serveItem.getActiveStatus();
        if(activeStatus!=FoundationStatusEnum.ENABLE.getStatus()){
            throw new ForbiddenOperationException("服务项为启动状态才能上架");
        }

        //更新上架状态
        LambdaUpdateWrapper<Serve> updateWrapper = Wrappers.<Serve>lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus());
        boolean isSuccess = update(updateWrapper);
        if(!isSuccess){
            throw new CommonException("服务下架失败");
        }
        return Result.ok();
    }

    @Override
    public Result deleteById(Long id) {
        Serve serve = baseMapper.selectById(id);
        if(ObjectUtil.isNull(serve)){
            throw new ForbiddenOperationException("区域服务不存在");
        }
        // 上架状态
        Integer saleStatus = serve.getSaleStatus();
        // 草稿状态才能删除
        if(saleStatus!=FoundationStatusEnum.INIT.getStatus()){
            throw new ForbiddenOperationException("服务为草稿状态才能删除");
        }
        // 删除服务
        baseMapper.deleteById(id);
        return Result.ok();
    }

    @Override
    @CacheEvict(value = RedisConstants.CacheName.SERVE, key = "#id")
    public Result offSale(Long id) {
        Serve serve = baseMapper.selectById(id);
        if(ObjectUtil.isNull(serve)){
            throw new ForbiddenOperationException("区域服务不存在");
        }
        // 上架状态
        Integer saleStatus = serve.getSaleStatus();
        // 上架的才可以下架
        if(saleStatus!=FoundationStatusEnum.ENABLE.getStatus()){
            throw new ForbiddenOperationException("服务为非启动状态，无法下架");
        }
        // 更新状态为下架
        boolean isSuccess = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getSaleStatus, FoundationStatusEnum.DISABLE.getStatus())
                .update();
        if(!isSuccess){
            throw new CommonException("服务下架失败");
        }
        return Result.ok();
    }

    @Override
    public Result changHotStatus(Long id, Integer flag) {
        //1.设置热门
        LambdaUpdateWrapper<Serve> updateWrapper = Wrappers.<Serve>lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getIsHot, flag)
                .set(Serve::getHotTimeStamp, System.currentTimeMillis());
        super.update(updateWrapper);
        return Result.ok();
    }
}
