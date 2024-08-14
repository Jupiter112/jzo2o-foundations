package com.jzo2o.foundations.service;

import com.jzo2o.foundations.model.domain.ServeSync;
import com.jzo2o.foundations.model.dto.request.ServeSyncUpdateReqDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;

import java.util.List;

/**
 * <p>
 * 服务同步表 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-07-10
 */
public interface IServeSyncService extends IService<ServeSync> {
    /**
     * 根据服务项id更新
     *
     * @param serveItemId           服务项id
     * @param serveSyncUpdateReqDTO 服务同步更新数据
     */
    void updateByServeItemId(Long serveItemId, ServeSyncUpdateReqDTO serveSyncUpdateReqDTO);

    /**
     * 根据服务类型id更新
     *
     * @param serveTypeId           服务类型id
     * @param serveSyncUpdateReqDTO 服务同步更新数据
     */
    void updateByServeTypeId(Long serveTypeId, ServeSyncUpdateReqDTO serveSyncUpdateReqDTO);

    List<ServeSimpleResDTO> findServeList(String cityCode, Long serveTypeId, String keyword);
}
