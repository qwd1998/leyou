package com.leyou.cart.service.impl;

import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    private static final String KEY_PREFIX = "user:cart";

    /**
     * 添加购物车
     * @param cart
     */
    @Override
    public void addCart(Cart cart) {
        //1.查询用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //判读用户是否存在购物车记录
        if (!redisTemplate.hasKey(KEY_PREFIX+userInfo.getId())){
            return;
        }

        //保存当前用户的购物车数量
        Integer num = cart.getNum();


        //2.查询该用户的购物车记录,在redis中查询
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        //获取
        String key = cart.getSkuId().toString();
        //3.判断该物品在不在该用户的购物车里
        if (hashOps.hasKey(key)) {
            //4.购物车里有这个商品，则更新数据
            //根据skuId查询该商品的信息，转换为json字符串
            String cartJson = hashOps.get(key).toString();

            cart = JsonUtils.parse(cartJson, Cart.class);

            cart.setNum(cart.getNum() + num);


        } else {
            //5.购物车里没有这个商品，添加购物车
            cart.setUserId(userInfo.getId());

            //其余的添加都是sku商品的信息，所有需要skuId查找sku信息，放入cart中
            Sku sku = goodsClient.querySkuBySkuId(cart.getSkuId());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);

        }
        //更新数据放入redis中
        hashOps.put(key, JsonUtils.serialize(cart));

    }

    /**
     * 查询购物车
     * @return
     */
    @Override
    public List<Cart> queryCarts() {
        //1.获取用户的信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //判断用户是否存在购物车
        if (!redisTemplate.hasKey(KEY_PREFIX + userInfo.getId())) {
            return null;
        }

        //购物车结构Map<userId,Map<skuId,Carts>>

        //2.获取用户的购物车记录
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        //获取购物车中所有Cart值
        List<Object> cartsJson = hashOps.values();

        //如果购物车集合为空，直接返回
        if (CollectionUtils.isEmpty(cartsJson)) {
            return null;
        }

        //把List<Object>集合转换为List<Cart>
        return cartsJson.stream().map(cartJson -> JsonUtils.parse(cartJson.toString(), Cart.class)).collect(Collectors.toList());
    }

    /**
     * 修改购物车数量
     * @param cart
     */
    @Override
    public void updateCarts(Cart cart) {
        //1.先查询用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //判断用户是否存在购物车
        if (!redisTemplate.hasKey(KEY_PREFIX+userInfo.getId())){
            return;
        }
        //获取已知购物车的数量
        Integer num = cart.getNum();

        //2.获取用户的购物车记录
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        //获取map<skuId,cart>
        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();

        //反序列化cartJson
        cart = JsonUtils.parse(cartJson, Cart.class);

        cart.setNum(num);

        //把数据放入redis中
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
    }

    /**
     * 删除购物车里的商品
     * @param skuId
     */
    @Override
    public void deleteCart(String skuId) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //判断用户是否存在购物车信息
        if (!redisTemplate.hasKey(KEY_PREFIX+userInfo.getId())){
            return;
        }

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        hashOps.delete(skuId);

    }

}
