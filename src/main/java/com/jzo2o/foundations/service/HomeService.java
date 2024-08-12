package com.jzo2o.foundations.service;

import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeAggregationTypeSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;

import java.util.List;

/**
 * @className: HomeService
 * @description: TODO 类描述
 * @author: angelee
 * @date: 2024/8/10
 **/
public interface HomeService {
    List<ServeCategoryResDTO> queryServeIconCategoryByRegionId(Long regionId);

    List<ServeAggregationTypeSimpleResDTO> queryServeTypeListByRegionIdCache(Long regionId);

    List<ServeAggregationSimpleResDTO> findHotServeListByRegionIdCache(Long regionId);

    Serve queryServeByIdCache(Long id);

    ServeItem queryServeItemByIdCache(Long serveItemId);
}
