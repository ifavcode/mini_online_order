package cn.guetzjb.onlineorderingjava.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.onlineorderingjava.constant.MqTopicConstant;
import cn.guetzjb.onlineorderingjava.entity.OrderItem;
import cn.guetzjb.onlineorderingjava.entity.Orders;
import cn.guetzjb.onlineorderingjava.entity.RefundRecord;
import cn.guetzjb.onlineorderingjava.entity.SysUser;
import cn.guetzjb.onlineorderingjava.entity.dto.OrdersDTO;
import cn.guetzjb.onlineorderingjava.entity.dto.OrdersStatDTO;
import cn.guetzjb.onlineorderingjava.enums.OrdersEnum;
import cn.guetzjb.onlineorderingjava.repository.OrdersRepository;
import cn.guetzjb.onlineorderingjava.repository.RefundRecordRepository;
import cn.guetzjb.onlineorderingjava.service.ProducerService;
import cn.guetzjb.onlineorderingjava.service.WxCommonService;
import cn.guetzjb.onlineorderingjava.utils.NullAwareBeanUtils;
import cn.guetzjb.onlineorderingjava.utils.OrdersUtil;
import cn.guetzjb.onlineorderingjava.utils.WebSocketUtil;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/orders/admin")
@RequiredArgsConstructor
@Slf4j
public class OrdersAdminController {

    private final OrdersRepository ordersRepository;
    private final NullAwareBeanUtils beanUtils;
    private final OrdersUtil ordersUtil;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ProducerService producerService;
    private final WxPayService wxPayService;
    private final WxCommonService wxCommonService;
    private final RefundRecordRepository refundRecordRepository;

    @Value("${wx.pay.refund-notify-url}")
    private String refundNotifyUrl;
    @Value("${wx.pay.mchId}")
    private String mchId;

