package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.dto.OrdersDto;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }
    /**
     * 订单查询
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize, HttpServletRequest request){
        //分页构造器
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> dtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByDesc(Orders::getId);
        Long userid=0L;
        if(request.getSession().getAttribute("user")!=null){
            userid=(Long) request.getSession().getAttribute("user");
        }
        queryWrapper.eq(Orders::getUserId,userid);
        //分页查询
        orderService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> list= records.stream().map((item)->{
            OrdersDto ordersDto=new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);
            LambdaQueryWrapper<OrderDetail> queryWrapper_detail = new LambdaQueryWrapper<>();
            queryWrapper_detail.eq(OrderDetail::getOrderId,item.getId());
            List<OrderDetail> details = orderDetailService.list(queryWrapper_detail);
            if (details!=null)
            {
                ordersDto.setOrderDetails(details);
            }
            return ordersDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        System.out.println("**************************************");
        log.info(dtoPage.getRecords().toString());
        System.out.println("***************************************");
        return R.success(dtoPage);
    }
    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, String number,String beginTime,String endTime){
        log.info("page:{},pagesize:{},number:{},beginTime:{},endTime:{}",page,pageSize,number,beginTime,endTime);
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        Page<OrdersDto> dtoPage = new Page<>();
        LambdaQueryWrapper<Orders> olqw =new LambdaQueryWrapper<>();
        //模糊匹配订单号
        olqw.like(number!=null,Orders::getNumber,number);
        //匹配订单时间
        olqw.between(beginTime!=null,Orders::getOrderTime,beginTime,endTime);
        orderService.page(ordersPage,olqw);

        return R.success(ordersPage);

    }
    /**
     * 订单状态修改——管理端
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> order(@RequestBody Orders orders){
        //log.info("orders:{}", orders);
        Orders order = orderService.getById(orders.getId());
        if (order.getStatus() == 2){
            orders.setStatus(3);
            orderService.updateById(orders);
            return R.success("订单派送成功");
        }else {
            orders.setStatus(4);
            orderService.updateById(orders);
            return R.success("订单已完成");
        }
    }
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        log.info(orders.toString());
        Orders orders1 = orderService.getById(orders.getId());
        log.info(orders1.toString());
        orders1.setId(null);

        return R.success("success");
    }
}
