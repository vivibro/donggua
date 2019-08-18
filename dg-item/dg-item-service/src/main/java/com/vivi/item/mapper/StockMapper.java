package com.vivi.item.mapper;

import com.vivi.item.pojo.Sku;
import com.vivi.item.pojo.Stock;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface StockMapper extends Mapper<Stock>, DeleteByIdListMapper<Stock,Long> {
}
