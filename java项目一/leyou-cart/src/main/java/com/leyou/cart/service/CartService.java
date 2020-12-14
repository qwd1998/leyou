package com.leyou.cart.service;

import com.leyou.cart.pojo.Cart;

import java.util.List;

public interface CartService {
    void addCart(Cart cart);

    List<Cart> queryCarts();

    void updateCarts(Cart cart);

    void deleteCart(String skuId);


}
