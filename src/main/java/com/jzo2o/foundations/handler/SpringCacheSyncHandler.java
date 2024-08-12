package com.jzo2o.foundations.handler;

import com.jzo2o.api.foundations.dto.response.RegionSimpleResDTO;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IRegionService;
import com.jzo2o.foundations.service.IServeService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Resource
    private IServeService serveService;

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
            String key2 = RedisConstants.CacheName.SERVE_TYPE + "::" + item.getId().toString();
            String key3 = RedisConstants.CacheName.HOT_SERVE + "::" + item.getId().toString();
            redisTemplate.delete(key1);
            redisTemplate.delete(key2);
            redisTemplate.delete(key3);
            // 查询首页服务列表（添加缓存）
            homeService.queryServeIconCategoryByRegionId(item.getId());
            // 查询服务类型
            homeService.queryServeTypeListByRegionIdCache(item.getId());
            // 查询热门服务
            homeService.findHotServeListByRegionIdCache(item.getId());
        });

        log.info(">>>>>>>>更新已启用区域完成");
    }

    /**
     * 热门服务详情缓存更新
     * 每3小时执行
     */
    @XxlJob(value = "hotServeCacheSync")
    public void hotServeCacheSync() {
        log.info(">>>>>>>>开始进行缓存同步，更新热门服务详情");

        //1.查询热门且上架状态的服务
        List<Serve> hotAndOnSaleServeList = serveService.queryHotAndOnSaleServeList();
        Set<Long> hotServeItemIds=new HashSet<>();

        //2.热门服务缓存续期
        for (Serve serve : hotAndOnSaleServeList) {
            //2.1删除热门服务缓存
            String serveKey=RedisConstants.CacheName.SERVE+"::"+serve.getId();
            redisTemplate.delete(serveKey);

            //2.2重置热门服务缓存
            homeService.queryServeByIdCache(serve.getId());

            //2.2提取热门服务对应的服务项id
            hotServeItemIds.add(serve.getServeItemId());
        }

        //3.对热门服务项更新缓存
        for (Long serveItemId : hotServeItemIds) {
            //3.1删除热门服务项缓存
            String serveKey=RedisConstants.CacheName.SERVE_ITEM+"::"+serveItemId;
            redisTemplate.delete(serveKey);

            //3.2重置热门服务项缓存
            homeService.queryServeItemByIdCache(serveItemId);
        }
        log.info(">>>>>>>>更新热门服务详情完成");
    }
}
