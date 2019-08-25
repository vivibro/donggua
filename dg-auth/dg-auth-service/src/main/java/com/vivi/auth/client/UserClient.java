package com.vivi.auth.client;

import com.vivi.user.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "user-service")
public interface UserClient extends UserApi {
}
