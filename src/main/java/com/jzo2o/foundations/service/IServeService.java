package com.jzo2o.foundations.service;


import com.jzo2o.common.model.PageResult;

import com.jzo2o.common.model.Result;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * @className: IServeService
 * @description: TODO 类描述
 * @author: angelee
 * @date: 2024/8/6
 **/
public interface IServeService{

    PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO);

    Result batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOS);

    Result update(Long id, BigDecimal price);

    Result onSale(Long id);

    Result deleteById(Long id);

    Result offSale(Long id);

    Result changHotStatus(Long id, Integer flag);
}
