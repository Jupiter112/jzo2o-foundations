package com.jzo2o.foundations.service;

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

}
