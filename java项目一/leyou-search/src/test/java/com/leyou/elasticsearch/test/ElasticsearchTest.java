package com.leyou.elasticsearch.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearchTest {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsClient goodsClient;
    @Test
    public void test(){
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);

        Integer page = 1;
        Integer rows = 100;

        do {
            //分页查询spu，获取分页结果集
            PageResult<SpuBo> result = goodsClient.querySpuBoByPage(null, null, page, rows);
            //获取当前页的数据
            List<SpuBo> items = result.getItems();
            //处理List<spuBo>转换为List<Goods>
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return searchService.buildGoods(spuBo);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());

            //执行新增数据的方法
            goodsRepository.saveAll(goodsList);
            rows = items.size();
            page++;
        }while (rows>=100);

    }
}
