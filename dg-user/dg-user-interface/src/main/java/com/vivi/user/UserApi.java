package com.vivi.user;

import com.vivi.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserApi {
    @GetMapping("query")
    User queryUserByUNAndPW(@RequestParam("username")String username, @RequestParam("password")String password);
}
