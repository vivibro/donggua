package com.vivi.user.web;

import com.vivi.user.pojo.User;
import com.vivi.user.service.UserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
public class UserController {
    @Autowired
    private UserService userService;


    /**
     * 校验数据唯一性；
     * @param data 数据
     * @param type 类型，1为用户名，2为电话号
     * @return ture为不重复 false为重复
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> cheak(@PathVariable("data")String data,@PathVariable("type")Integer type){
        return ResponseEntity.ok(userService.checkData(data,type));
    }


    /**
     * 发验证码
     * @param phone 手机号
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone")String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code")String code, BindingResult result){
        if (result.hasErrors()){
            throw new RuntimeException(result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage()).collect(Collectors.joining("|")));
        }
        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名密码查询
     * @param username
     * @param password
     * @return 用户的json格式
     */
    @GetMapping("query")
    public ResponseEntity<User> queryUserByUNAndPW(@RequestParam ("username")String username,
                                          @RequestParam("password")String password){
        return ResponseEntity.ok(userService.queryUserByUNAndPW(username,password));
    }
}
