package com.vivi.order.service;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.wxpay.sdk.WXPayUtil;
import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.common.utils.IdWorker;
import com.vivi.common.vo.PageResult;
import com.vivi.entity.UserInfo;
import com.vivi.order.config.enums.PayState;
import com.vivi.order.mapper.OrderMapper;
import com.vivi.order.pojo.Order;
import com.vivi.order.pojo.OrderDetail;
import com.vivi.order.pojo.OrderStatus;
import com.vivi.order.utils.PayHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.vivi.entity.UserInfo;

import com.vivi.item.pojo.Stock;
import com.vivi.order.interceptor.UserInterceptor;
import com.vivi.order.mapper.*;
import com.vivi.order.pojo.Order;
import com.vivi.order.pojo.OrderDetail;
import com.vivi.order.pojo.OrderStatus;
import com.vivi.order.service.OrderService;
import com.vivi.order.service.OrderStatusService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.BoundHashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.sound.midi.SoundbankResource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService{

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private PayHelper payHelper;

//    @Autowired
//    private OrderStatusService orderStatusService;

//    @Autowired
//    private GoodsClient goodsClient;
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private SeckillOrderMapper seckillOrderMapper;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

//    添加事务，发生异常则回滚
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Order order) {
        //创建订单
        //1.生成orderId
        long orderId = idWorker.nextId();
        //2.获取登录的用户
        UserInfo userInfo = UserInterceptor.getThreadLocal();
        //3.初始化数据
        order.setBuyerNick(userInfo.getUsername());
        order.setBuyerRate(false);
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setUserId(userInfo.getId());
        //4.保存数据
        this.orderMapper.insertSelective(order);

        //5.保存订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        //初始状态未未付款：1
        orderStatus.setStatus(1);
        //6.保存数据
        int selective = this.orderStatusMapper.insertSelective(orderStatus);
        System.out.print("保存订单状态" + selective );

        //7.在订单详情中添加orderId
        order.getOrderDetails().forEach(orderDetail -> {
            //添加订单
            orderDetail.setOrderId(orderId);
        });

        //8.保存订单详情，使用批量插入功能
        this.orderDetailMapper.insertList(order.getOrderDetails());

        order.getOrderDetails().forEach(orderDetail -> this.stockMapper.reduceStock(orderDetail.getSkuId(), orderDetail.getNum()));

        return orderId;

    }
    /**
     * 查询订单下商品的库存，返回库存不足的商品Id
     * @param order
     * @return
     */
    public List<Long> queryStock(Order order) {
        List<Long> skuId = new ArrayList<>();
        order.getOrderDetails().forEach(orderDetail -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(orderDetail.getSkuId());
            if (stock.getStock() - orderDetail.getNum() < 0){
                //先判断库存是否充足
                skuId.add(orderDetail.getSkuId());
            }
        });
        return skuId;
    }

    public String createPayUrl(Long orderId) {
//        查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
//        判断订单状态
        Integer status = orderStatusMapper.selectByPrimaryKey(orderId).getStatus();
        if (status!=1) {
//            不在未支付状态就抛异常
            throw new DgException(ExceptionEnum.ORDER_STATUS_ERR);
        }
        Long totalPay = order.getTotalPay().longValue();
        String WXPayUrl = payHelper.createOrder(orderId, totalPay,"商城消费");
        return WXPayUrl;
    }

    public void handlerNotify(Map<String, String> payResult) {
//        判断通信和业务标识是否成功
        payHelper.isSuccess(payResult);
//        判断签名
        payHelper.isSignValid(payResult);
//        校验金额
        long total_fee = Long.parseLong(payResult.get("total_fee"));
        Order order = orderMapper.selectByPrimaryKey(Long.parseLong(payResult.get("out_tarde_no")));
        Double totalPay = order.getTotalPay();
        if(totalPay != total_fee){
            throw new DgException(ExceptionEnum.TOTAL_FEE_ERR);
        }
//        修改订单状态
        OrderStatus status = orderStatusMapper.selectByPrimaryKey(payResult.get("out_tarde_no"));
        status.setPaymentTime(new Date());
        status.setStatus(2);
        int count =orderStatusMapper.updateByPrimaryKey(status);
        if (count != 1){
            throw new DgException(ExceptionEnum.UPDATE_ORDER_STATUS_ERR);
        }
    }

    public PayState queryOrderStatus(Long id) {
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        Integer status = orderStatus.getStatus();
        if (status != 1){
            return PayState.SUCCESS;
        }
//    状态等于1不代表支付失败，有可能消息还没到。可以主动去微信接口查
        PayState payState = payHelper.queryPayState(id);
        return payState;
    }


