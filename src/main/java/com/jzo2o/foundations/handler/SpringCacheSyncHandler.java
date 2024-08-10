package com.jzo2o.foundations.handler;

import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.service.IRegionService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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

    /**
     * 定时更新缓存
     */
    @XxlJob(value = "activeRegionCacheSync")
    public void activeRegionCacheSync(){
        log.info(">>>>>开始进行缓存同步，更新已启用区域");
        // 1. 清理缓存
        String key = RedisConstants.CacheName.JZ_CACHE+"::ACTIVE_REGIONS";
        Boolean isSuccess = redisTemplate.delete(key);
        // 2. 刷新缓存
        regionService.queryActiveRegionListCache();
        log.info(">>>>>>>>更新已启用区域完成");
    }
}
