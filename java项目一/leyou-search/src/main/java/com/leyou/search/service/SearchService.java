package com.leyou.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.BooleanQuery;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Goods buildGoods(Spu spu) throws JsonProcessingException {
        Goods goods = new Goods();

        //1.根据分类的id查询分类的名称
        List<String> names = categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //2.根据品牌id查询品牌名称
        Brand brand = brandClient.queryBrandByBid(spu.getBrandId());

        //3.根据spu_id查询所有的sku
        List<Sku> skus = goodsClient.querySkusBySpuId(spu.getId());
        //初始化一个价格集合，收集所有的sku的price
        List<Long> prices = new ArrayList<>();
        //收集sku的必要字段信息
        List<Map<String, Object>> skuMapList = new ArrayList<>();

        skus.forEach(sku -> {
            prices.add(sku.getPrice());

            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            //获取sku中的图片，数据库中的图片可能是多张，以，进行分隔，所有也以，进行切割，返回图片数组，获取第一张图片
            map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            skuMapList.add(map);
        });

        //4.根据spu中的cid3查询出所有的搜索规格参数
        List<SpecParam> params = specificationClient.queryParamByGid(null, spu.getCid3(), null, true);
        //先根据spu_id查询spuDetail
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spu.getId());
        //反序列通用的规格参数值，化为map结构
        Map<String, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {
        });
        //反序列特殊的规格参数值，化为map结构
        Map<String, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>() {
        });

        //存储参数的map
        Map<String, Object> specs = new HashMap<>();

        params.forEach(param -> {
            //判断规格参数的类型，是否是通用的规格参数
            if (param.getGeneric()) {
                //如果是通用类型的参数，从genericSpecMap获取规格参数值
                String value = genericSpecMap.get(param.getId().toString()).toString();
                //判断是否是数值类型，一个返回一个区间
                if (param.getNumeric()) {
                    value = chooseSegment(value, param);
                }
                specs.put(param.getName(), value);

            } else {
                //如果是特殊类型的参数，从specialSpecMap获取规格参数值
                List<Object> value = specialSpecMap.get(param.getId().toString());
                specs.put(param.getName(), value);
            }
        });

        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());

        //拼接all字符串，需要title 和 分类名称 品牌名称
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " ") + " " + brand.getName());

        //获取spu下所有sku的价格
        goods.setPrice(prices);

        //获取spu下的所有sku并转换成json字符串
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));

        //获取所有查询的规格参数{name：value}
        goods.setSpecs(specs);
        return goods;
    }


    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        if (StringUtils.isBlank(key)) {
            return null;
        }
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //1.添加基本查询
        BoolQueryBuilder basicQuery = buildBooleanQueryBuilder(searchRequest); //过滤的查询
        //QueryBuilder basicQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);
        queryBuilder.withQuery(basicQuery);

        //2.添加分页查询
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize()));

        //3.通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));

        //4.添加排序条件
        if (StringUtils.isNotBlank(searchRequest.getSortBy())) {
            queryBuilder.withSort(SortBuilders.fieldSort(searchRequest.getSortBy()).order(searchRequest.getDescending() ? SortOrder.DESC : SortOrder.ASC));
        }

        //添加分类和品牌的聚合
        String categoryAggName = "categories";
        String brandAggName = "brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //5、查询，获取结果
        AggregatedPage<Goods> pageInfo = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());

        //获取聚合结果集，并解析
        List<Map<String, Object>> categories = getCategoryAggResult(pageInfo.getAggregation(categoryAggName));

        List<Brand> brands = getBrandAggResult(pageInfo.getAggregation(brandAggName));

        //判断是否是一个分类，只有一个分类才做规格参数的聚合
        List<Map<String, Object>> specs = null;
        if (!CollectionUtils.isEmpty(categories) && categories.size() == 1 ) {
            //对规格参数进行聚合
            specs = getParamAggResult((Long) categories.get(0).get("id"), basicQuery);
            
        }
        /*LongTerms categoryAggregation =(LongTerms) pageInfo.getAggregation(categoryAggName);
        LongTerms brandAggregation =(LongTerms) pageInfo.getAggregation(brandAggName);*/


        //6. 封装结果并返回

        return new SearchResult(pageInfo.getTotalElements(), pageInfo.getTotalPages(), pageInfo.getContent(), brands, categories, specs);
    }

    /**
     * 构建bool查询的过滤
     * @param searchRequest
     * @return
     */
    private BoolQueryBuilder buildBooleanQueryBuilder(SearchRequest searchRequest) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //给bool查询添加基本查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()).operator(Operator.AND));

        //添加过滤条件
        if (CollectionUtils.isEmpty(searchRequest.getFilter())){
            return boolQueryBuilder;
        }
        for (Map.Entry<String, String> entry : searchRequest.getFilter().entrySet()) {
            String key = entry.getKey();
            if (key.equals("品牌")){
                key = "brandId";
            }else if (key.equals("分类")){
                key = "cid3";
            }else{
                key = "specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }

        return boolQueryBuilder;
    }

    /**
     * 根据查询条件聚合参数
     *
     * @param cid
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getParamAggResult(Long cid, QueryBuilder basicQuery) {
        //自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        queryBuilder.withQuery(basicQuery);
        //查询聚合的规格参数
        List<SpecParam> params = specificationClient.queryParamByGid(null, cid, null, true);

        //添加规格参数的聚合
        params.forEach(param -> {
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs." + param.getName() + ".keyword"));
        });

        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));

        //执行聚合查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());

        //解析结果集,key-聚合名称（规格参数名）  value聚合对象
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();

        List<Map<String, Object>> specs = new ArrayList<>();
        for (Map.Entry<String, Aggregation> entry : aggregationMap.entrySet()) {
            //初始化一个map，key为规格参数名，value是聚合的规格参数数组
            Map<String, Object> map = new HashMap<>();
            map.put("key", entry.getKey());

            //获取聚合
            StringTerms terms = (StringTerms) entry.getValue();

            //获取桶集合
            //把桶中的key收集成options集合
            List<String> options = new ArrayList<>();
            terms.getBuckets().forEach(bucket -> {
                options.add(bucket.getKeyAsString());
            });
            map.put("options", options);
            specs.add(map);
        }
        return specs;
    }

    /**
     * 解析brand的聚合结果集
     *
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        LongTerms terms = (LongTerms) aggregation;
        return terms.getBuckets().stream().map(bucket -> {
            return brandClient.queryBrandByBid(bucket.getKeyAsNumber().longValue());
        }).collect(Collectors.toList());

    }

    /**
     * 解析category的聚合结果集
     *
     * @param aggregation
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms terms = (LongTerms) aggregation;

        return terms.getBuckets().stream().map(bucket -> {
            Map<String, Object> map = new HashMap<>();
            long id = bucket.getKeyAsNumber().longValue();
            List<String> names = categoryClient.queryNamesByIds(Arrays.asList(id));
            map.put("id", id);
            map.put("name", names.get(0));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 在elasticsearch中增加
     * @param spuId
     */
    public void save(Long spuId) throws Exception {
        Spu spu = this.goodsClient.querySpuById(spuId);
        Goods goods = this.buildGoods(spu);
        goodsRepository.save(goods);
    }

    /**
     * 在elasticsearch中删除
     * @param spuId
     */
    public void delete(Long spuId) {

        goodsRepository.deleteById(spuId);
    }
}
