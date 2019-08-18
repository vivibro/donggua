package com.vivi.search.mq;

import com.vivi.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ItemListener {
    @Autowired
    SearchService searchService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.insert.queue", durable = "true"),
            exchange = @Exchange(name = "dg.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.insert","item.update"}
    ))
    public void listenInsertOrUpdate (Long spuID) throws IOException {
        if(spuID == null)return;
//        索引库新增
        searchService.creatOrUpdateIndex(spuID);

    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.insert.queue", durable = "true"),
            exchange = @Exchange(name = "dg.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void listenDelete (Long spuID) throws IOException {
        if(spuID == null)return;
//        索引库新增
        searchService.deleteIndex(spuID);

    }

}