    @PostMapping
    @SaCheckPermission({"system:orders:add"})
    @Transactional
    public ResponseEntity<Map> createOrders(@RequestBody Orders orders) {
        orders.setOrderNo(ordersUtil.generateOrderNo());
        orders.setWxOrderNo(ordersUtil.generateWxOrderNo());
        for (OrderItem orderItem : orders.getOrderItems()) {
            orderItem.setOrder(orders);
        }
        int userId = StpUtil.getLoginIdAsInt();
        orders.setSysUser(SysUser.builder().userId(userId).build());

        // 检查库存
        ordersUtil.checkInventory(orders);
        if (orders.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            orders.setStatus(OrdersEnum.PAID.getCode());
        } else {
            orders.setStatus(OrdersEnum.PENDING_PAYMENT.getCode());
        }
        Orders saved = ordersRepository.save(orders);
        // 减库存
        ordersUtil.subtractInventory(saved);
        // 发送异步消息 延迟一小时
        producerService.sendDelayMessage(MqTopicConstant.ORDER_TIMEOUT, saved.getId(), 17);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                WebSocketUtil.sendAll(String.valueOf(saved.getId()));
            }
        });
        Map<String, ? extends Serializable> map = Map.of("success", true, "orderId", saved.getId(), "orderNo", saved.getOrderNo(), "wxOrderNo", saved.getWxOrderNo());
        return ResponseEntity.ok(map);
    }

    @PutMapping("/{id}")
    @SaCheckPermission({"system:orders:add"})
    @Transactional
    public ResponseEntity<Orders> updateOrders(@PathVariable Integer id, @RequestBody Orders orders) {
        Orders existingOrders = ordersRepository.findById(id).get();
        existingOrders.getOrderItems().clear();
        for (OrderItem orderItem : orders.getOrderItems()) {
            orderItem.setOrder(existingOrders);
            existingOrders.getOrderItems().add(orderItem);
        }
        beanUtils.copyProperties(orders, existingOrders, "orderItems");
        Orders saved = ordersRepository.save(existingOrders);
        if (Objects.equals(saved.getStatus(), OrdersEnum.COMPLETED.getCode())) {
            wxCommonService.sendTemplateMsg(saved.getSysUser().getOpenid(), saved);
        }
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/status/{id}")
    @SaCheckPermission({"system:orders:add"})
    @Transactional
    public ResponseEntity<Orders> updateOrdersStatus(@PathVariable Integer id, @RequestBody Orders orders) {
        Orders existingOrders = ordersRepository.findById(id).get();
        existingOrders.setStatus(orders.getStatus());
        Orders saved = ordersRepository.save(existingOrders);
        if (Objects.equals(saved.getStatus(), OrdersEnum.COMPLETED.getCode())) {
            wxCommonService.sendTemplateMsg(saved.getSysUser().getOpenid(), saved);
        }
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission({"system:orders:delete"})
    public ResponseEntity<Void> deleteOrders(@PathVariable Integer id) {
        return ordersRepository.findById(id)
                .map(orders -> {
                    ordersRepository.delete(orders);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/batch/{ids}")
    @SaCheckPermission({"system:orders:delete"})
    public ResponseEntity<Void> deleteOrdersBatch(@PathVariable List<Integer> ids) {
        ordersRepository.deleteAllById(ids);
        return ResponseEntity.noContent().<Void>build();
    }

    @GetMapping("/{id}")
    @SaCheckPermission({"system:orders:query"})
    public ResponseEntity<Orders> getOrdersById(@PathVariable Integer id) {
        Optional<Orders> orders = ordersRepository.findById(id);
        return orders.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @SaCheckPermission({"system:orders:query"})
    public ResponseEntity<List<Orders>> getAllOrders() {
        List<Orders> ordersList = ordersRepository.findAll();
        return ResponseEntity.ok(ordersList);
    }

    @GetMapping("/page")
    @SaCheckPermission({"system:orders:query"})
    public ResponseEntity<Page<Orders>> getOrdersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "", required = false) String orderNo,
            @RequestParam(defaultValue = "", required = false) String phone,
            @RequestParam(defaultValue = "", required = false) String status,
            @RequestParam(defaultValue = "", required = false) String orderType,
            @RequestParam(defaultValue = "", required = false) String startTime,
            @RequestParam(defaultValue = "", required = false) String endTime,
            @RequestParam(defaultValue = "", required = false) String openid) {

        Specification<Orders> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtils.hasText(orderNo)) {
                list.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNo")), "%" + orderNo.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(phone)) {
                list.add(criteriaBuilder.like(root.get("phone"), "%" + phone + "%"));
            }
            if (StringUtils.hasText(status)) {
                list.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(orderType)) {
                list.add(criteriaBuilder.equal(root.get("orderType"), orderType));
            }
            if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
                list.add(criteriaBuilder.between(root.get("createdAt"), LocalDateTime.parse(startTime, DATE_TIME_FORMATTER), LocalDateTime.parse(endTime, DATE_TIME_FORMATTER)));
            }
            if (StringUtils.hasText(openid)) {
                list.add(criteriaBuilder.like(root.get("sysUser").get("openid"), "%" + openid + "%"));
            }
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return criteriaBuilder.and(list.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(page, size);
        Page<Orders> ordersPage = ordersRepository.findAll(spec, pageable);

        return ResponseEntity.ok(ordersPage);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@RequestParam String startTime, @RequestParam String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER);
        LocalDateTime end = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER);
        List<Orders> orders = ordersRepository.findAllByCreatedAtBetween(start, end);
        orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        List<OrdersStatDTO> dtoList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (Orders cur : orders) {
            OrdersStatDTO dto = OrdersStatDTO.transOrders(cur);
            dtoList.add(dto);
        }
        map.put("ordersList", dtoList);

        List<RefundRecord> refundRecordList = refundRecordRepository.findAllBySuccessTimeBetween(startTime, endTime);
        refundRecordList.sort((o1, o2) -> o2.getSuccessTime().compareTo(o1.getSuccessTime()));
        map.put("refundRecordList", refundRecordList);

        return ResponseEntity.ok(map);
    }

    @PostMapping("/refund")
    @SaCheckPermission({"system:orders:refund"})
    public ResponseEntity<String> refund(@RequestParam String wxOrderNo, @RequestParam(required = false) BigDecimal refundFeeP,
                                         @RequestParam(required = false, defaultValue = "申请退款") String refundDesc) {
        Orders orders = ordersRepository.findByWxOrderNo(wxOrderNo);
        int totalFee = orders.getTotalAmount().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue(); // 分
        int refundFee = refundFeeP != null ?
                (refundFeeP.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue())
                :
                totalFee;
        if (refundNotifyUrl == null || refundNotifyUrl.isEmpty()) {
            log.warn("未配置退款回调URL，这会导致已退款的订单无法更新状态");
        }
        WxPayRefundRequest build = WxPayRefundRequest.newBuilder().outTradeNo(wxOrderNo).outRefundNo(ordersUtil.generateWxOrderNo())
                .refundDesc(refundDesc).totalFee(totalFee)
                .refundFee(refundFee)
                .opUserId(mchId)
                .notifyUrl(refundNotifyUrl)
                .build();
        try {
            wxPayService.refund(build);
        } catch (WxPayException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getErrCodeDes());
        }
        return ResponseEntity.ok("已发起退款");
    }

}
