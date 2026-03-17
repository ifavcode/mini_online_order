package cn.guetzjb.onlineorderingjava.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.onlineorderingjava.constant.MqTopicConstant;
import cn.guetzjb.onlineorderingjava.entity.*;
import cn.guetzjb.onlineorderingjava.enums.OrdersEnum;
import cn.guetzjb.onlineorderingjava.repository.DictRepository;
import cn.guetzjb.onlineorderingjava.repository.OrdersRepository;
import cn.guetzjb.onlineorderingjava.repository.TableRepository;
import cn.guetzjb.onlineorderingjava.service.ProducerService;
import cn.guetzjb.onlineorderingjava.service.WxCommonService;
import cn.guetzjb.onlineorderingjava.utils.NullAwareBeanUtils;
import cn.guetzjb.onlineorderingjava.utils.OrdersUtil;
import cn.guetzjb.onlineorderingjava.utils.WebSocketUtil;
import com.alibaba.fastjson2.JSONObject;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersRepository ordersRepository;
    private final NullAwareBeanUtils beanUtils;
    private final OrdersUtil ordersUtil;
    private final WxCommonService wxCommonService;
    private final ProducerService producerService;
    private final TableRepository tableRepository;
    private final DictRepository dictRepository;

    @PostMapping
    @Transactional
    @SaCheckLogin
    public ResponseEntity<Map> createOrders(@RequestBody Orders orders) {
        orders.setOrderNo(ordersUtil.generateOrderNo());
        orders.setWxOrderNo(ordersUtil.generateWxOrderNo());
        for (OrderItem orderItem : orders.getOrderItems()) {
            orderItem.setOrder(orders);
        }
        int userId = StpUtil.getLoginIdAsInt();
        orders.setSysUser(SysUser.builder().userId(userId).build());
        if (orders.getTable() != null && orders.getTable().getTableCode() != null) {
            Table byTableCode = tableRepository.findByTableCode(orders.getTable().getTableCode());
            orders.setTable(byTableCode);
        } else {
            orders.setTable(null);
        }

        // 检查库存
        ordersUtil.checkInventory(orders);
        if (orders.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            orders.setStatus(OrdersEnum.PAID.getCode());
        } else {
            List<SysDict> paySwitch = dictRepository.findByDictKeyStartingWith("pay_switch");
            if (paySwitch != null && !paySwitch.isEmpty()) {
                if (paySwitch.getFirst().getDictValue().equals("0")) {
                    orders.setStatus(OrdersEnum.PAID.getCode());
                } else {
                    orders.setStatus(OrdersEnum.PENDING_PAYMENT.getCode());
                }
            } else {
                orders.setStatus(OrdersEnum.PENDING_PAYMENT.getCode());
            }
        }
        Orders saved = ordersRepository.save(orders);

        // 减库存
        ordersUtil.subtractInventory(saved);

        // 发送异步消息 延迟一小时
        producerService.sendDelayMessage(MqTopicConstant.ORDER_TIMEOUT, saved.getId(), 17);
        // 管理端提示
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String openid = StpUtil.getSession().get("openid").toString();
                WebSocketUtil.sendAll(String.valueOf(saved.getId()));
            }
        });
        Map<String, ? extends Serializable> map = Map.of("success", true, "orderId", saved.getId(), "orderNo", saved.getOrderNo(), "wxOrderNo", saved.getWxOrderNo());
        return ResponseEntity.ok(map);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Orders> updateOrders(@PathVariable Integer id, @RequestBody Orders order) {
        return ordersRepository.findById(id).map(existingOrders -> {
            if (!ordersUtil.checkSelfOrder(existingOrders)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Orders());
            }
            beanUtils.copyProperties(order, existingOrders);
            Orders updatedOrders = ordersRepository.save(existingOrders);
            return ResponseEntity.ok(updatedOrders);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrders(@PathVariable Integer id) {
        return ordersRepository.findById(id).map(order -> {
            if (!ordersUtil.checkSelfOrder(order)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
            }
            ordersRepository.delete(order);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orders> getOrdersById(@PathVariable Integer id) {
        Optional<Orders> order = ordersRepository.findById(id);
        if (order.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Orders());
        }
        if (!ordersUtil.checkSelfOrder(order.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Orders());
        }
        return order.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @SaCheckLogin
    public ResponseEntity<List<Orders>> getAllOrders() {
        List<Orders> orderList = ordersRepository.findAllBySysUser_UserId(StpUtil.getLoginIdAsInt());
        return ResponseEntity.ok(orderList);
    }

    @GetMapping("/page")
    @SaCheckLogin
    public ResponseEntity<Page<Orders>> getOrdersPage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "") String orderNo) {

        int userId = StpUtil.getLoginIdAsInt();

        Specification<Orders> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtils.hasText(orderNo)) {
                list.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNo")), "%" + orderNo.toLowerCase() + "%"));
            }
            Join<Orders, SysUser> sysUserJoin = root.join("sysUser", JoinType.LEFT);
            list.add(criteriaBuilder.equal(sysUserJoin.get("userId"), userId));
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return criteriaBuilder.and(list.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(page, size);
        Page<Orders> orderPage = ordersRepository.findAll(spec, pageable);

        return ResponseEntity.ok(orderPage);
    }
}
