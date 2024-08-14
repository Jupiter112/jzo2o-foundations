package com.jzo2o.foundations.handler;

import com.jzo2o.canal.listeners.AbstractCanalRabbitMqMsgListener;
import com.jzo2o.es.core.ElasticSearchTemplate;
import com.jzo2o.foundations.constants.IndexConstants;
import com.jzo2o.foundations.model.domain.ServeSync;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @className: ServeCanalDataSyncHandler
 * @description: 服务信息同步程序
 * @author: angelee
 * @date: 2024/8/14
 **/
@Component
public class ServeCanalDataSyncHandler extends AbstractCanalRabbitMqMsgListener<ServeSync> {

    @Resource
    private ElasticSearchTemplate elasticSearchTemplate;

    //@RabbitListener(queues = "canal-mq-jzo2o-foundations", concurrency = "1")
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "canal-mq-jzo2o-foundations",arguments={@Argument(name="x-single-active-consumer", value = "true", type = "java.lang.Boolean") }),
            exchange = @Exchange(name="exchange.canal-jzo2o",type = ExchangeTypes.TOPIC),
            key="canal-mq-jzo2o-foundations"),
            concurrency="1"
    )
    public void onMessage(Message message) throws Exception{
        parseMsg(message);
    }

    /**
     * 向es中保存数据，解析binlog中的新增，更新消息执行此方法
     * @param data
     */
    @Override
    public void batchSave(List<ServeSync> data) {
        Boolean aBoolean = elasticSearchTemplate.opsForDoc().batchInsert(IndexConstants.SERVE, data);
        // 如果执行失败，要抛出异常，给mq回nack
        if(!aBoolean){
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                throw new RuntimeException(e);
            }
            throw new RuntimeException("同步失败");
        }
    }

    /**
     * 将es中的文档进行删除，解析打binlog中delete消息，将es中指定文档删除
     * @param ids
     */
    @Override
    public void batchDelete(List<Long> ids) {
        Boolean aBoolean = elasticSearchTemplate.opsForDoc().batchDelete(IndexConstants.SERVE, ids);
        if(!aBoolean){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("同步失败");
        }
    }
}
