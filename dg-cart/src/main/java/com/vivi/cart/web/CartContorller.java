package com.vivi.cart.web;


import com.vivi.cart.pojo.Cart;
import com.vivi.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartContorller {

    @Autowired
    private CartService cartService;

//    购物车商品新增
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
//    已登录购物车查询
    @GetMapping
    public ResponseEntity<List<Cart>> queryCartList() {
//        System.out.println("123");
        List<Cart> carts = this.cartService.queryCartList();
        if (carts == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(carts);
    }
//    购物车内容更新
    @PostMapping("/up")
    public ResponseEntity<Void> updateCartList(@RequestBody Cart cart) {
        this.cartService.updateCartList(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

//    购物车删除
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId) {
        this.cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }
}
