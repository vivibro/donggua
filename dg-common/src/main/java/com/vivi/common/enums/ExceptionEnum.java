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
    GOODS_ID_CANNOT_BE_NULL("商品id不能为空",400);
    private String msg;
    private int code;

}
