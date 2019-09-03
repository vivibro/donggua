package com.vivi.cart.web;


import com.vivi.cart.pojo.Cart;
import com.vivi.cart.service.CartService;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    @PutMapping
    public ResponseEntity<Void> updateCartList(@RequestBody Cart cart) {
        this.cartService.updateCartList(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
