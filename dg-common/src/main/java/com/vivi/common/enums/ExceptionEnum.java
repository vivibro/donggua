package com.vivi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//异常的枚举
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    BRAND_SAVE_ERROE("品牌新增失败",500),
    BRAND_NOT_FOUND("商品未被找到",404),
    PRICE_CANNOT_BE_NULL("价格不能为空",400),
    CATEGORY_NOT_FIND("商品分类没查到",404),
    UPLOAD_FILE_ERROR("文件上传失败",500 ),
    INVALOD_FILE_TYPE("无效文件类型", 500),
    GOODS_ID_CANNOT_BE_NULL("商品id不能为空",400),
    INVALID_USER_DATA_TYPE("请求数据有误",400),
    INVALID_VERIFY_CODE("无效的验证码",400),
    INVALID_USERNAME_OR_PASSWORD("用户名或密码有误",400),
    UNAUTHORIZED("无权访问",403),
    CART_NOT_FOUND("购物车为空",404),
    GOOD_STOCK_IS_NULL("商品库存不足",404),
    WX_PAY_ORDER_FAIL("微信下单失败",500),
    ORDER_STATUS_ERR("订单状态异常",403),
    SIGN_INVALID("无效签名",400),
    TOTAL_FEE_ERR("金额不符",400),
    UPDATE_ORDER_STATUS_ERR("更新订单状态失败",400),
    CREATE_TOKEN_FAIL("用户凭证生成失败",500);

    private String msg;
    private int code;

}