//    /**
//     * 根据订单号查询订单
//     * @param id
//     * @return
//     */
//    public Order queryOrderById(Long id) {
////        System.out.print("订单id："+ "id");
////        sid.substring(3, sid.length()-3);
//
//        //1.查询订单
//        Order order = this.orderMapper.selectByPrimaryKey(id);
//        //2.查询订单详情
//        OrderDetail orderDetail1 = new OrderDetail();
//        orderDetail1.setId(id);
////        Example example = new Example(OrderDetail.class);
////        example.createCriteria().andEqualTo("orderId",id);
//        List<OrderDetail> orderDetail = this.orderDetailMapper.select(orderDetail1);
//        orderDetail.forEach(System.out::println);
//        //3.查询订单状态
//        OrderStatus orderStatus = this.orderStatusMapper.selectByPrimaryKey(order.getOrderId());
//        //4.order对象填充订单详情
//        order.setOrderDetails(orderDetail);
//        //5.order对象设置订单状态
////        order.setOrderStatus(orderStatus);
//        order.setStatus(orderStatus.getStatus());
//        //6.返回order
//        return order;
//    }
//
//    /**
//     * 查询当前登录用户的订单，通过订单状态进行筛选
//     * @param page
//     * @param rows
//     * @param status
//     * @return
//     */
//    public PageResult<Order> queryUserOrderList(Integer page, Integer rows, Integer status) {
//        try{
//            //1.分页
//            PageHelper.startPage(page,rows);
//            //2.获取登录用户
//            UserInfo userInfo = LoginInterceptor.getLoginUser();
//            //3.查询
//            Page<Order> pageInfo = (Page<Order>) this.orderMapper.queryOrderList(userInfo.getId(), status);
//            //4.填充orderDetail
//            List<Order> orderList = pageInfo.getResult();
//            orderList.forEach(order -> {
//                Example example = new Example(OrderDetail.class);
//                example.createCriteria().andEqualTo("orderId",order.getOrderId());
//                List<OrderDetail> orderDetailList = this.orderDetailMapper.selectByExample(example);
//                order.setOrderDetails(orderDetailList);
//            });
//            return new PageResult<>(pageInfo.getTotal(),(long)pageInfo.getPages(), orderList);
//        }catch (Exception e){
//            logger.error("查询订单出错",e);
//            return null;
//        }
//    }
//
//    /**
//     * 更新订单状态
//     * @param id
//     * @param status
//     * @return
//     */
//    public Boolean updateOrderStatus(Long id, Integer status) {
//        UserInfo userInfo = LoginInterceptor.getLoginUser();
//        Long spuId = this.goodsClient.querySkuById(findSkuIdByOrderId(id)).getSpuId();
//
//        OrderStatus orderStatus = new OrderStatus();
//        orderStatus.setOrderId(id);
//        orderStatus.setStatus(status);
//
//        //延时消息
//        OrderStatusMessage orderStatusMessage = new OrderStatusMessage(id,userInfo.getId(),userInfo.getUsername(),spuId,1);
//        OrderStatusMessage orderStatusMessage2 = new OrderStatusMessage(id,userInfo.getId(),userInfo.getUsername(),spuId,2);
//        //1.根据状态判断要修改的时间
//        switch (status){
//            case 2:
//                //2.付款时间
//                orderStatus.setPaymentTime(new Date());
//                break;
//            case 3:
//                //3.发货时间
//                orderStatus.setConsignTime(new Date());
//                //发送消息到延迟队列，防止用户忘记确认收货
//                orderStatusService.sendMessage(orderStatusMessage);
//                orderStatusService.sendMessage(orderStatusMessage2);
//                break;
//            case 4:
//                //4.确认收货，订单结束
//                orderStatus.setEndTime(new Date());
//                orderStatusService.sendMessage(orderStatusMessage2);
//                break;
//            case 5:
//                //5.交易失败，订单关闭
//                orderStatus.setCloseTime(new Date());
//                break;
//            case 6:
//                //6.评价时间
//                orderStatus.setCommentTime(new Date());
//                break;
//
//            default:
//                return null;
//        }
//        int count = this.orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
//        return count == 1;
//    }
//
//    /**
//     * 根据订单号查询商品id
//     * @param id
//     * @return
//     */
//    public List<Long> querySkuIdByOrderId(Long id) {
//        Example example = new Example(OrderDetail.class);
//        example.createCriteria().andEqualTo("orderId",id);
//        List<OrderDetail> orderDetailList = this.orderDetailMapper.selectByExample(example);
//        List<Long> ids = new ArrayList<>();
//        orderDetailList.forEach(orderDetail -> ids.add(orderDetail.getSkuId()));
//        return ids;
//    }
//
//    /**
//     * 根据订单号查询订单状态
//     * @param id
//     * @return
//     */
//    public OrderStatus queryOrderStatusById(Long id) {
//        return this.orderStatusMapper.selectByPrimaryKey(id);
//    }


//    /**
//     * 根据订单id查询其skuId
//     * @param id
//     * @return
//     */
//    public Long findSkuIdByOrderId(Long id){
//        Example example = new Example(OrderDetail.class);
//        example.createCriteria().andEqualTo("orderId", id);
//        List<OrderDetail> orderDetail = this.orderDetailMapper.selectByExample(example);
//        return orderDetail.get(0).getSkuId();
//    }

}

