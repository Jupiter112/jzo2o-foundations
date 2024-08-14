package com.jzo2o.foundations.service.impl;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.es.core.ElasticSearchTemplate;
import com.jzo2o.es.utils.SearchResponseUtils;
import com.jzo2o.foundations.mapper.ServeSyncMapper;
import com.jzo2o.foundations.model.domain.ServeAggregation;
import com.jzo2o.foundations.model.domain.ServeSync;
import com.jzo2o.foundations.model.dto.request.ServeSyncUpdateReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
import com.jzo2o.foundations.service.IServeSyncService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务同步表 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-07-10
 */
@Service
public class ServeSyncServiceImpl extends ServiceImpl<ServeSyncMapper, ServeSync> implements IServeSyncService {

    @Resource
    private ElasticSearchTemplate elasticSearchTemplate;

    /**
     * 根据服务项id更新
     *
     * @param serveItemId           服务项id
     * @param serveSyncUpdateReqDTO 服务同步更新数据
     */
    @Override
    public void updateByServeItemId(Long serveItemId, ServeSyncUpdateReqDTO serveSyncUpdateReqDTO) {
        LambdaUpdateWrapper<ServeSync> updateWrapper = Wrappers.<ServeSync>lambdaUpdate()
                .eq(ServeSync::getServeItemId, serveItemId)
                .set(ServeSync::getServeItemName, serveSyncUpdateReqDTO.getServeItemName())
                .set(ServeSync::getServeItemSortNum, serveSyncUpdateReqDTO.getServeItemSortNum())
                .set(ServeSync::getUnit, serveSyncUpdateReqDTO.getUnit())
                .set(ServeSync::getServeItemImg, serveSyncUpdateReqDTO.getServeItemImg())
                .set(ServeSync::getServeItemIcon, serveSyncUpdateReqDTO.getServeItemIcon());
        super.update(updateWrapper);
    }

    /**
     * 根据服务类型id更新
     *
     * @param serveTypeId           服务类型id
     * @param serveSyncUpdateReqDTO 服务同步更新数据
     */
    @Override
    public void updateByServeTypeId(Long serveTypeId, ServeSyncUpdateReqDTO serveSyncUpdateReqDTO) {
        LambdaUpdateWrapper<ServeSync> updateWrapper = Wrappers.<ServeSync>lambdaUpdate()
                .eq(ServeSync::getServeTypeId, serveTypeId)
                .set(ServeSync::getServeTypeName, serveSyncUpdateReqDTO.getServeTypeName())
                .set(ServeSync::getServeTypeImg, serveSyncUpdateReqDTO.getServeTypeImg())
                .set(ServeSync::getServeTypeIcon, serveSyncUpdateReqDTO.getServeTypeIcon())
                .set(ServeSync::getServeTypeSortNum, serveSyncUpdateReqDTO.getServeTypeSortNum());
        super.update(updateWrapper);
    }

    @Override
    public List<ServeSimpleResDTO> findServeList(String cityCode, Long serveTypeId, String keyword) {
        // 构造查询条件
        SearchRequest.Builder builder = new SearchRequest.Builder();

        builder.query(query -> query.bool(bool -> {
            //匹配citycode
            bool.must(must ->
                    must.term(term ->
                            term.field("city_code").value(cityCode)));
            //匹配服务类型
            bool.must(must ->
                    must.term(term ->
                            term.field("serveTypeId").value(serveTypeId)));

            //匹配关键字
            if (ObjectUtils.isNotEmpty(keyword)) {
                bool.must(must ->
                        must.multiMatch(multiMatch ->
                                multiMatch.fields("serve_item_name", "serve_type_name").query(keyword)));
            }
            return bool;
        }));
        // 排序 按服务项的serveItemSortNum排序(升序)
        List<SortOptions> sortOptions = new ArrayList<>();
        sortOptions.add(SortOptions.of(sortOption -> sortOption.field(field -> field.field("serve_item_sort_num").order(SortOrder.Asc))));
        builder.sort(sortOptions);
        //指定索引
        builder.index("serve_aggregation");
        //请求对象
        SearchRequest searchRequest = builder.build();
        // 检索数据
        SearchResponse<ServeAggregation> searchResponse = elasticSearchTemplate.opsForDoc().search(searchRequest, ServeAggregation.class);
        //如果搜索成功返回结果集
        if (SearchResponseUtils.isSuccess(searchResponse)) {
            List<ServeAggregation> collect = searchResponse.hits().hits()
                    .stream().map(hit -> {
                        ServeAggregation serve = hit.source();
                        return serve;
                    })
                    .collect(Collectors.toList());
            List<ServeSimpleResDTO> serveSimpleResDTOS = BeanUtil.copyToList(collect, ServeSimpleResDTO.class);
            return serveSimpleResDTOS;
        }
        return  Collections.emptyList();
    }
}
