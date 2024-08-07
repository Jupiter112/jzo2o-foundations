package com.jzo2o.foundations.service;

import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

/**
 * @className: ServeMapperTest
 * @description: ServeMapper单元测试类
 * @author: angelee
 * @date: 2024/8/6
 **/
@SpringBootTest
@Slf4j
public class ServeMapperTest {

    @Resource
    private ServeMapper serveMapper;

    @Test
    public void test_queryListByRegionId(){
        log.info("ServeMapper单元测试");
        List<ServeResDTO> serveResDTOS = serveMapper.queryListByRegionId(1692472339767234562L);
        Assert.notEmpty(serveResDTOS, "查询数据结果为空");
    }
}
