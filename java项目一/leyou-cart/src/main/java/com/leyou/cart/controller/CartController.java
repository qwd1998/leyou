package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 登陆保存购物车
     *
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询购物车
     * @return
     */
    @GetMapping
    public ResponseEntity<List<Cart>> queryCarts() {
        List<Cart> carts = cartService.queryCarts();
        if (CollectionUtils.isEmpty(carts)){
            return  ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carts);
    }

    /**
     * 更新购物车数量
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateCarts(@RequestBody Cart cart) {
        cartService.updateCarts(cart);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 删除购物车中的商品
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId) {

        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}
