package com.leyou.search.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsListener {

    @Autowired
    private SearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            value =@Queue(value = "leyou.search.save.queue",durable = "true"),
            exchange = @Exchange(value = "leyou.item.exchange",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.insert","item.update"}
    ))
    public void save(Long spuId) throws Exception {
        if (spuId == null ){
            return;
        }
        this.searchService.save(spuId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value =@Queue(value = "leyou.search.delete.queue",durable = "true"),
            exchange = @Exchange(value = "leyou.item.exchange",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void delete(Long spuId) throws Exception {
        if (spuId == null ){
            return;
        }
        this.searchService.delete(spuId);
    }
}
