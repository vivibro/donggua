package com.vivi.search.repository;

import com.vivi.common.vo.PageResult;
import com.vivi.item.pojo.SpuBo;
import com.vivi.search.client.GoodsClient;
import com.vivi.search.pojo.Goods;
import com.vivi.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;

//    创建索引
    @Test
    public void testCreatIndex(){
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void loadData() throws IOException {
        List<SpuBo> list = new ArrayList<>();
        int page = 1;
        int row = 100;
        int size;
        do {
            //分页查询数据
            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(page, row, null, true);
            List<SpuBo> spus = result.getItems();
            size = spus.size();
            page ++;
            list.addAll(spus);
        }while (size == 100);

        //创建Goods集合
        List<Goods> goodsList = new ArrayList<>();
        //遍历spu
        for (SpuBo spu : list) {
            try {
                System.out.println("spu id" + spu.getId());
                Goods goods = this.searchService.buildGoods(spu);
                System.out.println("spu id" + spu.getId());
                goodsList.add(goods);
            } catch (IOException e) {
                System.out.println("查询失败：" + spu.getId());
            }
        }
        this.goodsRepository.saveAll(goodsList);
    }
}