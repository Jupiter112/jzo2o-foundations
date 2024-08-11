package com.jzo2o.foundations.handler;

import com.jzo2o.api.foundations.dto.response.RegionSimpleResDTO;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IRegionService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @className: SpringCacheSyncHandler
 * @description: 缓存同步任务
 * @author: angelee
 * @date: 2024/8/10
 **/
@Component
@Slf4j
public class SpringCacheSyncHandler {

    @Resource
    private IRegionService regionService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private HomeService homeService;

    /**
     * 定时更新缓存
     */
    @XxlJob(value = "activeRegionCacheSync")
    public void activeRegionCacheSync(){
        log.info(">>>>>开始进行缓存同步，更新已启用区域");
        // 1. 清理缓存
        String key = RedisConstants.CacheName.JZ_CACHE+"::ACTIVE_REGIONS";
        redisTemplate.delete(key);
        // 2. 添加缓存,拿到所有开通的区域
        List<RegionSimpleResDTO> regionSimpleResDTOS = regionService.queryActiveRegionListCache();
        // 遍历区域，对每个区域的首页服务列表进行删除缓存再添加缓存
        regionSimpleResDTOS.forEach(item->{
            String key1 = RedisConstants.CacheName.SERVE_ICON+"::"+item.getId().toString();
            redisTemplate.delete(key1);
            // 查询首页服务列表（添加缓存）
            homeService.queryServeIconCategoryByRegionId(item.getId());
        });

        log.info(">>>>>>>>更新已启用区域完成");
    }
}
