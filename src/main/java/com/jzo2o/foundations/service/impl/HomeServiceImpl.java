package com.jzo2o.foundations.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IRegionService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @className: HomeServiceImpl
 * @description: TODO 类描述
 * @author: angelee
 * @date: 2024/8/10
 **/
@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    private IRegionService regionService;

    @Resource
    private ServeMapper serveMapper;

    /**
     * 根据区域id查询已开通的服务类型
     * @param regionId
     * @return
     */
    @Override
    @Caching(
            cacheable = {
                    // result 为null时，属于缓存穿透情况，缓存时间为30分钟
                    @Cacheable(value = RedisConstants.CacheName.SERVE_ICON, key = "#regionId", unless = "#result.size()!=0", cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
                    // result 不为null时，永久缓存
                    @Cacheable(value = RedisConstants.CacheName.SERVE_ICON, key = "#regionId", unless = "#result.size()==0", cacheManager = RedisConstants.CacheManager.FOREVER)
            }
    )
    public List<ServeCategoryResDTO> queryServeIconCategoryByRegionId(Long regionId) {

        // 1. 校验当前城市是否为启用状态
        Region region = regionService.getById(regionId);
        if(ObjectUtil.isNull(region) || ObjectUtil.equal(FoundationStatusEnum.DISABLE.getStatus(), region.getActiveStatus())){
            return Collections.emptyList();
        }
        // 2. 根据城市编码查询所有的服务图标
        List<ServeCategoryResDTO> list = serveMapper.findServeIconCategoryByRegionId(regionId);
        if(ObjectUtil.isNull(list)){
            return Collections.emptyList();
        }
        // 3. 服务类型取前两个，每个类型下服务项取前4个
        int endIndex = list.size() >= 2 ? 2 : list.size();
        // 最多包含两个服务类型
        List<ServeCategoryResDTO> serveCategoryResDTOS = new ArrayList<>(list.subList(0, endIndex));
        serveCategoryResDTOS.forEach(item->{
            List<ServeSimpleResDTO> serveResDTOList = item.getServeResDTOList();
            int endIndex2 = serveResDTOList.size() >= 4 ? 4 : serveResDTOList.size();
            // 取出最多4个服务项
            ArrayList<ServeSimpleResDTO> serveSimpleResDTOS = new ArrayList<>(serveResDTOList.subList(0, endIndex2));
            item.setServeResDTOList(serveSimpleResDTOS);
        });

        return serveCategoryResDTOS;
    }
}
