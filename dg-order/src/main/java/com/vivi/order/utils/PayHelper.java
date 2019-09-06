package com.vivi.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.order.config.PayConfig;
import com.vivi.order.config.enums.PayState;
import com.vivi.order.mapper.OrderStatusMapper;
import com.vivi.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;
    @Autowired
    private PayConfig config;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    /**
     * @param orderId 商品订单id
     * @param totalPay 支付金额
     * @param desc 商品描述
     * @return 微信支付的url
     */
    public String createOrder(Long orderId,Long totalPay,String desc){
        try{
            Map<String,String> data = new HashMap<>();
//            商品描述
            data.put("body", desc);
//            订单号
            data.put("out_trade_no", orderId.toString());
//            支付金额
            data.put("total_fee", totalPay.toString());
//            调用微信支付的终端IP
            data.put("spbill_create_ip","127.0.0.1");
//            回调地址
            data.put("notify_url", config.getNotifyUrl());
//            交易类型
            data.put("trade_type", "NATIVE");
//            发起请求
            Map<String,String> result = wxPay.unifiedOrder(data);
            isSuccess(result);
            return result.get("code_url");
        }catch (Exception e){
            log.error("[微信支付获取失败]" + orderId);
            return null;
        }
    }


    /**
     * 判断通信与业务是否成功
     * @param result
     */
    public static void isSuccess(Map<String, String> result) {
        String returnCode = result.get("return_code");
//            判断通信标识
        if (returnCode.equals(WXPayConstants.FAIL)){
//                通信失败
            log.error("[微信下单] 通信失败,失败原因:{}"  ,result.get("return_msg"));
            throw new DgException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
//            判断业务标识
        String resultCode = result.get("result_code");
        if (resultCode.equals(WXPayConstants.FAIL)){
//                业务失败
            log.error("[微信下单] 业务失败,错误原因:{}，错误码:{}",result.get("err_code_des"),result.get("err_code"));
            throw new DgException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }

    /**
     * 签名校验
     * @param payResult
     */
    public void isSignValid(Map<String, String> payResult) {

        try {
            if(!WXPayUtil.isSignatureValid(payResult, config.getKey())){
             throw new DgException(ExceptionEnum.SIGN_INVALID);
            }
        } catch (Exception e) {
            throw new DgException(ExceptionEnum.SIGN_INVALID);
        }
    }

    /**
     * 支付状态查询
     * @param orderId
     */
    public PayState queryPayState(Long orderId){
        try {
            Map<String,String> data = new HashMap<>();
            data.put("out_trade_no", orderId.toString());
            Map<String, String> map = wxPay.orderQuery(data);
            isSuccess(map);
            isSignValid(map);
            String state = map.get("trade_state");
//            SUCCESS—支付成功
//
//            REFUND—转入退款
//
//            NOTPAY—未支付
//
//            CLOSED—已关闭
//
//            REVOKED—已撤销（付款码支付）
//
//            USERPAYING--用户支付中（付款码支付）
//
//            PAYERROR--支付失败(其他原因，如银行返回失败)
            if(state.equals("SUCCESS")){
                OrderStatus status = orderStatusMapper.selectByPrimaryKey(orderId);
                status.setStatus(2);
                status.setPaymentTime(new Date());
                orderStatusMapper.updateByPrimaryKey(status);
                return PayState.SUCCESS;
            }else if(state.equals("USERPAYING") || state.equals("NOTPAY")){
                return PayState.NOT_PAY;
            }else {
                return PayState.FAIL;
            }
        } catch (Exception e) {
            return PayState.NOT_PAY;
        }
    }

}
